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
        namespace = "io.github.openflocon.flocon.wire.grpc"
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.flocon.flocon)
            api(libs.wire.grpc.client)
        }

        jvmTest.dependencies {
            implementation(libs.mockwebserver)
        }
    }
}

mavenPublishing {
    coordinates(
        groupId = project.property("floconGroupId") as String,
        artifactId = "flocon-wire-grpc",
        version = System.getenv("PROJECT_VERSION_NAME") ?: project.property("floconVersion") as String
    )

    pom {
        name = "Flocon Square Wire gRPC"
    }
}
