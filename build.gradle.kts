import org.gradle.plugin.compatibility.compatibility

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.plugin.publish)
}

group = "io.github.alorma"
version = "1.0.3"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.launcher)
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
