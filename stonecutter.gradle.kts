plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active file(".sc_active_version")

// Per-build dependency coordinates. Each entry is keyed by the Stonecutter project name (the
// Minecraft version the build is compiled against). Versions verified against Modrinth and each
// Minecraft version's piston-meta manifest - see mc_ecosystem_facts_2026 memory.

// fabric.mod.json "minecraft" range each build declares: every version in the group whose used
// API was checked to be identical (Minecraft members via official mappings / client jars, Cloth
// Config via javap across the group).
extra["mcCompat"] = mapOf(
    "1.21.10" to ">=1.21 <=1.21.10",
    "1.21.11" to "1.21.11",
    "26.1.2" to ">=26.1 <=26.1.2",
    "26.3" to ">=26.2 <=26.3"
)
extra["fabricApiVersions"] = mapOf(
    "1.21.10" to "0.138.4+1.21.10",
    "1.21.11" to "0.141.5+1.21.11",
    "26.1.2" to "0.155.2+26.1.2",
    "26.3" to "0.161.0+26.3"
)
extra["clothConfigVersions"] = mapOf(
    "1.21.10" to "20.0.149",
    "1.21.11" to "21.11.153",
    "26.1.2" to "26.1.154",
    "26.3" to "26.3.159"
)
extra["modMenuVersions"] = mapOf(
    "1.21.10" to "16.0.1",
    "1.21.11" to "17.0.1-beta.1",
    "26.1.2" to "18.0.0",
    "26.3" to "21.0.0"
)

// Minecraft bundles neither netty-handler-proxy nor netty-codec-socks in any version (so SOCKS
// mode needs both), and netty-codec-http only from 1.21.9 (so HTTP mode needs it on 1.21 - 1.21.8).
// Loom's include() isn't transitive, so every module is listed and jar-in-jar'd explicitly.
// Pinned to the OLDEST Netty in each group: newer Netty stays compatible with older handler jars,
// not the other way round. Where the game does bundle a module, the game's copy loads first.
extra["nettyVersions"] = mapOf(
    "1.21.10" to "4.1.97.Final",   // group spans Netty 4.1.97 (1.21) .. 4.1.118 (1.21.10)
    "1.21.11" to "4.2.7.Final",
    "26.1.2" to "4.2.7.Final",
    "26.3" to "4.2.15.Final"   // group spans Netty 4.2.15 (26.2) .. 4.2.16 (26.3)
)
extra["nettyModules"] = mapOf(
    "1.21.10" to listOf("netty-handler-proxy", "netty-codec-socks", "netty-codec-http"),
    "1.21.11" to listOf("netty-handler-proxy", "netty-codec-socks"),
    "26.1.2" to listOf("netty-handler-proxy", "netty-codec-socks"),
    "26.3" to listOf("netty-handler-proxy", "netty-codec-socks")
)

// See https://stonecutter.kikugie.dev/wiki/config/params
stonecutter parameters {
    // Swaps replace everything from the marker comment to end-of-line, so the trailing
    // `;` from the original placeholder statement must be part of the replacement text.
    swaps["mod_id"] = "\"${property("mod.id")}\";"
    swaps["mod_name"] = "\"${property("mod.name")}\";"
    swaps["mod_version"] = "\"${property("mod.version")}\";"
    swaps["minecraft"] = "\"${current.project}\";"

    replacements {
        // Mojang renamed ResourceLocation to Identifier starting with 1.21.11 (carries into 26.1+).
        string(current.parsed >= "1.21.11") {
            replace("ResourceLocation", "Identifier")
            replace("location()", "identifier()")
        }
    }
}
