plugins {
    // Picks the correct Loom variant for the active Minecraft version automatically.
    id("dev.kikugie.loom-back-compat")
}

val modVersion = property("mod.version") as String
val modId = property("mod.id") as String

version = "$modVersion+${sc.current.version}"
base.archivesName = modId

val requiredJava: JavaVersion = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    else -> JavaVersion.VERSION_21
}

@Suppress("UNCHECKED_CAST")
fun depFor(mapName: String): String =
    (rootProject.extra[mapName] as Map<String, String>)[sc.current.version]
        ?: error("No '$mapName' entry for Minecraft ${sc.current.version}")

repositories {
    mavenCentral()
    // maven.terraformersmc.com's CDN deterministically truncates responses to Gradle's Apache
    // HttpClient (confirmed on both the POM and the jar itself, with or without keepAlive,
    // with or without metadataSources{artifact()}) - curl against the same URLs is unaffected.
    // Modrinth's maven proxy mirrors the same jars without this incompatibility, but has its
    // own occasional outages; rootProject/libs is a local fallback (flatDir ignores groupId,
    // matches by "<artifact>-<version>.jar") - see readme.md Development section.
    flatDir { dirs(rootProject.file("libs")) }
    exclusiveContent {
        forRepository { maven("https://api.modrinth.com/maven") { name = "Modrinth" } }
        filter { includeGroup("maven.modrinth") }
    }
    exclusiveContent {
        forRepository { maven("https://maven.shedaniel.me/") { name = "shedaniel" } }
        filter { includeGroup("me.shedaniel.cloth") }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    loomx.applyMojangMappings()

    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${depFor("fabricApiVersions")}")

    modImplementation("maven.modrinth:modmenu:${depFor("modMenuVersions")}")
    modImplementation("me.shedaniel.cloth:cloth-config-fabric:${depFor("clothConfigVersions")}") {
        exclude(group = "net.fabricmc.fabric-api")
    }

    // Minecraft doesn't bundle this Netty module; jar-in-jar it at the exact version this
    // release ships, so ConnectionProxyMixin's Socks5ProxyHandler/HttpProxyHandler resolve.
    include(implementation("io.netty:netty-handler-proxy:${depFor("nettyVersions")}")!!)
}

loom {
    runConfigs.all {
        preferGradleTask = true
        generateRunConfig = true
        runDirectory = rootProject.file("run")
        programArgs("--username=Dev")
    }
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava
    toolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}

val resourceProps = mapOf(
    "id" to modId,
    "name" to property("mod.name"),
    "version" to modVersion,
    "description" to property("mod.description"),
    "minecraft" to sc.current.version
)

tasks.processResources {
    inputs.properties(resourceProps)
    filesMatching("fabric.mod.json") { expand(resourceProps) }
}

tasks.register<Copy>("buildAndCollect") {
    group = "build"
    from(loomx.modJar.flatMap { it.archiveFile }, loomx.modSourcesJar.flatMap { it.archiveFile })
    into(rootProject.layout.buildDirectory.dir("libs/$modVersion"))
    dependsOn("build")
}
