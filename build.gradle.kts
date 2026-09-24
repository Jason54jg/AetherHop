plugins {
    // Picks the correct Loom variant for the active Minecraft version automatically.
    id("dev.kikugie.loom-back-compat")
}

val modVersion = property("mod.version") as String
val modId = property("mod.id") as String

// Jar name carries the whole Minecraft range it supports, e.g. aetherhop-1.0.0+mc1.21-1.21.10.jar
val mcLabel = mapOf(
    "1.21.10" to "1.21-1.21.10",
    "1.21.11" to "1.21.11",
    "26.1.2" to "26.1-26.1.2",
    "26.3" to "26.2-26.3"
)[sc.current.version] ?: error("No mcLabel for Minecraft ${sc.current.version}")

version = "$modVersion+mc$mcLabel"
base.archivesName = modId

val requiredJava: JavaVersion = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    else -> JavaVersion.VERSION_21
}

@Suppress("UNCHECKED_CAST")
fun depFor(mapName: String): String =
    (rootProject.extra[mapName] as Map<String, String>)[sc.current.version]
        ?: error("No '$mapName' entry for Minecraft ${sc.current.version}")

@Suppress("UNCHECKED_CAST")
fun nettyModules(): List<String> =
    (rootProject.extra["nettyModules"] as Map<String, List<String>>)[sc.current.version]
        ?: error("No 'nettyModules' entry for Minecraft ${sc.current.version}")

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

    // Minecraft doesn't bundle these Netty modules (see nettyModules in stonecutter.gradle.kts),
    // so jar-in-jar them: ConnectionProxyMixin's Socks5ProxyHandler/HttpProxyHandler need them.
    val nettyVersion = depFor("nettyVersions")
    for (module in nettyModules()) {
        include(implementation("io.netty:$module:$nettyVersion")!!)
    }
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
    // Version range the jar declares in fabric.mod.json, e.g. ">=1.21 <=1.21.10"
    "minecraft" to depFor("mcCompat"),
    "java" to requiredJava.majorVersion
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
