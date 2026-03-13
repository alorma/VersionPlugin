import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class VersionPluginTest {

    @TempDir
    lateinit var projectDir: File

    private fun writeVersionProperties(
        major: Int = 1,
        minor: Int = 2,
        patch: Int = 3,
        snapshot: Boolean = false,
    ) {
        projectDir.resolve("version.properties").writeText(
            """
            major=$major
            minor=$minor
            patch=$patch
            snapshot=$snapshot
            """.trimIndent()
        )
    }

    private fun writeBuildScript() {
        projectDir.resolve("settings.gradle.kts").writeText(
            """
            pluginManagement {
                repositories {
                    mavenLocal()
                    gradlePluginPortal()
                }
            }
            rootProject.name = "test-project"
            """.trimIndent()
        )
        projectDir.resolve("build.gradle.kts").writeText(
            """
            plugins {
                id("io.github.alorma.version")
            }
            """.trimIndent()
        )
    }

    private fun runner(vararg tasks: String): GradleRunner =
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments(*tasks, "--stacktrace")
            .withPluginClasspath()

    // --- version name ---

    @Test
    fun `printVersionName outputs release version name`() {
        writeVersionProperties(major = 1, minor = 2, patch = 3, snapshot = false)
        writeBuildScript()

        val result = runner("printVersionName").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":printVersionName")?.outcome)
        assertTrue(result.output.contains("1.2.3"), "Expected '1.2.3' in output:\n${result.output}")
    }

    @Test
    fun `printVersionName outputs snapshot version name`() {
        writeVersionProperties(major = 0, minor = 1, patch = 0, snapshot = true)
        writeBuildScript()

        val result = runner("printVersionName").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":printVersionName")?.outcome)
        assertTrue(result.output.contains("0.1.0-SNAPSHOT"), "Expected '0.1.0-SNAPSHOT' in output:\n${result.output}")
    }

    // --- version code ---

    @Test
    fun `printVersionCode outputs correct code for release`() {
        writeVersionProperties(major = 1, minor = 2, patch = 3, snapshot = false)
        writeBuildScript()

        val result = runner("printVersionCode").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":printVersionCode")?.outcome)
        // 001 002 003 1 → "0010020031".toInt() = 10020031
        assertTrue(result.output.contains("10020031"), "Expected '10020031' in output:\n${result.output}")
    }

    @Test
    fun `printVersionCode outputs correct code for snapshot`() {
        writeVersionProperties(major = 1, minor = 2, patch = 3, snapshot = true)
        writeBuildScript()

        val result = runner("printVersionCode").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":printVersionCode")?.outcome)
        // 001 002 003 0 → "0010020030".toInt() = 10020030
        assertTrue(result.output.contains("10020030"), "Expected '10020030' in output:\n${result.output}")
    }

    // --- version task ---

    @Test
    fun `version task prints all components`() {
        writeVersionProperties(major = 2, minor = 0, patch = 5, snapshot = false)
        writeBuildScript()

        val result = runner("version").build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":version")?.outcome)
        with(result.output) {
            assertTrue(contains("2.0.5"))
            assertTrue(contains("Major:"))
            assertTrue(contains("Minor:"))
            assertTrue(contains("Patch:"))
            assertTrue(contains("Snapshot:"))
        }
    }

    // --- missing version.properties ---

    @Test
    fun `build fails when version properties file is missing`() {
        writeBuildScript()
        // intentionally NOT writing version.properties

        val result = runner("printVersionName").buildAndFail()

        assertTrue(
            result.output.contains("version.properties file not found"),
            "Expected error about missing file:\n${result.output}"
        )
    }
}
