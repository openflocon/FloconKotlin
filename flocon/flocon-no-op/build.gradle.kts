plugins {
    id("flocon.kotlin.library")
    id("flocon.android.library")
    id("flocon.ios.library")
    id("flocon.jvm.library")
    id("flocon.wasm.library")
    id("flocon.publish")
}

// Turns Flocon off: add it next to the real Flocon modules in release builds.
// It brings the real API, plus what turns it off: a marker class found at runtime,
// and an R8 rule that removes the Flocon code from minified Android builds.
kotlin {
    android {
        namespace = "io.github.openflocon.flocon.noop"

        optimization {
            consumerKeepRules.publish = true
            consumerKeepRules.file("consumer-rules.pro")
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.flocon.flocon)
        }
    }
}

floconPublishing {
    coordinates(artifactId = "flocon-no-op", name = "Flocon No-Op")
}

mavenPublishing {
    pom {
        description = "Turns Flocon off in the builds it is added to, such as release builds."
    }
}
