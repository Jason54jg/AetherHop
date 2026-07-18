pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") { name = "FabricMC" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
        maven("https://maven.parchmentmc.org") { name = "ParchmentMC" }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("dev.kikugie.stonecutter") version "0.9.6"
    // Applied here (not `apply false`!) so its Settings-level hook can inject the right
    // Fabric Loom variant (remap vs. unobfuscated) onto each version subproject's buildscript
    // classpath before that subproject's build.gradle.kts evaluates.
    id("dev.kikugie.loom-back-compat") version "0.4"
}

// Fabric only - NeoForge and Quilt targets were dropped.
val mcVersions = listOf(
    "1.21", "1.21.1", "1.21.2", "1.21.3", "1.21.4", "1.21.5", "1.21.6",
    "1.21.7", "1.21.8", "1.21.9", "1.21.10", "1.21.11",
    "26.1", "26.1.1", "26.1.2", "26.2"
)

stonecutter {
    create(rootProject) {
        versions(*mcVersions.toTypedArray())
        vcsVersion = "26.2"
    }
}

rootProject.name = "AetherHop"
