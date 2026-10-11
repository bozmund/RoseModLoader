// GameTests for the M6 mods (Forge 1.20.1 mods other than Farmer's Delight). The mods are not part of the repo: put
// their jars in run/gametest/mods/; a test whose mod is missing passes as skipped. Mod code is reached by registry
// ids and reflection.
dependencies {
    compileOnly(rootProject.extra["minecraft"] as FileCollection)
    compileOnly(project(":api"))
    compileOnly(project(":loader"))
    compileOnly(libs.asm) // provided by the launcher's class loader
}
