plugins {
    id("flocon.kotlin.library")
    id("flocon.android.library")
    id("flocon.jvm.library")
    id("flocon.ios.library")
    id("flocon.wasm.library")
    id("flocon.publish")
}

kotlin {
    android {
        namespace = "io.github.openflocon.flocon.accessibility.noop"
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.flocon.floconNoOp)
        }
    }
}

floconPublishing {
    coordinates(artifactId = "flocon-accessibility-no-op", name = "Flocon Accessibility No-Op")
}
