plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    id("maven-publish")
    id("com.gradle.plugin-publish").version("0.16.0")
}

repositories {
    mavenCentral()
}

group = "com.doist.gradle"
version = property("version") as String

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin")
}

val pluginName = "Ktlint"

gradlePlugin {
    plugins.register(pluginName) {
        id = "${project.group}.${project.name}"
        implementationClass = "com.doist.gradle.KtlintPlugin"
    }
    isAutomatedPublishing = true
}

pluginBundle {
    website = "https://github.com/Doist/ktlint-gradle-plugin"
    vcsUrl = "https://github.com/Doist/ktlint-gradle-plugin.git"

    plugins.getByName(pluginName) {
        displayName = "Ktlint Gradle Plugin"
        description = "This plugin is mainly designed to validate the code styles on changed kt files."
        tags = listOf(
            "check",
            "code quality",
            "git",
            "kotlin",
            "ktlint",
            "verification"
        )
    }

    mavenCoordinates {
        groupId = project.group.toString()
        artifactId = project.name
        version = project.version.toString()
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions{
        freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
    }
}

tasks.named<Wrapper>("wrapper") {
    distributionType = Wrapper.DistributionType.ALL
}
