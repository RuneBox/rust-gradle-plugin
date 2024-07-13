<!DOCTYPE html>
<html>
<head>
    <style>
       body { font-family: Arial, serif; margin: 0; padding: 0; font-size: 1.1em; color: #333; }
       h1, h2, h3, h4, h5, h6 { color: #1976D2; cursor: pointer; }
       .highlight { color: #FF3D00; }
       a { color: #1976D2; text-decoration: none; }
       a:hover { text-decoration: underline; }
       p, ul { margin: 0.8em 0; }
       #header-banner { color: white; padding: 20px 0; text-align: center; }
    </style>
</head>
<body>
    <header id="header-banner">
        <h1>Gradle-Rust-Plugin</h1>
        <p>A plugin for Gradle that provides RustUp, Rustc, and Cargo tasks</p>
    </header>
    <div id="content">
        <h2>About</h2>
        <p>This project comprises a powerful and flexible Gradle plugin tailored for Rust development. The plugin integrates key Rust tools - RustUp, Rustc, and Cargo - into the Gradle framework, enabling streamlined and coordinated build processes.</p>
<h5>Add RuneBox Maven's repository to your build.gradle.kts file.</h5>

    pluginManagement {
        repositories {
            maven(url = "https://maven.runebox.org/")
            gradlePluginPortal()
        }
    }
    
    root.projectName = "my-rootproj"

<h3>Add the gradle-plugin and configuration to your build.gradle.kts</h3>
    
    plugins {
        id("io.runebox.gradle.rust") version "0.1.0"
    }

    dependencies {
        ...
    }

    /*
     * The Rust-Gradle-Plugin Config
     */

    rust {
        module("my-library") {
            path = file("src/my-library")
            profile = "release
            targets = listOf("x86_64", "x86", "arm", "arm_64")
            
            buildType("debug") {
                profile = "dev"
            }

            buildType("release") {
                profile = "release"
                runTests = true
                targets = listOf("arm", "arm64", "x86", "x86_64")
            }
        }

        module("another-rust-module") {
            ...
        }
    }

</div>
</body>
</html>