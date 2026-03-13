# VersionPlugin

A Gradle plugin that automatically configures versioning from a `version.properties` file. Works with Android apps, Android libraries, and Kotlin Multiplatform (KMP) projects.

## Setup

### 1. Add the plugin to your version catalog

```toml
# gradle/libs.versions.toml
[versions]
versionPlugin = "1.0.0"

[plugins]
versionPlugin = { id = "io.github.alorma.version", version.ref = "versionPlugin" }
```

### 2. Apply the plugin in your module

```kotlin
// app/build.gradle.kts  (Android app, Android library, or KMP module)
plugins {
    alias(libs.plugins.versionPlugin)
}
```

Alternatively, without a version catalog:

```kotlin
// app/build.gradle.kts
plugins {
    id("io.github.alorma.version") version "1.0.0"
}
```

### 3. Create `version.properties` in your project root

```properties
major=1
minor=0
patch=0
snapshot=false
```

That's it. The plugin reads the file and automatically configures versioning:

| Project type | What gets set |
|---|---|
| `com.android.application` | `android.defaultConfig.versionName` and `android.defaultConfig.versionCode` |
| `com.android.library` | `project.version` |
| `org.jetbrains.kotlin.multiplatform` | `project.version` |

## Version format

| Property | Example | Result |
|---|---|---|
| `versionName` (release) | `major=1, minor=2, patch=3` | `1.2.3` |
| `versionName` (snapshot) | `snapshot=true` | `1.2.3-SNAPSHOT` |
| `versionCode` | `major=1, minor=2, patch=3, snapshot=false` | `10020031` |

The `versionCode` is computed by zero-padding each component to 3 digits and appending `1` for release or `0` for snapshot:
```
001 002 003 1  →  "0010020031".toInt()  →  10020031
```

## Kotlin Multiplatform

For KMP projects, the plugin sets `project.version` which is automatically picked up by the Kotlin Multiplatform publishing plugin (`maven-publish`). This works for both KMP libraries and KMP apps.

```kotlin
// shared/build.gradle.kts
plugins {
    kotlin("multiplatform")
    id("io.github.alorma.version")
    `maven-publish`
}

kotlin {
    androidTarget()
    iosArm64()
    jvm()
}
// project.version is automatically set to the value from version.properties
```

## Tasks

The plugin registers three tasks in the `help` group:

```
./gradlew version          # Prints all version info
./gradlew printVersionName # Prints the version name (useful in CI)
./gradlew printVersionCode # Prints the version code (useful in CI)
```

## License

```
Copyright 2024 Bernat Borrás

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0
```
