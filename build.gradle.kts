plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.plugin.publish)
}

group = "com.github.alorma"
version = "1.0.0"

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
            id = "com.github.alorma.version"
            implementationClass = "VersionPlugin"
            displayName = "Version Plugin"
            description = "Automatically configure Android app versioning from a version.properties file"
            tags = listOf("android", "versioning")
        }
    }
    testSourceSets(sourceSets["test"])
}
