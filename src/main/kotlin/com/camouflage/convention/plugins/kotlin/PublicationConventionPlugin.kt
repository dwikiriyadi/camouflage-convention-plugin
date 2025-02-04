package com.camouflage.convention.plugins.kotlin

import com.android.build.api.dsl.LibraryExtension
import com.camouflage.convention.plugins.configuration.PublishConfiguration
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get

class PublicationConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            with(pluginManager) {
                apply("maven-publish")
            }

            val configuration =
                extensions.create("publishConfiguration", PublishConfiguration::class.java)

            extensions.configure<LibraryExtension> {
                publishing {
                    multipleVariants {
                        allVariants()
                    }
                }
            }

            afterEvaluate {
                extensions.configure<PublishingExtension> {

                    publications {
                        val configGroupId = configuration.groupId
                        val configArtifactId = configuration.artifactId
                        val configVersion = configuration.version

                        create("release", MavenPublication::class.java) {
                            artifact(tasks["bundleReleaseAar"])
                            groupId = configGroupId
                            artifactId = configArtifactId
                            version = configVersion
                            pom.withXml {
                                val dependenciesNode = asNode().appendNode("dependencies")

                                configurations["api"].allDependencies.forEach {
                                    val dependencyNode =
                                        dependenciesNode.appendNode("dependency")
                                    dependencyNode.appendNode("groupId", it.group)
                                    dependencyNode.appendNode("artifactId", it.name)
                                    dependencyNode.appendNode("version", it.version)
                                }
                            }
                        }
                        create("debug", MavenPublication::class.java) {
                            artifact(tasks["bundleDebugAar"])
                            groupId = configGroupId
                            artifactId = "$configArtifactId-debug"
                            version = configVersion
                            pom.withXml {
                                val dependenciesNode = asNode().appendNode("dependencies")

                                configurations["api"].allDependencies.forEach {
                                    val dependencyNode =
                                        dependenciesNode.appendNode("dependency")
                                    dependencyNode.appendNode("groupId", it.group)
                                    dependencyNode.appendNode("artifactId", it.name)
                                    dependencyNode.appendNode("version", it.version)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}