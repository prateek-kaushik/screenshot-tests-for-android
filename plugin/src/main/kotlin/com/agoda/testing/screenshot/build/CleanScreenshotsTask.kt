package com.agoda.testing.screenshot.build

import com.android.build.gradle.api.TestVariant
import org.gradle.api.tasks.TaskAction

open class CleanScreenshotsTask : ScreenshotTask() {
    companion object {
        fun taskName(variant: TestVariant) = "clean${variant.name.capitalize()}Screenshots"
    }

    init {
        description = "Clean last generated screenshot report"
        group = ScreenshotsPlugin.GROUP
    }

    @TaskAction
    fun cleanScreenshots() {
        val outputDir = PullScreenshotsTask.getReportDir(project, variant)
        project.delete(outputDir)
    }
}
