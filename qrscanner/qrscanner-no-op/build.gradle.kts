plugins {
    id("flocon.kotlin.library")
    id("flocon.android.library")
    id("flocon.jvm.library")
    id("flocon.publish")
}

kotlin {
    android {
        namespace = "io.github.openflocon.flocon.qrscanner.noop"
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.flocon.floconNoOp)
        }
    }
}

floconPublishing {
    coordinates(artifactId = "flocon-qr-scanner-no-op", name = "Flocon QR Scanner No-Op")
}
