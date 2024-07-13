package org.runebox.gradle.plugin.rust.util

import org.runebox.gradle.plugin.rust.RustPluginExtension
import org.runebox.gradle.plugin.rust.RustGradlePlugin
import org.gradle.api.GradleException
import org.gradle.api.Project

internal fun Project.findRustGradlePlugin() = plugins.asSequence()
    .mapNotNull { it as? RustGradlePlugin }
    .firstOrNull()

val Project.rustGradlePlugin get() = checkNotNull(findRustGradlePlugin()) {
    "Could not find the rust-gradle-plugin plugin."
}

val Project.rustPluginExtension: RustPluginExtension
    get() = extensions.findByType(RustPluginExtension::class.java)
    ?: throw GradleException("Could not find the 'rust-gradle-plugin' extension.")

