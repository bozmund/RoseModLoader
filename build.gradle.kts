plugins {
    `java-library`
}

allprojects {
    group = "io.github.bozmund.rose"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }        // mapping-io, tiny-remapper, sponge-mixin fork
        maven("https://repo.spongepowered.org/repository/maven-public/") { name = "Sponge" }
        maven("https://libraries.minecraft.net/") { name = "Mojang" }   // DataFixerUpper, brigadier, authlib
        maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
    }
}

subprojects {
    // Container folders ("eras", "dialects") are not real projects.
    if (childProjects.isNotEmpty()) return@subprojects

    apply(plugin = "java-library")

    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(25)) // Minecraft 26.3 requires Java 25
        withSourcesJar()
    }

    dependencies {
        "testImplementation"(platform(rootProject.libs.junit.bom))
        "testImplementation"("org.junit.jupiter:junit-jupiter")
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(25)
    }
}
