import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.util.Properties

class VersionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create("appVersion", VersionExtension::class.java, target)

        target.tasks.register("version", DefaultTask::class.java, Action { task ->
            task.group = "help"
            task.description = "Displays the current version information"

            val versionName = extension.versionName
            val versionCode = extension.versionCode
            val major = extension.major
            val minor = extension.minor
            val patch = extension.patch
            val snapshot = extension.snapshot

            task.doLast {
                println("====================================")
                println("  Version Information")
                println("====================================")
                println("Version Name: $versionName")
                if (target.plugins.hasPlugin("com.android.application")) {
                    println("Version Code: $versionCode")
                }
                println()
                println("Components:")
                println("  Major:    $major")
                println("  Minor:    $minor")
                println("  Patch:    $patch")
                println("  Snapshot: $snapshot")
                println("====================================")
            }
        })

        target.tasks.register("printVersionName", DefaultTask::class.java, Action { task ->
            task.group = "help"
            task.description = "Prints the version name (for CI/CD)"

            val versionName = extension.versionName

            task.doLast {
                println(versionName)
            }
        })

        target.plugins.withId("com.android.application") {
            target.tasks.register("printVersionCode", DefaultTask::class.java, Action { task ->
                task.group = "help"
                task.description = "Prints the version code (for CI/CD)"

                val versionCode = extension.versionCode

                task.doLast {
                    println(versionCode)
                }
            })

            val android = target.extensions.getByType(ApplicationExtension::class.java)
            android.defaultConfig {
                versionCode = extension.versionCode
                versionName = extension.versionName
            }
        }

        target.plugins.withId("com.android.library") {
            target.version = extension.versionName
        }

        target.plugins.withId("org.jetbrains.kotlin.multiplatform") {
            target.version = extension.versionName
        }
    }
}

open class VersionExtension(private val project: Project) {
    private val versionProperties = Properties().apply {
        val versionFile = project.rootProject.file("version.properties")
        if (versionFile.exists()) {
            load(versionFile.inputStream())
        } else {
            throw IllegalStateException("version.properties file not found at ${versionFile.absolutePath}")
        }
    }

    val major: Int = versionProperties.getProperty("major").toInt()
    val minor: Int = versionProperties.getProperty("minor").toInt()
    val patch: Int = versionProperties.getProperty("patch").toInt()
    val snapshot: Boolean = versionProperties.getProperty("snapshot").toBoolean()

    val versionName: String = buildString {
        append("$major.$minor.$patch")
        if (snapshot) append("-SNAPSHOT")
    }

    val versionCode: Int = run {
        val majorPart = major.toString().padStart(3, '0')
        val minorPart = minor.toString().padStart(3, '0')
        val patchPart = patch.toString().padStart(3, '0')
        val snapshotPart = if (snapshot) "0" else "1"
        "$majorPart$minorPart$patchPart$snapshotPart".toInt()
    }
}
