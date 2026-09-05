import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    // Applies the correct Loom variant (remapping Yarn build vs. unobfuscated
    // Mojang-names build) automatically based on the active Minecraft version.
    id("dev.kikugie.loom-back-compat")
    id("org.jetbrains.kotlin.jvm") version "2.1.20"
    `maven-publish`
}

// DO NOT set group here manually per-version -- Stonecutter needs it consistent.
group = property("mod.group") as String
version = "${property("mod.version")}+${sc.current.version}"
base.archivesName.set(property("mod.id") as String)

val requiredJava: JavaVersion = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    sc.current.parsed >= "1.18" -> JavaVersion.VERSION_17
    sc.current.parsed >= "1.17" -> JavaVersion.VERSION_16
    else -> JavaVersion.VERSION_1_8
}

repositories {
    maven("https://maven.fabricmc.net/") { name = "Fabric" }
}

loom {
    // splitEnvironmentSourceSets() requires the client jar to bundle a server
    // jar, which only exists from 1.18 onward -- 1.17.1 doesn't have one and
    // throws UnsupportedOperationException trying to split. TurtleClient has
    // no server-side code at all (see fabric.mod.json: "environment": "client"),
    // so there's nothing the split was protecting here. Merge src/client into
    // the main source set instead, for every version uniformly.
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json")
}

sourceSets.main.get().apply {
    // src/client/{java,kotlin,resources} used to be merged in here via extra
    // srcDir() entries. That silently didn't work: this whole build goes through
    // Stonecutter's per-node generated source tree (see the `stonecutterGenerate`
    // task, which runs before processResources/compileKotlin for every node,
    // including the "active" one), and Stonecutter only redirects the standard
    // src/main/{java,kotlin,resources} convention dirs into that generated tree --
    // it has no reason to know about a custom src/client dir. Real CI logs confirm
    // it: processResources found turtle-client.mixins.json (from src/main/resources)
    // but never turtle-client.client.mixins.json (from src/client/resources), on
    // every node. Nothing here compiled or packaged those files; the resulting jars
    // were just missing everything client-side, which is what crashed the game with
    // "resource ... was invalid or could not be read" on FabricMixinBootstrap.
    //
    // Fix: src/client/{java,kotlin,resources} was merged directly into
    // src/main/{java,kotlin,resources} (same relative subpaths, no filename
    // collisions) so everything goes through the one tree Stonecutter actually
    // redirects. The mod is client-only anyway ("environment": "client" in
    // fabric.mod.json), so this split was never load-bearing at the Fabric level --
    // it was purely a dev-side organizational choice, and not one worth fighting
    // Stonecutter over.
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")

    // loomx.applyMojangMappings() does NOT auto-switch between Yarn and Mojang
    // per node -- per the plugin's own docs it always applies Mojang's official
    // mapping names (it's a shorthand for mappings(loom.officialMojangMappings()),
    // "but only on remapped versions" -- i.e. it's a no-op on unobfuscated ones,
    // not a Yarn fallback). The mod's source is written entirely in Yarn's naming
    // (MinecraftClient, KeyBinding, InputUtil, DrawContext, Identifier, Text,
    // RenderLayer, textRenderer, drawTextWithShadow, etc, across 19+ files), so
    // forcing Mojang mappings on every node breaks every Minecraft-API reference
    // in the whole mod. 26.2 ships fully unobfuscated with no Yarn mappings at
    // all, so it's the one node that has no choice but Mojang's names -- every
    // other node needs real Yarn to match how the code is actually written.
    if (sc.current.parsed >= "26.1") {
        loomx.applyMojangMappings()
    } else {
        val yarnBuild: String = sc.properties["deps.yarn"]
        mappings("net.fabricmc:yarn:$yarnBuild:v2")
    }

    val fabricApiVersion: String = sc.properties["deps.fabric_api"]

    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
    modImplementation("net.fabricmc:fabric-language-kotlin:${property("deps.fabric_kotlin")}")
}

tasks.processResources {
    fun MutableMap<String, String>.register(key: String, prop: String) {
        val value: String = sc.properties[prop]
        inputs.property(key, value)
        set(key, value)
    }

    val props = buildMap {
        register("id", "mod.id")
        register("name", "mod.name")
        register("version", "mod.version")
        register("minecraft", "mod.mc_compat")
    }

    filesMatching("fabric.mod.json") { expand(props) }

    // Replace just the compatibility value, not Groovy-expand the whole JSON:
    // nested mixin class names contain '$' (e.g. ExampleClientMixin$SplashMixin).
    inputs.property("mixinJava", requiredJava.majorVersion)
    filesMatching("*.mixins.json") {
        filter { line ->
            when {
                sc.current.parsed < "1.21.2" && line.contains("\"BadgeRenderStateMixin\"") -> ""
                sc.current.parsed >= "1.19" && line.contains("\"LegacyChatMixin\"") -> ""
                sc.current.parsed >= "1.19.4" && line.contains("\"LegacyFpsAccessor\"") -> ""
                else -> line.replace("\"JAVA_21\"", "\"JAVA_${requiredJava.majorVersion}\"")
            }
        }
    }

    // Guard rail: verify every mixins.json fabric.mod.json expects is both PRESENT
    // in the packaged output and still valid JSON. A crash log from a 1.21.1 device
    // build hit "java.lang.IllegalArgumentException: The specified resource
    // 'turtle-client.client.mixins.json' was invalid or could not be read" out of
    // FabricMixinBootstrap. That message covers two different Mixin-side failures --
    // resource not found at all, vs. found but unparseable -- and CI had previously
    // gone green on a build that still crashed on-device this way, because a
    // content-only JSON check silently passes on zero files found. Checking for the
    // expected filenames first closes that blind spot.
    doLast {
        val expectedMixinConfigs = setOf("turtle-client.mixins.json", "turtle-client.client.mixins.json")
        // NOTE: destinationDirectory (Provider API) only exists on AbstractArchiveTask
        // (Jar/Zip/Tar). processResources is a Copy/ProcessResources task, which never
        // had that property -- only the plain File-typed destinationDir getter, which
        // Gradle 9.5.1 still has. Do NOT "fix" this back to destinationDirectory; that
        // was the bug, not the fix.
        val packaged = fileTree(destinationDir) { include("**/*.mixins.json") }.files.associateBy { it.name }
        val missing = expectedMixinConfigs - packaged.keys
        if (missing.isNotEmpty()) {
            throw GradleException(
                "processResources for ${sc.current.version} did not package expected mixin config(s): $missing "
                    + "(found: ${packaged.keys}). This is why the game crashes with "
                    + "\"resource ... was invalid or could not be read\" -- fabric.mod.json references a file "
                    + "that never made it into the jar."
            )
        }

        val broken = packaged.values.mapNotNull { file ->
            runCatching { groovy.json.JsonSlurper().parse(file) }
                .exceptionOrNull()?.let { "${file.name}: ${it.message}" }
        }
        if (broken.isNotEmpty()) {
            throw GradleException("processResources emitted invalid mixin config JSON:\n" + broken.joinToString("\n"))
        }
    }
}
tasks.withType<JavaCompile>().configureEach {
    options.release = requiredJava.majorVersion.toInt()
}

kotlin {
    compilerOptions {
        // Kotlin Gradle plugin is pinned to 2.1.20 (see deps.fabric_kotlin comment
        // in stonecutter.properties.toml re: the Loom remap metadata ceiling), but
        // JvmTarget.JVM_25/26 weren't added until Kotlin 2.3.0. On the 26.2 node
        // (requiredJava = VERSION_25) JvmTarget.fromTarget("25") throws
        // "Unknown Kotlin JVM target: 25". Fall back to the highest target this
        // plugin version actually knows about rather than bumping the plugin
        // globally, which would reintroduce the remap failure on every other node.
        jvmTarget = runCatching { JvmTarget.fromTarget(requiredJava.majorVersion) }
            .getOrElse { JvmTarget.entries.last() }
    }
}

java {
    withSourcesJar()
    sourceCompatibility = requiredJava
    targetCompatibility = requiredJava

    toolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}

val projectName = property("mod.id") as String
val modVersion = property("mod.version") as String

tasks.jar {
    inputs.property("projectName", projectName)
    from("LICENSE") {
        rename { "${it}_$projectName" }
    }
}

// Builds every version and collects the jars into build/libs/<mc-version>/
// Run from Termux/CI with: ./gradlew chiseledBuild
tasks.register<Copy>("buildAndCollect") {
    group = "build"
    description = "Builds this version's jar and copies it to build/libs/{mc version}/"

    inputs.property("version", modVersion)
    from(loomx.modJar.flatMap { it.archiveFile }, loomx.modSourcesJar.flatMap { it.archiveFile })
    into(rootProject.layout.buildDirectory.file("libs/${sc.current.version}"))
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}

// Temporary CI diagnostics while validating the multi-version migration.
if (System.getenv("GITHUB_ACTIONS") == "true") {
    if (!rootProject.extra.has("turtleClient.problemMatchers")) {
        rootProject.extra["turtleClient.problemMatchers"] = true
        logger.lifecycle("::add-matcher::${rootProject.file(".github/problem-matchers.json")}")
    }
    if (sc.current.version == "26.2") {
        apply(from = rootProject.file(".github/inspect-api.init.gradle"))
    }
}
