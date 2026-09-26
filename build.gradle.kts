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
}

