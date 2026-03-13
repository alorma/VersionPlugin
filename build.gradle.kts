plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.maven.publish)
    `java-gradle-plugin`
}

group = "com.github.alorma"
version = "1.0.1"

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
    plugins {
        create("versionPlugin") {
            id = "com.github.alorma.version"
            implementationClass = "VersionPlugin"
            displayName = "Version Plugin"
            description = "Automatically configure Android app versioning from a version.properties file"
        }
    }
    testSourceSets(sourceSets["test"])
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates("com.github.alorma", "version-plugin", "1.0.0")

    pom {
        name.set("Version Plugin")
        description.set("Automatically configure Android app versioning from a version.properties file")
        url.set("https://github.com/alorma/VersionPlugin")

        licenses {
            license {
                name.set("Apache License 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0")
            }
        }

        developers {
            developer {
                id.set("alorma")
                name.set("Bernat Borrás")
                email.set("bernatbor15@gmail.com")
            }
        }

        scm {
            connection.set("scm:git:github.com/alorma/VersionPlugin.git")
            developerConnection.set("scm:git:ssh://github.com/alorma/VersionPlugin.git")
            url.set("https://github.com/alorma/VersionPlugin/tree/main")
        }
    }
}
