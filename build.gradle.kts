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
    java.srcDir("src/client/java")
    kotlin.srcDir("src/client/kotlin")
    resources.srcDir("src/client/resources")
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")

    // Yarn on pre-26.1 (obfuscated) versions, Mojang's own names on 26.1+
    // (unobfuscated) -- loom-back-compat picks the right one per node.
    loomx.applyMojangMappings()

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

    val mixinJava = "JAVA_${requiredJava.majorVersion}"
    filesMatching("*.mixins.json") { expand("java" to mixinJava) }
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

    inputs.property("version", property("mod.version"))
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
