plugins {
    id("flocon.kotlin.library")
    id("flocon.android.library")
    id("flocon.ios.library")
    id("flocon.jvm.library")
    id("flocon.publish")
}

kotlin {
    android {
        namespace = "io.github.openflocon.flocon.database.room"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.flocon.flocon)
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
        }

        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
        }
    }
}

floconPublishing {
    coordinates(artifactId = "flocon-database-room", name = "Flocon Database Room")
}
