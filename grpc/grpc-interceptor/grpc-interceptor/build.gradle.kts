plugins {
    id("flocon.kotlin.library")
    id("flocon.android.library")
    id("flocon.publish")
}

kotlin {
    android {
        namespace = "io.github.openflocon.flocon.grpc"
    }

    sourceSets {
        androidMain.dependencies {
            implementation(projects.flocon.flocon)
            api(projects.grpc.grpcInterceptorBase.grpcInterceptorBase)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.grpc.android)
            implementation(libs.protobuf.util)
        }
    }
}

floconPublishing {
    coordinates(artifactId = "flocon-grpc-interceptor", name = "Flocon gRPC Interceptor")
}
