plugins {
    java
}

group = "me.abdullahrasheed"
version = "0.1.0-SNAPSHOT"

repositories {
    maven(url = "https://repo.papermc.io/repository/maven-public/") {
        name = "papermc"
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

val generateTexturePackAssets = tasks.register<Exec>("generateTexturePackAssets") {
    val javaLauncher = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(25)
    }

    inputs.file("texturepack-assets/GenerateTexturePackAssets.java")
    outputs.files(
        "mineframe-texturepack/assets/minecraft/textures/gui/sprites/boss_bar/notched_20_background.png",
        "mineframe-texturepack/assets/minecraft/textures/gui/sprites/boss_bar/white_background.png"
    )
    commandLine(
        javaLauncher.get().executablePath.asFile.absolutePath,
        "-Djava.awt.headless=true",
        "texturepack-assets/GenerateTexturePackAssets.java"
    )
}

val texturePackZip = tasks.register<Zip>("texturePackZip") {
    dependsOn(generateTexturePackAssets)
    from("mineframe-texturepack") {
        exclude(".DS_Store")
    }
    archiveFileName = "mineframe-texturepack.zip"
    destinationDirectory = layout.buildDirectory.dir("distributions")
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release = 25
    }

    processResources {
        val properties = mapOf("version" to project.version)
        inputs.properties(properties)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(properties)
        }
    }

    jar {
        archiveBaseName = "Mineframe"
    }

    build {
        dependsOn(texturePackZip)
    }
}
