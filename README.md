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
| `flocon-database-room` / `-room3` / `-sqldelight` | `:database:*` | Database inspection for Room, Room 3 and SQLDelight |
| `flocon-datastores` | `:datastores:datastores` | Typed DataStore |
| `flocon-accessibility` / `-no-op` | `:accessibility:*` | Accessibility tools |
| `flocon-qr-scanner` / `-no-op` | `:qrscanner:*` | QR scanner |

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

## Releasing

Each module has its own version (`floconVersion.<artifactId>` in `gradle.properties`), so a module can be released without the others:

1. In a PR, bump the version of the modules you release, and of `flocon-bom`.
2. Run the [Android - Publish to MavenCentral](.github/workflows/publish_android.yml) workflow and tick those modules. It fails early if a version is already on Maven Central, then publishes them with `flocon-bom` and tags `<artifactId>/<version>`.

A released module depends on the released version of the other modules: `flocon-database-room` 2.0.3 released alone depends on the `flocon` version in `gradle.properties` (say 2.0.0). To make sure it works with it, the workflow builds it against the modules from Maven Central: if it uses a `flocon` change that is not released yet, it fails to compile, and `flocon` must be released with it.

Check it locally with `./gradlew :database:room:room:compileKotlinJvm -PfloconUseMaven=true -PfloconRelease=flocon-database-room`.

## License

[MIT](LICENSE)
