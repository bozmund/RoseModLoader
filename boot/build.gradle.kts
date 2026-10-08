dependencies {
    api(project(":loader"))
    api(libs.gson)
}

// Mojang's launcher starts the game with these JVM flags; Rose needs them too.
val gameJvmArgs = listOf(
    "--enable-native-access=ALL-UNNAMED",
    "--add-exports", "java.base/jdk.internal.misc=ALL-UNNAMED",
    "-XX:StackShadowPages=32",
    "-Xmx4G",
)

fun JavaExec.roseLaunch(side: String) {
    group = "rose"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("rose.boot.RoseLaunch")
    workingDir = rootProject.projectDir
    jvmArgs(gameJvmArgs)
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
