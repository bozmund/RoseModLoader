plugins {
    application
}

// `rose analyze <mod.jar>`: static compatibility report for an old mod against Minecraft 26.3.
dependencies {
    implementation(project(":translate"))
    implementation(libs.gson)
}

application {
    mainClass.set("rose.analyzer.AnalyzerMain")
}

tasks.named<JavaExec>("run") {
    workingDir = rootProject.projectDir
    maxHeapSize = "4G"
    dependsOn(":boot:installMinecraft", ":rosetta:buildNameLayers", ":eras:era-1.20.1:jar")
    // Exit code 3 means "problems found", which is a result, not a build failure. `rose analyze` reads the report.
    isIgnoreExitValue = true
}
