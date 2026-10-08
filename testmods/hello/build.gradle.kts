// A tiny Rose-native mod used to check that mod loading and Mixin work end to end.
// Its mixins name their targets as strings, so it compiles without Minecraft on the classpath.
dependencies {
    compileOnly(libs.mixin)
}
