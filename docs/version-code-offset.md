# Extension: `formFactor` (one `versionCode` per form factor)

Origin: the Converses app (`alorma/Converses`) is adding a Wear OS module (`:wear`) that ships under the **same package name and signing key** as the phone app, in the same Google Play listing.

## Problem

`VersionExtension.versionCode` was derived only from `version.properties`. If the plugin is applied to two `com.android.application` modules of the same build (phone and watch), both got **the same `versionCode`**.

Google Play requires the `versionCode` to be **unique across form factors** of one listing (multi-APK delivery; see https://developer.android.com/training/wearables/packaging). The `versionName` can stay identical; only the `versionCode` has to differ.

## Design

Each application module declares its form factor; the plugin adds a fixed offset to the computed `versionCode`:

```kotlin
// wear/build.gradle.kts
appVersion {
    formFactor = FormFactor.WEAR
}
```

```kotlin
enum class FormFactor(val offset: Int) {
    PHONE(0),       // default, codes unchanged
    WEAR(2),
    TV(4),
    AUTOMOTIVE(6),
}
```

A single `formFactor` property was chosen over `versionCodeOffset` or one property per type (`wearOffset`, `tvOffset`...): a module has exactly one form factor, users do not need to know the numbering convention, and new types are one enum entry.

## Semantics

The base formula is unchanged: `MMM mmm ppp S`, where `S` is `1` (release) or `0` (snapshot), parsed as an `Int` (`1.6.1` release → `10060011`).

`versionCode = base + formFactor.offset`.

- Offsets are even and at most 8, so `S + offset` stays a single digit: codes never collide with another form factor in the same `major.minor.patch`, nor with the next patch (a patch bump always raises the code within a form factor).
- Only 5 slots exist (0, 2, 4, 6, 8); `8` is still free.
- Example, `1.6.1`: phone release `10060011`, snapshot `10060010`; watch release `10060013`, snapshot `10060012`.
- Do **not** add a form-factor digit to the base formula: it would change every code already published.

## Implementation notes

- `android.defaultConfig.versionCode` can no longer be assigned at plugin application time (the user's `appVersion { }` block runs later) nor in `afterEvaluate` (AGP has already read it: "It is too late to set versionCode"). The code is wired lazily through the variant API: `ApplicationAndroidComponentsExtension.onVariants { variant.outputs.forEach { it.versionCode.set(provider) } }`.
- `printVersionCode` and `version` read the code through a `Provider` evaluated in `doLast`, so they include the offset and stay configuration-cache compatible.
- `FormFactor` lives in the default package, like `VersionPlugin`, and is usable from build scripts without an import.
- Libraries and KMP modules only use `versionName`; `formFactor` is ignored there.
- Tests: AGP is added to TestKit's plugin classpath through a dedicated `testPluginClasspath` configuration in `build.gradle.kts` (it must share a classloader with the plugin under test, without leaking into published dependencies).

## Tests (TestKit, `VersionPluginTest.kt`)

- No form factor: code unchanged (`1.2.3` → `10020031`).
- `WEAR`, release and snapshot: `10020033` and `10020032`.
- The code AGP actually uses for the variant output equals the printed one.
- `version` task includes the offset (`TV` → `10020035`).
- Second run with `--configuration-cache` reuses the entry.
- Offsets are unique, even and within `0..8`.

## Release

Version bumped to `1.1.0` (new feature). Publishing is done by the maintainer through `.github/workflows/publish.yml`.

## What Converses will do afterwards (not part of this change)

1. Bump `versionPlugin` in its `gradle/libs.versions.toml` to `1.1.0`.
2. Apply the plugin to `:wear` with `appVersion { formFactor = FormFactor.WEAR }`, sharing the root `version.properties`.
3. Check that its release workflow (Gradle Play Publisher) uploads both bundles to the same release.
