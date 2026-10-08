// Rose's public API for mods. Compiles against Minecraft 26.3; at runtime it ships inside the "rose" core mod.
dependencies {
    compileOnly(rootProject.extra["minecraft"] as FileCollection)
    testImplementation(libs.gson) // the parts tested without Minecraft (events, config) only need Gson
}
