dependencies {
    api(project(":loader"))
    api(project(":mixin-service"))
    api(libs.gson)
}

// Mojang's launcher starts the game with these JVM flags; Rose needs them too.
val gameJvmArgs = listOf(
    "--enable-native-access=ALL-UNNAMED",
    "--add-exports", "java.base/jdk.internal.misc=ALL-UNNAMED",
    "-XX:StackShadowPages=32",
    "-Xmx4G",
)

// Development test mods loaded on every runClient/runServer (see testmods/).
val devMods = listOf(":testmods:hello")

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
    jvmArgumentProviders.add(CommandLineArgumentProvider {
        listOf("-Drose.dev.mods=" + modJars.files.joinToString(File.pathSeparator) { it.absolutePath })
    })
    args("--side", side, "--runDir", rootProject.file("run").absolutePath)
    standardInput = System.`in`
}

tasks.register<JavaExec>("runClient") {
    description = "Launch the Minecraft 26.3 client through Rose (development, offline)."
    roseLaunch("client")
}

tasks.register<JavaExec>("runServer") {
    description = "Launch the Minecraft 26.3 dedicated server through Rose. Accept Mojang's EULA in run/server/eula.txt first."
    roseLaunch("server")
}
