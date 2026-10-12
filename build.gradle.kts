import org.gradle.plugin.compatibility.compatibility

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.plugin.publish)
}

group = "io.github.alorma"
version = "1.1.1"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

// AGP must share a classloader with the plugin under test (TestKit), without leaking into its published dependencies
val testPluginClasspath = configurations.create("testPluginClasspath")

dependencies {
    testPluginClasspath(libs.android.gradlePlugin)
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.launcher)
}

tasks.pluginUnderTestMetadata {
    pluginClasspath.from(testPluginClasspath)
}

tasks.test {
    useJUnitPlatform()
}

gradlePlugin {
    website = "https://github.com/alorma/VersionPlugin"
    vcsUrl = "https://github.com/alorma/VersionPlugin"

    plugins {
        create("versionPlugin") {
            id = "io.github.alorma.version"
            implementationClass = "VersionPlugin"
            displayName = "Version Plugin"
            description = "Automatically configure versioning from a version.properties file — supports Android apps, Android libraries, and Kotlin Multiplatform (KMP) projects"
            tags = listOf("android", "kotlin-multiplatform", "kmp", "versioning")
            compatibility {
                features {
                    configurationCache = true
                }
            }
        }
    }
    testSourceSets(sourceSets["test"])
}
