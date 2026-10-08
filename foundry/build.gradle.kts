plugins {
    application
}

// The AI foundry: turns `rose analyze` findings into small tasks, runs an agent (the `pi` coding agent with the
// local model, or a script) in an isolated git worktree per task, and lets a fix land only when the gates pass.
dependencies {
    implementation(project(":rosetta"))
    implementation(project(":translate"))
    implementation(libs.gson)
    implementation(libs.vineflower)
    implementation(libs.asm)
}

application {
    mainClass.set("rose.foundry.FoundryMain")
}

tasks.named<JavaExec>("run") {
    workingDir = rootProject.projectDir
    maxHeapSize = "4G"
    standardInput = System.`in`
    isIgnoreExitValue = true
}
