# VersionPlugin

A Gradle plugin that automatically configures `versionName` and `versionCode` for Android apps from a `version.properties` file.

## Setup

### 1. Add the plugin to your version catalog

```toml
# gradle/libs.versions.toml
[versions]
versionPlugin = "1.0.0"

[plugins]
versionPlugin = { id = "io.github.alorma.version", version.ref = "versionPlugin" }
```

### 2. Apply the plugin in your app module

```kotlin
// app/build.gradle.kts
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

That's it. The plugin reads the file and automatically sets `android.defaultConfig.versionName` and `versionCode`.

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
