plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active file(".sc_active_version")

// Per-Minecraft-version dependency coordinates, shared by every loader's buildscript.
// Verified against Modrinth/NeoForged metadata on 2026-07-17 - see mc_ecosystem_facts_2026 memory.
extra["fabricApiVersions"] = mapOf(
    "1.21" to "0.102.0+1.21",
    "1.21.1" to "0.116.14+1.21.1",
    "1.21.2" to "0.106.1+1.21.2",
    "1.21.3" to "0.114.1+1.21.3",
    "1.21.4" to "0.119.4+1.21.4",
    "1.21.5" to "0.128.2+1.21.5",
    "1.21.6" to "0.128.2+1.21.6",
    "1.21.7" to "0.129.0+1.21.7",
    "1.21.8" to "0.136.1+1.21.8",
    "1.21.9" to "0.134.1+1.21.9",
    "1.21.10" to "0.138.4+1.21.10",
    "1.21.11" to "0.141.5+1.21.11",
    "26.1" to "0.155.2+26.1.2",
    "26.1.1" to "0.155.2+26.1.2",
    "26.1.2" to "0.155.2+26.1.2",
    "26.2" to "0.155.2+26.2"
)
extra["clothConfigVersions"] = mapOf(
    "1.21" to "15.0.140",
    "1.21.1" to "15.0.140",
    "1.21.2" to "16.0.143",
    "1.21.3" to "16.0.143",
    "1.21.4" to "17.0.144",
    "1.21.5" to "18.0.145",
    "1.21.6" to "19.0.147",
    "1.21.7" to "19.0.147",
    "1.21.8" to "19.0.147",
    "1.21.9" to "20.0.149",
    "1.21.10" to "20.0.149",
    "1.21.11" to "21.11.153",
    "26.1" to "26.1.154",
    "26.1.1" to "26.1.154",
    "26.1.2" to "26.1.154",
    "26.2" to "26.2.155"
)
extra["modMenuVersions"] = mapOf(
    "1.21" to "11.0.4",
    "1.21.1" to "11.0.4",
    "1.21.2" to "12.0.1",
    "1.21.3" to "12.0.1",
    "1.21.4" to "13.0.4",
    "1.21.5" to "14.0.2",
    "1.21.6" to "15.0.2",
    "1.21.7" to "15.0.2",
    "1.21.8" to "15.0.2",
    "1.21.9" to "16.0.1",
    "1.21.10" to "16.0.1",
    "1.21.11" to "17.0.1-beta.1",
    "26.1" to "18.0.0",
    "26.1.1" to "18.0.0",
    "26.1.2" to "18.0.0",
    "26.2" to "20.0.1"
)
// Minecraft doesn't bundle io.netty:netty-handler-proxy (confirmed via each version's
// piston-meta manifest), so ConnectionProxyMixin needs it added and jar-embedded explicitly.
// Pinned to the exact netty-common version each Minecraft release bundles, to avoid a
// classpath version mismatch with the game's own Netty at runtime.
extra["nettyVersions"] = mapOf(
    "1.21" to "4.1.97.Final",
    "1.21.1" to "4.1.97.Final",
    "1.21.2" to "4.1.97.Final",
    "1.21.3" to "4.1.97.Final",
    "1.21.4" to "4.1.115.Final",
    "1.21.5" to "4.1.118.Final",
    "1.21.6" to "4.1.118.Final",
    "1.21.7" to "4.1.118.Final",
    "1.21.8" to "4.1.118.Final",
    "1.21.9" to "4.1.118.Final",
    "1.21.10" to "4.1.118.Final",
    "1.21.11" to "4.2.7.Final",
    "26.1" to "4.2.7.Final",
    "26.1.1" to "4.2.7.Final",
    "26.1.2" to "4.2.7.Final",
    "26.2" to "4.2.15.Final"
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
