package com.doist.gradle

import com.doist.gradle.tasks.ExecWithResultTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.StopExecutionException
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.create
import java.io.ByteArrayOutputStream
import java.io.File

@Suppress("unused")
class KtlintPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create<KtlintExtension>("ktlint")
        val configuration = target.configurations.create("ktlint")
        target.afterEvaluate { configure(extension, configuration) }
    }

    private fun Project.configure(extension: KtlintExtension, configuration: Configuration) {
        val version = requireNotNull(extension.version) {
            "Please specify ktlint.version for project: $name"
        }

        configuration.defaultDependencies {
            add(dependencies.create("com.pinterest:ktlint:$version"))
            add(dependencies.create("com.doist:ktlint-idea-reporter:1.0.0"))
        }

        tasks.createKtlintWithSrc(
            name = "ktlintCheck",
            description = "Checks code styles for all `kt` files in the project.",
            configuration = configuration,
            extension = extension,
            format = false
        )
        tasks.createKtlintWithSrc(
            name = "ktlintFormat",
            description = "Formats all `kt` files in the project",
            configuration = configuration,
            extension = extension,
            format = true
        )

        val uncommittedChanges = tasks.createGitDiff(
            name = "ktlintFindUncommittedChanges",
            description = "Finds all uncommitted changes in the project.",
            from = "HEAD"
        )
        tasks.createKtlint(
            name = "ktlintCheckUncommittedChanges",
            description = "Checks code styles for all changed `kt` files.",
            configuration = configuration,
            extension = extension,
            format = false,
            targetProvider = { uncommittedChanges.result.get().readLines() }
        ).dependsOn(uncommittedChanges)
        tasks.createKtlint(
            name = "ktlintFormatUncommittedChanges",
            description = "Formats code style for all changed `kt` files.",
            configuration = configuration,
            extension = extension,
            format = true,
            targetProvider = { uncommittedChanges.result.get().readLines() }
        ).dependsOn(uncommittedChanges)

        val printableBranchName = extension.mainBranch.capitalize()
        val changesFromMainBranch = tasks.createGitDiff(
            name = "ktlintFindChangesFrom$printableBranchName",
            description = "Finds all changes in current branch compared with remote main branch.",
            from = "origin/${extension.mainBranch}"
        )
        tasks.createKtlint(
            name = "ktlintCheckChangesFrom$printableBranchName",
            description = "Checks code style for all changed `kt` files against main branch.",
            configuration = configuration,
            extension = extension,
            format = false,
            targetProvider = { changesFromMainBranch.result.get().readLines() }
        ).dependsOn(changesFromMainBranch)
        tasks.createKtlint(
            name = "ktlintFormatChangesFrom$printableBranchName",
            description = "Formats code styles for all changed `kt` files against main branch.",
            configuration = configuration,
            extension = extension,
            format = true,
            targetProvider = { changesFromMainBranch.result.get().readLines() }
        ).dependsOn(changesFromMainBranch)
    }

    private inline fun TaskContainer.createKtlint(
        name: String,
        description: String,
        configuration: Configuration,
        extension: KtlintExtension,
        format: Boolean,
        crossinline targetProvider: Project.() -> List<String>
    ) = create<JavaExec>(name) {
        this.description = description
        group = "verification"
        mainClass.set("com.pinterest.ktlint.Main")
        classpath = configuration
        isIgnoreExitValue = true

        doFirst {
            val targets = project.targetProvider().takeIf { it.isNotEmpty() }
                ?: throw StopExecutionException()
            args = createArgs(extension, format, targets)
        }

        doLast {
            @Suppress("UnstableApiUsage")
            if (executionResult.orNull?.exitValue != 0) {
                throw GradleException("Code style check is failed.")
            }
        }
    }

    private fun TaskContainer.createKtlintWithSrc(
        name: String,
        description: String,
        configuration: Configuration,
        extension: KtlintExtension,
        format: Boolean
    ) = createKtlint(name, description, configuration, extension, format) {
        listOf("src/**/*.kt")
    }

    private fun TaskContainer.createGitDiff(
        name: String,
        description: String,
        from: String
    ) = create<ExecWithResultTask>(name) {
        this.description = description
        group = "other"
        workingDir = project.rootProject.projectDir

        result.set(File(project.buildDir, "tmp/$name.txt"))

        outputs.cacheIf { false }
        outputs.upToDateWhen { false }

        val output = ByteArrayOutputStream()
        standardOutput = output

        commandLine("git", "diff", "--name-only", from)

        doLast {
            val text =  output.toString()
                .split("\n")
                .map { "${project.rootProject.projectDir.absolutePath}/$it" }
                .filter { it.startsWith(project.projectDir.absolutePath) && it.endsWith(".kt") }
                .joinToString("\n") { it.removePrefix("${project.projectDir.absolutePath}/") }
            result.get().writeText(text)
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun createArgs(
        extension: KtlintExtension,
        format: Boolean,
        files: List<String>
    ) = buildList {
        if (format) {
            add("--format")
        }
        add("--reporter=idea")
        if (extension.android) {
            add("--android")
        }
        extension.disabledRules?.let {
            add(it.joinToString(separator = ",", prefix = "--disabled_rules="))
        }
        addAll(files)
    }
}
