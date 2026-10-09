import java.io.ByteArrayInputStream

dependencies {
    api(project(":loader"))
    api(project(":mixin-service"))
    implementation(project(":translate")) // translates mods from other loaders/versions at launch
    api(libs.gson)
}

// Mojang's launcher starts the game with these JVM flags; Rose needs them too.
val gameJvmArgs = listOf(
    "--enable-native-access=ALL-UNNAMED",
    "--add-exports", "java.base/jdk.internal.misc=ALL-UNNAMED",
    "-XX:StackShadowPages=32",
    "-Xmx4G",
    "-Drose.bridge=true", // dev runs: Agent Bridge on (localhost + token only)
)

// Mods loaded on every runClient/runServer/runGameTests: Rose's own core mod plus the test mods (see testmods/).
val devMods = listOf(":core", ":agent-bridge", ":eras:era-1.20.1", ":dialects:forge-1.20.1", ":testmods:hello", ":testmods:sample")

fun JavaExec.roseLaunch(side: String) {
    group = "rose"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("rose.boot.RoseLaunch")
    // The game writes logs/ and crash-reports/ to its working directory, so keep it inside run/.
    val gameDir = rootProject.file("run/$side")
    workingDir = gameDir
    doFirst { gameDir.mkdirs() }
    jvmArgs(gameJvmArgs)
    val modJars = files(devMods.map { project(it).tasks.named("jar") })
    inputs.files(modJars)
    // Old mods are translated with Rosetta's name layer (built from corpus/) and rules (rosetta/rules/).
    val nameLayer = rootProject.file("corpus/rosetta/names-forge-1.20.1.tsv")
    if (rootProject.file("corpus/mappings").isDirectory) dependsOn(":rosetta:buildNameLayers")
    jvmArgumentProviders.add(CommandLineArgumentProvider {
        listOf("-Drose.dev.mods=" + modJars.files.joinToString(File.pathSeparator) { it.absolutePath },
            "-Drose.rosetta.home=" + rootProject.file("rosetta").absolutePath,
            "-Drose.rosetta.names=" + nameLayer.absolutePath)
    })
    args("--side", side, "--runDir", rootProject.file("run").absolutePath)
    standardInput = System.`in`
}

tasks.register<JavaExec>("installMinecraft") {
    group = "rose"
    description = "Download Minecraft 26.3 into run/ and write its compile classpath for modules that use game classes."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("rose.boot.InstallMain")
    @Suppress("UNCHECKED_CAST")
    val out = (rootProject.extra["minecraftClasspathFile"] as Provider<RegularFile>).get().asFile
    outputs.file(out)
    args(rootProject.file("run").absolutePath, "26.3", out.absolutePath)
}

tasks.register<JavaExec>("runClient") {
    description = "Launch the Minecraft 26.3 client through Rose (development, offline)."
    roseLaunch("client")
}

tasks.register<JavaExec>("runServer") {
    description = "Launch the Minecraft 26.3 dedicated server through Rose. Accept Mojang's EULA in run/server/eula.txt first."
    roseLaunch("server")
}

// Headless: runs every loaded mod's GameTests on vanilla's GameTest server and writes a JUnit-style XML report.
// Narrow the selection with -Prose.tests=sample:* (vanilla's namespaced wildcard selector).
tasks.register<JavaExec>("runGameTests") {
    description = "Run mods' GameTests headless through Rose (no EULA needed). Report: build/gametest/report.xml"
    roseLaunch("gametest")
    standardInput = ByteArrayInputStream(ByteArray(0))
    val report = rootProject.layout.buildDirectory.file("gametest/report.xml").get().asFile
    outputs.upToDateWhen { false }
    doFirst { report.parentFile.mkdirs() }
    val selection = providers.gradleProperty("rose.tests").orElse("sample:*")
    argumentProviders.add(CommandLineArgumentProvider {
        listOf("--", "--report", report.absolutePath, "--tests", selection.get())
    })
}
