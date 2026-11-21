plugins {
    id("flocon.kotlin.library")
    id("flocon.android.library")
    id("flocon.jvm.library")
    id("flocon.publish")
}

kotlin {
    android {
        namespace = "io.github.openflocon.flocon.qrscanner"
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.flocon.flocon)
            implementation(libs.kotlinx.coroutines.core)
        }

        androidMain.dependencies {
            implementation(libs.google.play.services.code.scanner)
        }
    }
}

floconPublishing {
    coordinates(artifactId = "flocon-qr-scanner", name = "Flocon QR Scanner")
}
