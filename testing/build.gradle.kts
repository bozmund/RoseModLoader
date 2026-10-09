// Oracle comparison: Rose running old mods vs. a Fabric 26.3 server running their native ports (the answer key).
//   ./gradlew oracle                       dump both sides and compare -> build/oracle/farmersdelight.md / .json
//   ./gradlew oracle -Prose.oracle.strict  also fail when a difference isn't allow-listed (testing/oracle/*.allow.tsv)
dependencies {
    implementation(project(":boot")) // Downloader
    implementation(libs.gson)
}

val oracleDir = rootProject.layout.buildDirectory.dir("oracle").get().asFile
val namespaces = providers.gradleProperty("rose.oracle.namespaces").orElse("farmersdelight")
val fabricLoader = libs.versions.fabric.loader.get()
val fabricInstaller = "1.1.2"

// Fabric 26.3 + Fabric API + the native ports, from corpus/ (./gradlew corpusInputs, see rosetta/sources.json).
val referenceMods = listOf(
    "corpus/reference/fabric-26.3/fabric-api-0.162.0+26.3.jar",
    "corpus/reference/fabric-26.3/fabric-gametest-api-v1-4.0.33+3434d6d95d.jar",
    "corpus/reference/fabric-26.3/FarmersDelight-26.3-3.6.27+refabricated.jar",
)

val oracleReference = tasks.register<JavaExec>("oracleReference") {
    group = "rose"
    description = "Dump the native ports on a headless Fabric 26.3 server. Output: build/oracle/reference/"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("rose.testing.oracle.FabricReference")
    workingDir = rootProject.projectDir
    val dumperJar = project(":testmods:oracle").tasks.named<Jar>("fabricJar")
    inputs.files(dumperJar)
    outputs.upToDateWhen { false }
    argumentProviders.add(CommandLineArgumentProvider {
        listOf(rootProject.file("run/oracle-fabric").absolutePath, oracleDir.resolve("reference").absolutePath, namespaces.get(),
            "https://meta.fabricmc.net/v2/versions/loader/26.3/$fabricLoader/$fabricInstaller/server/jar") +
            referenceMods.map { rootProject.file(it).absolutePath } +
            dumperJar.get().archiveFile.get().asFile.absolutePath
    })
}

tasks.register<JavaExec>("oracle") {
    group = "rose"
    description = "Compare Rose's dump with the Fabric reference's. Report: build/oracle/<namespaces>.md and .json"
    dependsOn(":boot:dumpOracle", oracleReference)
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("rose.testing.oracle.OracleMain")
    workingDir = rootProject.projectDir
    outputs.upToDateWhen { false }
    val strict = providers.gradleProperty("rose.oracle.strict").isPresent
    argumentProviders.add(CommandLineArgumentProvider {
        val name = namespaces.get().replace(',', '+')
        val allow = rootProject.file("testing/oracle/$name.allow.tsv")
        listOf(oracleDir.resolve("rose").absolutePath, oracleDir.resolve("reference").absolutePath,
            if (allow.exists()) allow.absolutePath else "-", oracleDir.resolve(name).absolutePath) +
            (if (strict) listOf("--strict") else emptyList())
    })
}
