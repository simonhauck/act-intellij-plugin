import io.github.simonhauck.release.version.api.Version
import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("build.common.lifecycle")
    id("build.common.kotlin-conventions")
    alias(libs.plugins.intelliJPlatform)
    alias(libs.plugins.changelog)
    alias(libs.plugins.releasePlugin)
}

group = "io.github.simonhauck"

version = Version.fromPropertiesFile(file("version.properties"))

repositories {
    mavenCentral()

    // IntelliJ Platform Gradle Plugin Repositories Extension - read more:
    // https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-repositories-extension.html
    intellijPlatform { defaultRepositories() }
}

dependencies {
    testImplementation(libs.bundles.junit)
    testRuntimeOnly(libs.junitRuntime)
    testRuntimeOnly(libs.junitPlatforLauncher)

    intellijPlatform {
        create(
            providers.gradleProperty("platformType"),
            providers.gradleProperty("platformVersion"),
        )

        // Plugin Dependencies. Uses `platformBundledPlugins` property from the gradle.properties
        // file for bundled IntelliJ Platform plugins.
        bundledPlugins(providers.gradleProperty("platformBundledPlugins").map { it.split(',') })

        // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file for
        // plugin from JetBrains Marketplace.
        plugins(providers.gradleProperty("platformPlugins").map { it.split(',') })

        pluginVerifier()
        zipSigner()
        testFramework(TestFrameworkType.Platform)
    }
}

// Configure IntelliJ Platform Gradle Plugin - read more:
// https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-extension.html
intellijPlatform {
    pluginConfiguration {
        // Extract the <!-- Plugin description --> section from README.md and provide for the
        // plugin's manifest
        description =
            providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
                val start = "<!-- Plugin description -->"
                val end = "<!-- Plugin description end -->"

                with(it.lines()) {
                    if (!containsAll(listOf(start, end))) {
                        throw GradleException(
                            "Plugin description section not found in README.md:\n$start ... $end"
                        )
                    }
                    subList(indexOf(start) + 1, indexOf(end))
                        .joinToString("\n")
                        .let(::markdownToHTML)
                }
            }

        //        val changelog = project.changelog // local variable for configuration cache
        // compatibility
        // Get the latest available change notes from the changelog file
        changeNotes =
            with(changelog) {
                val releaseVersion = Version.fromPropertiesFile(file("version.properties"))
                renderItem(
                    (getOrNull(releaseVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
            untilBuild = providers.gradleProperty("pluginUntilBuild")
        }
    }

    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
        val currentVersion = Version.fromPropertiesFile(file("version.properties"))
        channels =
            listOf(
                currentVersion.substringAfter('-', "").substringBefore('.').ifEmpty { "default" }
            )
    }

    pluginVerification { ides { recommended() } }
}

// ---------------------------------------------------------------------------------------------------------------------
// Testing
// ---------------------------------------------------------------------------------------------------------------------

val checkJvmArgsCompatibilityTask =
    tasks.register<Build_common_lifecycle_gradle.CheckJvmArgsCompatibilityTask>(
        "checkJvmArgsCompatibility"
    ) {
        gradlePropertiesFiles =
            listOf(
                layout.projectDirectory.file("gradle.properties").asFile,
                layout.projectDirectory.file("build-logic/gradle.properties").asFile,
            )
    }

tasks.check { dependsOn(checkJvmArgsCompatibilityTask) }

intellijPlatformTesting {
    runIde {
        register("runIdeForUiTests") {
            task {
                jvmArgumentProviders += CommandLineArgumentProvider {
                    listOf(
                        "-Drobot-server.port=8082",
                        "-Dide.mac.message.dialogs.as.sheets=false",
                        "-Djb.privacy.policy.text=<!--999.999-->",
                        "-Djb.consents.confirmation.enabled=false",
                    )
                }
            }

            plugins { robotServerPlugin() }
        }
    }
}

// ---------------------------------------------------------------------------------------------------------------------
// Configure release process
// ---------------------------------------------------------------------------------------------------------------------

// Configure Gradle Changelog Plugin - read more:
// https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    groups.set(listOf("Added", "Changed", "Deprecated", "Removed", "Fixed", "Security"))
    repositoryUrl = "https://github.com/simonhauck/act-intellij-plugin"
}

release {
    disablePush = true
    releaseCommitAddFiles.set(listOf(file("version.properties"), file("CHANGELOG.md")))
    checkForUncommittedFiles = false
}

tasks.writeReleaseVersion {
    doLast {
        val newVersion = Version.fromPropertiesFile(file("version.properties"))
        project.version = newVersion
    }
    notCompatibleWithConfigurationCache("Project version is right now set manually")
}

tasks.patchChangelog { dependsOn(tasks.writeReleaseVersion) }

tasks.commitReleaseVersion { dependsOn(tasks.patchChangelog) }
