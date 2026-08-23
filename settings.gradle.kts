pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    }
}

plugins {
    // Multi-version management. Check latest at https://stonecutter.kikugie.dev/blog/changes/0.9
    id("dev.kikugie.stonecutter") version "0.9.7"

    // Bridges the pre-26.1 (Yarn/obfuscated) and 26.1+ (Mojang names/unobfuscated) toolchains
    // so one build script works across the whole range. https://codeberg.org/KikuGie/loom-back-compat
    id("dev.kikugie.loom-back-compat") version "0.4.2"

    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

stonecutter {
    create(rootProject) {
        // Yarn-mapped era (obfuscated) -- edit this list to add/drop versions.
        versions(
            "1.17.1",
            "1.18.2",
            "1.19.4",
            "1.20.1",
            "1.20.6",
            "1.21.1",
            "1.21.4",
            "1.21.11"
        )
        // Unobfuscated era (Mojang names, no remapping) -- 26.1+
        version("26.2", "26.2")

        // Version whose source is treated as the git-diff baseline when you
        // switch the active version. Keep this on the version you edit most.
        vcsVersion = "1.21.4"
    }
}

rootProject.name = "turtle-client"
