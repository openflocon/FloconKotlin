plugins {
    id("flocon.kotlin.library")
    id("flocon.android.library")
    id("flocon.ios.library")
    id("flocon.jvm.library")
    id("flocon.wasm.library")
    id("flocon.publish")
}

kotlin {
    android {
        namespace = "io.github.openflocon.flocon.database.room3"
    }

    sourceSets {
        // the SQLite driver methods are not suspend on these targets, they are on the web
        listOf("androidMain", "jvmMain", "iosMain").forEach {
            named(it) { kotlin.srcDir("src/nonWebMain/kotlin") }
        }
        commonMain {
            kotlin.srcDir("src/roomMain/kotlin")
            dependencies {
                api(projects.flocon.flocon)
                api(libs.androidx.room3.runtime)
                implementation(libs.androidx.sqlite.async)

                implementation(dependencies.platform(libs.kotlinx.coroutines.bom))
                implementation(libs.kotlinx.coroutines.core)

                implementation(dependencies.platform(libs.okhttp.bom))
            }
        }
    }
}

floconPublishing {
    coordinates(artifactId = "flocon-database-room3", name = "Flocon Room3 Database")
}
