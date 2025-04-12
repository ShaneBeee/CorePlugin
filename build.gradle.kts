import io.papermc.paperweight.userdev.ReobfArtifactConfiguration

plugins {
    id("java")
    id("io.github.goooler.shadow") version "8.1.7"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.16" // the latest version can be found on the Gradle Plugin Portal
}

// The Minecraft version we're currently building for
val minecraftVersion = "1.21.4"
// Where this builds on the server
val serverLocation = "Skript/1-21-5"
// Version of CorePlugin
val projectVersion = "1.0.0"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

paperweight.reobfArtifactConfiguration = ReobfArtifactConfiguration.MOJANG_PRODUCTION

repositories {
    mavenCentral()
    mavenLocal()

    // Command Api
    //maven("https://s01.oss.sonatype.org/content/repositories") // Regular
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots") // Snapshots

    // NBT-API repo
    maven(("https://repo.codemc.io/repository/maven-public/"))

    // JitPack repo
    maven("https://jitpack.io")
}

dependencies {
    // Paper
    //paperweight.paperDevBundle("${minecraftVersion}-R0.1-SNAPSHOT")
    paperweight.paperDevBundle("1.21.5-no-moonrise-SNAPSHOT")

    // FastBoard
    implementation("fr.mrmicky:fastboard:2.1.4")

    // Command Api
    implementation("dev.jorel:commandapi-bukkit-shade-mojang-mapped:10.0.1-SNAPSHOT")

    // NBT-API
    implementation("de.tr7zw:item-nbt-api:2.14.2-SNAPSHOT")

    // CoreAPI
    implementation("com.github.shanebeestudios:coreapi:master-SNAPSHOT")
}

tasks {
    register("server", Copy::class) {
        dependsOn("shadowJar")
        from("build/libs") {
            include("CorePlugin-*.jar")
            destinationDir = file("/Users/ShaneBee/Desktop/Server/${serverLocation}/plugins/")
        }

    }
    processResources {
        expand("version" to projectVersion)
    }
    compileJava {
        options.release = 21
        options.compilerArgs.add("-Xlint:unchecked")
        options.compilerArgs.add("-Xlint:deprecation")
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name()
        exclude("com/shanebeestudios/core/plugin")
        (options as StandardJavadocDocletOptions).links(
            "https://jd.papermc.io/paper/1.21.1/",
            "https://jd.advntr.dev/api/4.17.0/",
            "https://tr7zw.github.io/Item-NBT-API/v2-api/"
        )

    }
    shadowJar {
        relocate("fr.mrmicky.fastboard", "com.shanebeestudios.core.api.fastboard")
        relocate("dev.jorel.commandapi", "com.shanebeestudios.core.api.commandapi")
        relocate("de.tr7zw.changeme.nbtapi", "com.shanebeestudios.core.api.nbt")
        archiveFileName = "CorePlugin-${projectVersion}.jar"
    }
    jar {
        dependsOn(shadowJar)
        archiveFileName.set("CorePlugin.jar")
    }
}
