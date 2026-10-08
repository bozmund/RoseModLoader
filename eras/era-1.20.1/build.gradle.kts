// Era bridge for Minecraft 1.20.1 -> 26.3: static shims that redirected old calls land on.
// Shims compile against 26.3 and are loaded next to translated mods at runtime.
dependencies {
    compileOnly(rootProject.extra["minecraft"] as FileCollection)
}
