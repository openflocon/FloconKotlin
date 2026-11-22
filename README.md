<h1 align="center">FloconKotlin</h1>

<p align="center">
  <b>The Android and Kotlin Multiplatform SDK for <a href="https://github.com/openflocon/Flocon">Flocon</a>.</b><br/>
  Inspect your app's network, databases, preferences, analytics and more, live from the Flocon desktop app.
</p>

<p align="center">
  <a href="https://central.sonatype.com/artifact/io.github.openflocon/flocon"><img alt="Maven Central" src="https://img.shields.io/maven-central/v/io.github.openflocon/flocon?label=Maven%20Central" /></a>
  <a href="https://openflocon.github.io/Flocon/"><img alt="Documentation" src="https://img.shields.io/badge/docs-openflocon.github.io-black" /></a>
  <a href="LICENSE"><img alt="License: MIT" src="https://img.shields.io/badge/license-MIT-blue" /></a>
</p>

FloconKotlin is the SDK you add to your Android or Kotlin Multiplatform app (Android, iOS, JVM, WasmJs). It connects your app to the [Flocon desktop app](https://github.com/openflocon/Flocon), where you observe and drive it in real time. The desktop app, the MCP server for AI agents and the full documentation live in the main repository.

Other SDKs: [FloconSwift](https://github.com/openflocon/FloconSwift) (iOS / macOS) and [FloconFlutter](https://github.com/openflocon/FloconFlutter).

## Quick start

1. [Install the desktop app](https://github.com/openflocon/Flocon#1-install-the-desktop-app).
2. Add the SDK. Use the debug variant only, and `flocon-no-op` in release:

```kotlin
// build.gradle.kts
debugImplementation("io.github.openflocon:flocon:LAST_VERSION")
releaseImplementation("io.github.openflocon:flocon-no-op:LAST_VERSION")
// + one module per feature you need (see below)
```

3. Start Flocon:

```kotlin
startFlocon { ... }
```

Then run your app. See the [integration guide](https://openflocon.github.io/Flocon/install-kotlin) and [setup](https://openflocon.github.io/Flocon/setup) for emulators, physical devices and each plugin.

## Modules

Every feature is its own module, so you only ship what you use. Using the `flocon-bom` keeps versions aligned.

| Artifact | Gradle path | What it is |
|---|---|---|
| `flocon` | `:flocon:flocon` | Core, plus the plugins with no extra dependency: network (OkHttp interceptor), database, preferences, files, analytics, deeplinks, feature flags, timeline, crash reporter, dashboards, tables, WebView |
| `flocon-no-op` | `:flocon:flocon-no-op` | Turns Flocon off in release builds (R8 removes the implementation) |
| `flocon-bom` | `:bom` | Bill of materials |
| `flocon-ktor-interceptor` | `:network:ktor-interceptor:ktor-interceptor` | Ktor network interceptor |
| `flocon-grpc-interceptor` | `:grpc:grpc-interceptor:grpc-interceptor` | gRPC interceptor (`-lite` and `-base` variants exist) |
| `flocon-wire-grpc` | `:grpc:wire-grpc:wire-grpc` | Unary Square Wire gRPC inspection |
| `flocon-database-room` / `-room3` / `-sqldelight` | `:database:*` | Database inspection for Room, Room 3 and SQLDelight |
| `flocon-datastores` | `:datastores:datastores` | Typed DataStore |
| `flocon-accessibility` / `-no-op` | `:accessibility:*` | Accessibility tools |
| `flocon-qr-scanner` / `-no-op` | `:qrscanner:*` | QR scanner |

For Square Wire (the adapter builds against 7.2.2), install `FloconNetwork`, then wrap the configured client before creating your service:

```kotlin
import io.github.openflocon.flocon.wire.grpc.withFlocon

val greeter = GrpcGreeterClient(grpcClient.withFlocon(authority = "localhost:50051"))
val reply = greeter.SayHello().execute(HelloRequest(name = "World"))
```

The adapter records unary calls in Desktop and MCP, including decoded Wire messages and the gRPC status.
Use the original client for Wire's JVM `newBuilder()`. If you also install the Flocon OkHttp interceptor,
exclude requests tagged with Wire's `GrpcMethod` in its `shouldLog` filter to avoid duplicate HTTP entries.

Samples: [`sample`](sample) (Android) and [`sample-multiplatform`](sample-multiplatform).

## Contributing

Requires JDK 21.

```bash
./gradlew jvmTest test assemble                          # what CI runs on every PR
./gradlew :sample:android:assembleDebug :sample:android:assembleRelease
bash scripts/check-release-noop.sh                       # checks R8 removed the implementation in release
./publishLocal.sh                                        # publish to mavenLocal to try changes in your app
```

Architecture notes are in [AGENT.md](AGENT.md). Versions are managed in `gradle/libs.versions.toml`.

## License

[MIT](LICENSE)
