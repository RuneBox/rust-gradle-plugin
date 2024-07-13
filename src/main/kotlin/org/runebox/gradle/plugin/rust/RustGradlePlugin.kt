@file:Suppress("DEPRECATION")

package org.runebox.gradle.plugin.rust

import org.runebox.gradle.plugin.rust.util.Abi
import org.runebox.gradle.plugin.rust.util.SemanticVersion
import org.runebox.gradle.plugin.rust.util.rustPluginExtension
import org.runebox.gradle.plugin.rust.util.rustGradlePlugin
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@Suppress("unused")
class RustGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("rust", GradleException::class.java)
        val rustPlugin = project.rustGradlePlugin
        val rustPluginExtension = project.rustPluginExtension
        val tasksByBuildType = HashMap<String, ArrayList<TaskProvider<RustBuildTask>>>()

        val allRustAbiSet = mutableSetOf<Abi>()
        val extensionBuildDirectory = File(project.buildDir, "intermediates/rust")

        for (buildType in tasksByBuildType) {
            val buildTypeNameCap = buildType.key.capitalize(Locale.getDefault())

            val variantBuildDirectory = File(extensionBuildDirectory, buildType.key)
            val variantJniLibsDirectory = File(variantBuildDirectory, "jniLibs")

            val cleanTaskName = "clean${buildTypeNameCap}RustJniLibs"
            val cleanTask = project.tasks.register(cleanTaskName, RustCleanTask::class.java) {
                this.variantJniLibsDirectory.set(variantJniLibsDirectory)
            }

            for ((moduleName, module) in (extension as RustPluginExtension).modules) {
                val moduleNameCap = moduleName.capitalize(Locale.getDefault())
                val moduleBuildDirectory = File(variantBuildDirectory, "lib_$moduleName")

                val rustBuildType = module.buildTypes[buildType.key]
                val rustConfiguration = mergeRustConfigurations(rustBuildType, module, extension)

                val testTask = when (rustConfiguration.runTests) {
                    true -> {
                        val testTaskName = "test${moduleNameCap}Rust"
                        project.tasks.register(testTaskName, RustTestTask::class.java) {
                            this.rustProjectDirectory.set(module.path)
                            this.cargoTargetDirectory.set(moduleBuildDirectory)
                        }.get().dependsOn(cleanTask)
                    }
                    else -> null
                }

                val rustAbiSet = resolveAbiList(project, rustConfiguration.targets)
                allRustAbiSet.addAll(rustAbiSet)

                for (rustAbi in rustAbiSet) {
                    val buildTaskName = "build${buildTypeNameCap}${moduleNameCap}Rust[${rustAbi.androidName}]"
                    val buildTask = project.tasks.register(buildTaskName, RustBuildTask::class.java) {
                        this.abi.set(rustAbi)
                        this.rustProfile.set(rustConfiguration.profile)
                        this.rustProjectDirectory.set(module.path)
                        this.cargoTargetDirectory.set(moduleBuildDirectory)
                        this.variantJniLibsDirectory.set(variantJniLibsDirectory)
                    }
                    buildTask.get().dependsOn(testTask ?: cleanTask)
                    tasksByBuildType.getOrPut(buildType.key, ::ArrayList).add(buildTask)
                }
            }


        }

        val minimumSupportedRustVersion = SemanticVersion(rustPluginExtension.minimumSupportedRustVersion)
        installRustComponentsIfNeeded(project, minimumSupportedRustVersion, allRustAbiSet)

        project.subprojects.forEach { variant ->
            val tasks = tasksByBuildType[variant.name] ?: return@forEach
            val variantName = variant.name.capitalize(Locale.getDefault())

            project.afterEvaluate {
                val parentTask = project.tasks.getByName("pre${variantName}Build")
                for (task in tasks) {
                    parentTask.dependsOn(task)
                }
            }
        }
    }

    private fun resolveAbiList(project: Project, requested: Collection<String>): Collection<Abi> {
        val requestedAbi = Abi.fromRustNames(requested)

        val injectedAbi = Abi.fromInjectedBuildAbi(project)
        if (injectedAbi.isEmpty()) {
            return requestedAbi
        }

        val intersectionAbi = requestedAbi.intersect(injectedAbi)
        check(intersectionAbi.isNotEmpty()) {
            "ABIs requested by IDE ($injectedAbi) are not supported by the build config ($requested)"
        }

        return when {
            intersectionAbi.contains(Abi.Arm64) -> listOf(Abi.Arm64)
            intersectionAbi.contains(Abi.X86_64) -> listOf(Abi.X86_64)
            else -> listOf(intersectionAbi.first())
        }
    }

    private fun mergeRustConfigurations(vararg configurations: AbstractRustConfig?): AbstractRustConfig {
        val defaultConfiguration = AbstractRustConfig().also {
            it.profile = "release"
            it.targets = Abi.values().mapTo(ArrayList(), Abi::rustName)
            it.runTests = null
        }

        return configurations.asSequence()
            .filterNotNull()
            .plus(defaultConfiguration)
            .reduce { result, base ->
                if (result.profile.isEmpty()) {
                    result.profile = base.profile
                }
                if (result.targets.isEmpty()) {
                    result.targets = base.targets
                }
                if (result.runTests == null) {
                    result.runTests = base.runTests
                }
                result
            }
    }
}