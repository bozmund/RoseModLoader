plugins {
    application
}

// The `rose` command: launches the game, talks to the Agent Bridge (`rose ctl`), and serves MCP (`rose mcp`).
// Runs outside the game, so it has no Minecraft dependency.
dependencies {
    implementation(libs.gson)
}

application {
    applicationName = "rose"
    mainClass.set("rose.cli.RoseCli")
}

// The CLI runs on whatever Java the user's JAVA_HOME points to, so don't require the game's Java 25.
tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}
