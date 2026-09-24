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

// Fabric only. One build per API-compatible group of Minecraft versions, each compiled against
// the group's newest member and shipped with a fabric.mod.json range covering the whole group
// (see mcCompat in stonecutter.gradle.kts). The groups match the real API boundaries, verified
// member-by-member against the official mappings / client jars:
//   1.21.10 -> 1.21 .. 1.21.10   (Connection.connect(.., boolean, ..), Minecraft.setScreen)
//   1.21.11 -> 1.21.11           (Connection.connect(.., EventLoopGroupHolder, ..))
//   26.1.2  -> 26.1 .. 26.1.2
//   26.3    -> 26.2 .. 26.3      (Minecraft.setScreen removed in 26.2 -> setScreenAndShow;
//                                 26.3 checked identical to 26.2 for everything the mod uses)
val mcVersions = listOf("1.21.10", "1.21.11", "26.1.2", "26.3")

stonecutter {
    create(rootProject) {
        versions(*mcVersions.toTypedArray())
        vcsVersion = "26.3"
    }
}

rootProject.name = "AetherHop"
