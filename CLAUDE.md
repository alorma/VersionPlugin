# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
./gradlew assemble

# Run all tests
./gradlew test --no-configuration-cache

# Run a single test
./gradlew test --tests "VersionPluginTest.printVersionName outputs release version name" --no-configuration-cache

# Publish to local build/repo/ for manual inspection
./gradlew publishToMavenLocal
```

## Architecture

This is a single-module Gradle plugin project. All logic lives in two files:

- `src/main/kotlin/VersionPlugin.kt` — the plugin entry point (`VersionPlugin`) and the `VersionExtension` class that parses `version.properties` and computes `versionName`/`versionCode`.
- `src/test/kotlin/VersionPluginTest.kt` — functional tests using Gradle TestKit. Each test writes a temporary project to a `@TempDir` and runs the real Gradle runner against it with the plugin on the classpath (no publish step needed — `gradlePlugin { testSourceSets(...) }` handles this).

## Key conventions

- **Group / plugin ID**: `io.github.alorma` / `io.github.alorma.version`
- **Publishing**: uses `com.gradle.plugin-publish` — publishes to the Gradle Plugin Portal via `./gradlew publishPlugins`. Credentials `GRADLE_PUBLISH_KEY` / `GRADLE_PUBLISH_SECRET` are passed as env vars in CI (see `.github/workflows/publish.yml`).
- **Kotlin version** must match Gradle's embedded Kotlin (currently `2.3.0` for Gradle 9.4). Changing the Gradle wrapper version requires updating `kotlin` in `gradle/libs.versions.toml` to match.
- **`versionCode` formula**: each of `major`, `minor`, `patch` is zero-padded to 3 digits, then `1` (release) or `0` (snapshot) is appended, and the result is parsed as `Int`. Leading zeroes are dropped by `toInt()`, so `1.2.3` → `10020031`.
