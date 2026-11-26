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
        namespace = "io.github.openflocon.flocon.ktor"
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.flocon.flocon)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.client.core)
        }
    }
}

floconPublishing {
    coordinates(artifactId = "flocon-ktor-interceptor", name = "Flocon Ktor Interceptor")
}
