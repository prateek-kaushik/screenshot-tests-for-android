package com.agoda.testing.screenshot.build

import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.TestVariant
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File


open class PullScreenshotsTask : ScreenshotTask() {
    companion object {
        fun taskName(variant: TestVariant) = "pull${variant.name.capitalize()}Screenshots"

        fun getReportDir(project: Project, variant: TestVariant): File =
                File(project.buildDir, "screenshots" + variant.name.capitalize())
    }

    private lateinit var apkPath: File

    @Input
    protected var verify = false

    @Input
    protected var record = false

    @Input
    protected var bundleResults = false

    @Input
    protected lateinit var testRunId: String

    @Input
    protected var keepOldRecord = false

    init {
        description = "Pull screenshots from your device"
        group = ScreenshotsPlugin.GROUP
    }

    override fun init(variant: TestVariant, extension: ScreenshotsPluginExtension) {
        super.init(variant, extension)
        val output = variant.outputs.find { it is ApkVariantOutput } as? ApkVariantOutput
                ?: throw IllegalArgumentException("Can't find APK output")
        val packageTask = variant.packageApplicationProvider.orNull
                ?: throw IllegalArgumentException("Can't find package application provider")

        apkPath = File(packageTask.outputDirectory.asFile.get(), output.outputFileName)
        bundleResults = extension.bundleResults
        testRunId = extension.testRunId
    }

    @TaskAction
    fun pullScreenshots() {
        val codeSource = ScreenshotsPlugin::class.java.protectionDomain.codeSource
        val jarFile = File(codeSource.location.toURI().path)
        val isVerifyOnly = verify && extension.referenceDir != null

        val outputDir = if (isVerifyOnly) {
            File(extension.referenceDir)
        } else {
            getReportDir(project, variant)
        }

        assert(if (isVerifyOnly) outputDir.exists() else !outputDir.exists())

        project.exec {
            it.executable = extension.pythonExecutable
            it.environment("PYTHONPATH", jarFile)

            it.args = mutableListOf(
                "-m",
                "android_screenshot_tests.pull_screenshots",
                "--apk",
                apkPath.absolutePath,
                "--test-run-id",
                testRunId,
                "--temp-dir",
                outputDir.absolutePath
            ).apply {
                if (verify) {
                    add("--verify")
                } else if (record) {
                    add("--record")
                }

                if (extension.variantRecord) {
                    if (verify || record) {
                        add(extension.recordDir + variant.flavorName.toLowerCase() + "/")
                    }
                } else {
                    if (verify || record) {
                        add(extension.recordDir)
                    }
                }

                if (verify && extension.failureDir != null) {
                    add("--failure-dir")
                    add("${extension.failureDir}")
                }

                if (extension.multipleDevices) {
                    add("--multiple-devices")
                    add("${extension.multipleDevices}")
                }

                if (isVerifyOnly) {
                    add("--no-pull")
                }

                if (bundleResults) {
                    add("--bundle-results")
                }

                if (keepOldRecord) {
                    add("--keep-old-record")
                }
            }

            println(it.args)
        }
    }
}
