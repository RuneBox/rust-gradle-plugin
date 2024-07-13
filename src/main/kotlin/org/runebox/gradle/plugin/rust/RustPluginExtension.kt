package org.runebox.gradle.plugin.rust

import java.io.File

@DslMarker
annotation class RustGradlePluginDslMarker

@RustGradlePluginDslMarker
open class AbstractRustConfig {
    var profile: String = ""
    var targets = listOf<String>()
    var runTests: Boolean? = null
}

@RustGradlePluginDslMarker
open class RustPluginExtension : AbstractRustConfig() {
    var minimumSupportedRustVersion = ""
    var modules = mutableMapOf<String, RustGradleModule>()

    fun module(name: String, configure: RustGradleModule.() -> Unit) {
        modules.getOrPut(name, ::RustGradleModule).configure()
    }
}

@RustGradlePluginDslMarker
open class RustGradleModule : AbstractRustConfig() {
    lateinit var path: File

    var buildTypes = hashMapOf(
        "debug" to RustGradleBuildType().also { it.profile = "dev" },
        "release" to RustGradleBuildType().also { it.profile = "release" }
    )

    fun buildType(name: String, configure: RustGradleBuildType.() -> Unit) {
        buildTypes.getOrPut(name, ::RustGradleBuildType).configure()
    }
}

@RustGradlePluginDslMarker
class RustGradleBuildType : AbstractRustConfig()