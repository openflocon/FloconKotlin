plugins {
    id("flocon.kotlin.library")
    id("flocon.android.library")
    id("flocon.publish")
}

kotlin {
    android {
        namespace = "io.github.openflocon.flocon.grpc.base"
    }

    sourceSets {
        androidMain.dependencies {
            api(projects.flocon.flocon)
            api(projects.flocon.flocon)
            // FloconGrpcBaseInterceptor is a ClientInterceptor
            api(libs.grpc.android)

            implementation(dependencies.platform(libs.kotlinx.coroutines.bom))
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}

floconPublishing {
    coordinates(artifactId = "flocon-grpc-interceptor-base", name = "Flocon gRPC Interceptor Base")
}
