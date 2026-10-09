// GameTests that check Farmer's Delight (Forge 1.20.1) behaves on Rose. FD itself is not part of the repo: put its jar
// in run/gametest/mods/; without it these tests pass as skipped. FD code is reached by registry ids and reflection.
dependencies {
    compileOnly(rootProject.extra["minecraft"] as FileCollection)
    compileOnly(project(":api"))
    compileOnly(project(":loader"))
}
