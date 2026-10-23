plugins {
    java
}

group = "rege.rege.hmclmods"
version = findProperty("mod_version") ?: "unknown"

val javafxVersion = property("javafx_version") as String

repositories {
    maven("https://maven.fabricmc.net/") {
        name = "FabricMC"
    }
    mavenCentral()
}

base {
    archivesName.set("hmcl-mod-menu")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    withSourcesJar()
}

repositories {
    mavenCentral()
    ivy("https://hmcl.glavo.site/download/") {
        name = "HMCL-Glavo"
        patternLayout {
            artifact("[artifact]-[revision].[ext]")
        }
        metadataSources {
            artifact()
        }
    }
}

dependencies {
    compileOnly("org.glavo.hmcl:HMCL:${property("hmcl_version")}@jar")
    compileOnly(
        "net.fabricmc:fabric-loader:${property("fabric_loader_version")}"
    )
    compileOnly("net.fabricmc:sponge-mixin:${property("mixin_version")}")
    compileOnly("io.github.llamalad7:mixinextras-fabric:${
        property("mixinextras_version")
    }")
    val osName = System.getProperty("os.name").lowercase()
    val fxClassifier = when {
        "win" in osName -> "win"
        "mac" in osName -> "mac"
        else -> "linux"
    }
    compileOnly("org.openjfx:javafx-controls:$javafxVersion:$fxClassifier")
    compileOnly("org.openjfx:javafx-graphics:$javafxVersion:$fxClassifier")
    compileOnly("org.openjfx:javafx-base:$javafxVersion:$fxClassifier")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(17)
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}
