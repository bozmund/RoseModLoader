// The translation engine: rewrites old mod bytecode to Minecraft 26.3 names using Rosetta.
dependencies {
    api(project(":rosetta"))
    api(libs.bundles.asm)
    implementation(libs.gson) // Mixin refmaps
}
