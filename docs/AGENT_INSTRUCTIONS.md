# VersionPlugin: instructions for AI agents

Use this file when you are asked to **add VersionPlugin to a project** (for working on the plugin
itself, see [AGENTS.md](../AGENTS.md)).

VersionPlugin is a Gradle plugin (`io.github.alorma.version`, Gradle Plugin Portal) that derives the
app version from a single `version.properties` file. Supports Android apps, Android libraries and
Kotlin Multiplatform modules.

## 1. Setup (3 steps)

Latest version: check the Gradle Plugin Portal / `build.gradle.kts` in this repo (`1.1.1` at the time
of writing).

1. Add to the version catalog (or use the `id(...) version` form below):

```toml
# gradle/libs.versions.toml
[versions]
versionPlugin = "1.1.1"

[plugins]
versionPlugin = { id = "io.github.alorma.version", version.ref = "versionPlugin" }
```

2. Apply it in **each module that needs the version**:

```kotlin
plugins {
  alias(libs.plugins.versionPlugin)
  // or: id("io.github.alorma.version") version "1.1.1"
}
```

3. Create `version.properties` in the **root project directory** (all four keys are required; a missing
   file or key fails the build):

```properties
major=1
minor=0
patch=0
snapshot=false
```

Plugin order does not matter: the plugin reacts to `com.android.application`, `com.android.library`
and `org.jetbrains.kotlin.multiplatform` whenever they are applied.

## 2. What it configures

| Module type | Effect |
|-------------|--------|
| `com.android.application` | `versionName` in `defaultConfig`; `versionCode` set on each variant output |
| `com.android.library` | `project.version` |
| `org.jetbrains.kotlin.multiplatform` | `project.version` (picked up by `maven-publish`) |

Do **not** also set `versionName` / `versionCode` by hand in `defaultConfig`; the plugin owns them.

## 3. Version format

- `versionName`: `major.minor.patch`, plus `-SNAPSHOT` when `snapshot=true`.
- `versionCode` (apps only): each of major/minor/patch zero-padded to 3 digits, then `1` (release) or
  `0` (snapshot), parsed as Int. `1.2.3` release → `10020031`, snapshot → `10020030`.
  Limits: each component must stay below 1000 and the code must fit in an Int (major ≤ 2 in practice).

To release: edit `version.properties` (bump numbers, set `snapshot=false`). Do not edit Gradle files.

## 4. Several apps in one Play listing (Wear OS, TV, Automotive)

Modules sharing a package name and `version.properties` need distinct `versionCode`s. Set the form
factor in every non-phone `com.android.application` module:

```kotlin
// wear/build.gradle.kts
plugins { alias(libs.plugins.versionPlugin) }

appVersion {
  formFactor = FormFactor.WEAR
}
```

Offsets added to the code: `PHONE` 0 (default), `WEAR` 2, `TV` 4, `AUTOMOTIVE` 6. `formFactor` is
ignored for libraries and KMP. The extension is named `appVersion`.

## 5. Tasks (group `help`)

```
./gradlew version           # all version info
./gradlew printVersionName  # name only, for CI
./gradlew printVersionCode  # code only, for CI; registered only on com.android.application modules
```

For CI, read the version with `./gradlew -q printVersionName`.

## 6. Common mistakes

- Putting `version.properties` in a submodule: it is read from the **root** project.
- Omitting a key (`snapshot`, etc.) or using non-numeric values: the build fails.
- Hardcoding `version = "..."` or `versionCode` in modules that apply the plugin.
- Expecting `printVersionCode` or a `versionCode` on libraries / KMP modules: only apps have one.
- Using the same `versionCode` for phone and watch modules: set `formFactor` on the non-phone one.
- Forgetting `-q` when parsing task output in CI scripts.

## 7. Where to look for more

- [README](../README.md) and [formFactor design notes](version-code-offset.md).
- Tests in `src/test/kotlin/VersionPluginTest.kt` show complete example projects.
