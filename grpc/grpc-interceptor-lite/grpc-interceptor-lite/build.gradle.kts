plugins {
    id("flocon.kotlin.library")
    id("flocon.android.library")
    id("flocon.publish")
}

kotlin {
    android {
        namespace = "io.github.openflocon.flocon.grpc.lite"
    }

    sourceSets {
        androidMain.dependencies {
            implementation(projects.flocon.flocon)
            api(projects.grpc.grpcInterceptorBase.grpcInterceptorBase)
            implementation(libs.grpc.android)
            implementation(libs.gson)
        }
    }
}

floconPublishing {
    coordinates(artifactId = "flocon-grpc-interceptor-lite", name = "Flocon gRPC Interceptor Lite")
}
