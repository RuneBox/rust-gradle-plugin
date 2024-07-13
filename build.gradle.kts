@file:Suppress("UnstableApiUsage")

plugins {
    kotlin("jvm") version "1.9.22"
    id("com.gradle.plugin-publish") version "+"
    id("io.github.kotlin-artisans.changesets") version "0.0.4"
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
    signing
    idea
}

defaultTasks("build")

group = "org.runebox.gradle"
version = "1.0.0"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(gradleApi())
    implementation(gradleKotlinDsl())
}

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of("11")
        vendor = JvmVendorSpec.ADOPTOPENJDK
        implementation = JvmImplementation.VENDOR_SPECIFIC
    }
}

tasks.wrapper {
    gradleVersion = "8.7"
}

idea {
    project {
        jdkName = "11"
        languageLevel.level = "1.8"
    }
}

gradlePlugin {
    plugins {
        create("rustGradle") {
            id = "org.runebox.gradle.rust"
            version = version.toString()
            implementationClass = "org.runebox.gradle.plugin.rust.RustGradlePlugin"
            displayName = "Rust+Cargo Gradle Build Plugin"
            description = "Provides a build system for both gradle/jvm + gradle/rust projects which are embedded."
            tags.set(listOf("build", "reversing", "osrs", "runescape", "rust"))
            website.set("https://github.com/runebox/")
            vcsUrl.set("https://github.com/runebox/rust-gradle-plugin.git")
        }
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    group = "build"
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

publishing {
    repositories {
        mavenLocal()
        maven(url = "https://jitpack.io")
    }

    publications {
        create<MavenPublication>("rustMaven") {
            from(components["java"])
            groupId = "org.runebox.gradle"
            artifactId = "rust-plugin"
            version = rootProject.project.version.toString()
        }
    }
}

artifacts {
    add("implementation", sourcesJar)
}