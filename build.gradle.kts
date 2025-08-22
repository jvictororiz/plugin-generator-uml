plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.5.0"
}

group = "br.roriz.generator"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        // Base IDE = IntelliJ Community (IC) 2025.1
        create("IC", "2025.1")

        // Framework de testes do plugin
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)

        // 🔹 Plugins necessários para PSI e UAST
        bundledPlugin("com.intellij.java")      // Suporte a Java (PSI de Java)
        bundledPlugin("org.jetbrains.kotlin")   // Suporte a Kotlin
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "251"
            untilBuild = "" // compatível com futuras builds
        }

        changeNotes = """
            Initial version with Generate Diagram
        """.trimIndent()
    }
}

tasks {
    // JVM target
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}
