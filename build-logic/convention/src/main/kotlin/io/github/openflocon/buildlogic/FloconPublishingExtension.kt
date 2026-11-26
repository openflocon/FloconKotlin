package io.github.openflocon.buildlogic

import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import javax.inject.Inject

/**
 * Each published module has its own version, set by `floconVersion.<artifactId>` in the root gradle.properties,
 * so a module can be released without the others.
 */
abstract class FloconPublishingExtension @Inject constructor(private val project: Project) {

    fun coordinates(artifactId: String, name: String) {
        val version = project.floconVersion(artifactId)
        project.extensions.configure<MavenPublishBaseExtension> {
            coordinates(
                groupId = project.property("floconGroupId") as String,
                artifactId = artifactId,
                version = version,
            )
            pom { this.name.set(name) }
        }
    }
}

fun Project.floconVersion(artifactId: String): String = providers.gradleProperty("floconVersion.$artifactId").orNull
    ?: error("Missing floconVersion.$artifactId in gradle.properties")
