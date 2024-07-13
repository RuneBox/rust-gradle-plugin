package org.runebox.gradle.plugin.rust.util

import java.io.OutputStream

internal object NullOutputStream : OutputStream() {
    override fun write(value: Int) = Unit
}