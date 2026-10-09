// The Forge 1.20.1 dialect: net.minecraftforge.* reimplemented on Rose and Minecraft 26.3, so translated Forge
// 1.20.1 mods find the API they were written against. Ships as the built-in mod "rose_forge_1_20_1".
import java.util.concurrent.Callable

val accessWidener = file("src/main/resources/rose_forge_1_20_1.accesswidener")
val minecraft = rootProject.extra["minecraft"] as FileCollection
val widenerTool by configurations.creating

// Compile against the game with this mod's access widener applied, as it is at runtime.
val widenMinecraft by tasks.registering(JavaExec::class) {
    val gameJar = files(Callable { minecraft.files.filter { it.name == "client.jar" } })
    val out = layout.buildDirectory.file("widened/client-widened.jar")
    dependsOn(":boot:installMinecraft")
    inputs.files(gameJar, accessWidener)
    outputs.file(out)
    classpath = widenerTool
    mainClass.set("rose.loader.AccessWidenerMain")
    argumentProviders.add(CommandLineArgumentProvider {
        listOf(gameJar.singleFile.absolutePath, out.get().asFile.absolutePath, accessWidener.absolutePath)
    })
}

dependencies {
    widenerTool(project(":loader"))
    compileOnly(files(widenMinecraft.map { it.outputs.files }))
    compileOnly(files(Callable { minecraft.files.filter { it.name != "client.jar" } }).builtBy(":boot:installMinecraft"))
    compileOnly(libs.mixin)
    compileOnly(libs.bundles.asm) // provided by the launcher's class loader
    compileOnly(project(":loader")) // shared from the parent class loader at runtime
    compileOnly(project(":api"))    // ships inside the "rose" core mod
}
