plugins {
    application
}

dependencies {
    implementation(project(":boot")) // Mojang download helpers
    implementation(libs.gson)
    implementation(libs.mapping.io)
    implementation(libs.tiny.remapper)
    implementation(libs.vineflower)
}

application {
    mainClass.set("rose.corpus.CorpusMain")
}

// ./gradlew corpusSetup  -> prepares corpus/ for the versions Rose currently works with.
// Other versions: ./gradlew :corpus-tools:run --args="setup 1.19.2"
tasks.named<JavaExec>("run") {
    workingDir = rootProject.projectDir
    maxHeapSize = "6G"
}

tasks.register<JavaExec>("corpusSetup") {
    group = "rose"
    description = "Download, verify, remap and decompile the Minecraft versions Rose works with into corpus/."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("rose.corpus.CorpusMain")
    workingDir = rootProject.projectDir
    maxHeapSize = "6G"
    args("setup", "26.3", "1.20.1")
}

tasks.register<JavaExec>("corpusInputs") {
    group = "rose"
    description = "Download Rosetta's inputs (mapping files, game versions, pilot mods) listed in rosetta/sources.json."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("rose.corpus.CorpusMain")
    workingDir = rootProject.projectDir
    args("inputs")
}
