// Rosetta: knowledge of how names and APIs correspond between Minecraft versions.
// The name layers it builds come from Mojang/Forge/Fabric mapping files in corpus/ and stay local;
// hand-written rules (rosetta/rules/) are Rose's own and are committed.
dependencies {
    implementation(libs.mapping.io)
    implementation(libs.gson)
    implementation(libs.asm)
}

tasks.register<JavaExec>("buildNameLayers") {
    group = "rose"
    description = "Build Rosetta's name layers (corpus/rosetta/) from the mapping files in corpus/. Run corpusInputs first."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("rose.rosetta.RosettaMain")
    workingDir = rootProject.projectDir
    maxHeapSize = "4G"
    // Up to date unless the mappings, the 26.3 jar, the rules or Rosetta's code changed.
    val corpus = rootProject.file("corpus")
    inputs.files(fileTree(corpus.resolve("mappings")), corpus.resolve("minecraft/1.20.1/client-mappings.txt"),
        corpus.resolve("minecraft/1.21.11/client-mappings.txt"), corpus.resolve("minecraft/26.3/client.jar"))
        .withPropertyName("mappingInputs").optional()
    inputs.dir(rootProject.file("rosetta/rules")).withPropertyName("rules")
    outputs.file(corpus.resolve("rosetta/names-forge-1.20.1.tsv"))
}
