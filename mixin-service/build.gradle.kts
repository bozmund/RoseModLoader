dependencies {
    api(project(":loader"))
    api(libs.mixin)
    api(libs.mixinextras)
    api(libs.bundles.asm)
    // Mixin uses Guava and Gson but doesn't declare them.
    implementation(libs.guava)
    implementation(libs.gson)
}
