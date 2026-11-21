plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.compose) apply false

    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false

    alias(libs.plugins.compose.multiplatform) apply false

    alias(libs.plugins.ksp) apply false

    alias(libs.plugins.vanniktech.maven.publish) apply false

    alias(libs.plugins.protobuf) apply false

    alias(libs.plugins.buildconfig) apply false

    alias(libs.plugins.sqldelight) apply false

    alias(libs.plugins.ktlint.gradle) apply false
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    extensions.configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set(rootProject.libs.versions.ktlint.engine.get())
        filter {
            exclude { it.file.path.contains("/build/") }
        }
    }

    dependencies {
        add("ktlintRuleset", rootProject.libs.compose.rules.ktlint)
    }
}

// Samples consume either the local Flocon modules (default) or the artifacts published on Maven,
// to check that a release was uploaded correctly:
//   ./gradlew :sample:android:assembleDebug -PfloconUseMaven=true [-PfloconMavenVersion=x.y.z]
// without floconMavenVersion, each module is taken at its own version (floconVersion.<artifactId> in gradle.properties)
val floconUseMaven = providers.gradleProperty("floconUseMaven").map(String::toBoolean).getOrElse(false)
val floconMavenVersion = providers.gradleProperty("floconMavenVersion").orNull
val floconGroupId = providers.gradleProperty("floconGroupId").get()

fun floconVersion(artifactId: String): String = providers.gradleProperty("floconVersion.$artifactId").get()
fun floconMavenModule(artifactId: String) = "$floconGroupId:$artifactId:${floconMavenVersion ?: floconVersion(artifactId)}"

// project path -> published artifactId
val floconModules = mapOf(
    ":flocon:flocon" to "flocon",
    ":flocon:flocon-no-op" to "flocon-no-op",
    ":accessibility:accessibility" to "flocon-accessibility",
    ":accessibility:accessibility-no-op" to "flocon-accessibility-no-op",
    ":database:room:room" to "flocon-database-room",
    ":database:room3:room3" to "flocon-database-room3",
    ":database:sqldelight:sqldelight" to "flocon-database-sqldelight",
    ":datastores:datastores" to "flocon-datastores",
    ":grpc:grpc-interceptor-base:grpc-interceptor-base" to "flocon-grpc-interceptor-base",
    ":grpc:grpc-interceptor:grpc-interceptor" to "flocon-grpc-interceptor",
    ":grpc:grpc-interceptor-lite:grpc-interceptor-lite" to "flocon-grpc-interceptor-lite",
    ":network:ktor-interceptor:ktor-interceptor" to "flocon-ktor-interceptor",
    ":qrscanner:qrscanner" to "flocon-qr-scanner",
    ":qrscanner:qrscanner-no-op" to "flocon-qr-scanner-no-op",
)

// used by the samples to declare their dependencies: see flocon() in sample/android/build.gradle.kts
extra["floconUseMaven"] = floconUseMaven
extra["floconMavenCoordinates"] = floconModules.mapValues { (_, artifactId) -> floconMavenModule(artifactId) }

// :sample:android release builds add flocon-no-op, which turns Flocon off. The only modules it can't handle
// are the ones with Android manifest entries: they are swapped for their "-no-op" sibling, including when
// they come transitively from :sample-multiplatform
val floconManifestModules = mapOf(
    ":accessibility:accessibility" to "flocon-accessibility",
    ":qrscanner:qrscanner" to "flocon-qr-scanner",
)
project(":sample:android").configurations.configureEach {
    if (name.startsWith("release")) {
        resolutionStrategy.dependencySubstitution {
            floconManifestModules.forEach { (path, artifactId) ->
                val noOpModule = module(floconMavenModule("$artifactId-no-op"))
                substitute(project(path)).using(if (floconUseMaven) noOpModule else project("$path-no-op"))
            }
        }
    }
}

if (floconUseMaven) {
    listOf(":sample:android", ":sample-multiplatform").forEach { samplePath ->
        project(samplePath).configurations.configureEach {
            if (samplePath == ":sample:android" && name.startsWith("release")) return@configureEach // manifest no-ops, see above
            resolutionStrategy.dependencySubstitution {
                floconModules.forEach { (path, artifactId) ->
                    substitute(project(path)).using(module(floconMavenModule(artifactId)))
                }
            }
        }
    }
}

// Releases only some modules: ./gradlew publishFloconRelease -PfloconRelease=<artifactId>,<artifactId>,...
// The modules that are not released are taken from Maven Central at their released version (floconVersion.<artifactId>),
// so a released module is built against what its users will get: it fails to compile if it relies on unreleased changes
// of the modules it depends on (e.g. flocon-database-room using a new flocon API without releasing flocon)
val floconRelease = providers.gradleProperty("floconRelease").orNull
    ?.split(',')?.map(String::trim)?.filter(String::isNotEmpty)?.toSet()
if (floconRelease != null) {
    val publishedModules = floconModules + (":bom" to "flocon-bom")
    val unknown = floconRelease - publishedModules.values.toSet()
    require(unknown.isEmpty()) { "Unknown modules in floconRelease: $unknown, expected some of ${publishedModules.values}" }

    val notReleased = floconModules.filterValues { it !in floconRelease }
    subprojects {
        configurations.configureEach {
            resolutionStrategy.dependencySubstitution {
                notReleased.forEach { (path, artifactId) ->
                    substitute(project(path)).using(module("$floconGroupId:$artifactId:${floconVersion(artifactId)}"))
                }
            }
        }
    }

    tasks.register("publishFloconRelease") {
        description = "Publishes the modules listed in -PfloconRelease to Maven Central"
        dependsOn(publishedModules.filterValues { it in floconRelease }.keys.map { "$it:publishToMavenCentral" })
    }
}
