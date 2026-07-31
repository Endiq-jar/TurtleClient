pluginManagement {
	repositories {
		maven {
			name = "Fabric"
			url = uri("https://maven.fabricmc.net/")
		}
		mavenCentral()
		gradlePluginPortal()
	}

	plugins {
    id("fabric-loom") version providers.gradleProperty("loom_version")
}
}

// Should match your modid
rootProject.name = "turtle-client"
