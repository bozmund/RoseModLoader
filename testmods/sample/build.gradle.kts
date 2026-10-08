// Rose-native sample mod exercising the M2 API: block + block entity, item, recipe, payload, config, events, GameTests.
dependencies {
    compileOnly(rootProject.extra["minecraft"] as FileCollection)
    compileOnly(project(":api"))
}
