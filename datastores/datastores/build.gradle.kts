plugins {
    id("flocon.kotlin.library")
    id("flocon.android.library")
    id("flocon.ios.library")
    id("flocon.jvm.library")
    // no wasm: DataStore does not support it
    id("flocon.publish")
}

kotlin {
    android {
        namespace = "io.github.openflocon.flocon.datastores"
    }

    sourceSets {
        commonMain.dependencies {
            // FloconDatastorePreference implements FloconPreference
            api(projects.flocon.flocon)
            api(libs.androidx.datastore.preferences.core)

            implementation(dependencies.platform(libs.kotlinx.coroutines.bom))
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}

floconPublishing {
    coordinates(artifactId = "flocon-datastores", name = "Flocon Datastores")
}
