// Oracle dumper: writes registries, block properties, item components, recipes, loot tables and tags as JSON.
// One source set, two jars: `jar` loads on Rose, `fabricJar` on the Fabric 26.3 reference server (answer key).
dependencies {
    compileOnly(rootProject.extra["minecraft"] as FileCollection)
    compileOnly(project(":api"))
    compileOnly(libs.fabric.loader) { isTransitive = false }
    compileOnly(libs.fabric.api.base) { isTransitive = false }
    compileOnly(libs.fabric.lifecycle.events) { isTransitive = false }
}

tasks.named<Jar>("jar") {
    exclude("rose/testmods/oracle/FabricOracle*")
}

val fabricJar = tasks.register<Jar>("fabricJar") {
    group = "rose"
    description = "The oracle dumper as a Fabric 26.3 mod, for the reference server."
    archiveClassifier.set("fabric")
    from(sourceSets["main"].output) {
        // data/ holds Rose's GameTest instance; on Fabric its test function doesn't exist.
        exclude("rose/testmods/oracle/RoseOracle*", "rose.mod.json", "data/**")
    }
    from("src/fabric/resources")
}

tasks.named("assemble") { dependsOn(fabricJar) }
