package org.runebox.gradle.plugin.rust.util

import org.gradle.internal.os.OperatingSystem

internal enum class OperatingSystem {
    Linux,
    MacOs,
    Windows,
    Unknown;

    companion object {
        val current: org.runebox.gradle.plugin.rust.util.OperatingSystem

        init {
            val os = OperatingSystem.current()
            current = when {
                os.isLinux -> Linux
                os.isMacOsX -> MacOs
                os.isWindows -> Windows
                else -> Unknown
            }
        }
    }

    val isLinux: Boolean
        get() {
            return this == Linux
        }

    val isMacOs: Boolean
        get() {
            return this == MacOs
        }

    val isWindows: Boolean
        get() {
            return this == Windows
        }
}