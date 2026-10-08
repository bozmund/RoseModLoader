// The Rose Agent Bridge: a built-in mod ("rose_bridge") that lets AI agents and scripts observe, control and
// test the running game over a token-protected JSON-RPC endpoint on localhost. Off unless -Drose.bridge=true.
dependencies {
    compileOnly(rootProject.extra["minecraft"] as FileCollection)
    compileOnly(libs.mixin)
    compileOnly(project(":loader"))
    compileOnly(project(":api"))
}

dependencies {
    testImplementation(libs.gson) // EventLog and friends are testable without Minecraft
}
