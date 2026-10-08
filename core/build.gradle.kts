// The built-in "rose" mod: the API implementation and Rose's Mixin hooks into Minecraft 26.3.
// Its jar also contains the API classes, so the whole of Rose's game-side code loads as one mod.
dependencies {
    compileOnly(rootProject.extra["minecraft"] as FileCollection)
    compileOnly(libs.mixin)
    compileOnly(project(":loader")) // shared from the parent class loader at runtime
    implementation(project(":api"))
}

tasks.named<Jar>("jar") {
    val api = project(":api").sourceSets["main"].output
    from(api)
    dependsOn(project(":api").tasks.named("classes"))
}
