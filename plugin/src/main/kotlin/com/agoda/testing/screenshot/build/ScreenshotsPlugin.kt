package com.agoda.testing.screenshot.build

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.ApkVariantOutput
import com.android.build.gradle.api.TestVariant
import com.android.build.gradle.internal.tasks.factory.dependsOn
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import java.util.*

open class ScreenshotsPluginExtension {
  /** The directory to store recorded screenshots in */
  var recordDir = "screenshots"
  /** Whether to have the plugin dependency automatically add the core dependency */
  var addDeps = true
  /** Whether to store screenshots in device specific folders */
  var multipleDevices = false
  /** The python executable to use */
  var pythonExecutable = "python"
  /** The directory to compare screenshots from in verify only mode */
  var referenceDir: String? = null
  /** The directory to save failed screenshots */
  var failureDir: String? = null

  /** Whether to tar the screenshots in an archive file to transfer */
  var bundleResults = false

  var testRunId: String = UUID.randomUUID().toString()
  /** Using variant name as subdirectory for reference images */
  var variantRecord = false
}

class ScreenshotsPlugin : Plugin<Project> {
  companion object {
    const val GROUP = "Screenshot Test"
    const val DEPENDENCY_GROUP = "com.facebook.testing.screenshot"
    const val DEPENDENCY_CORE = "core"
  }

  private lateinit var screenshotExtensions: ScreenshotsPluginExtension

  override fun apply(project: Project) {
    val extensions = project.extensions
    val plugins = project.plugins
    screenshotExtensions = extensions.create("screenshots", ScreenshotsPluginExtension::class.java)

    project.afterEvaluate {
      if (screenshotExtensions.addDeps) {
        it.dependencies.add("androidTestImplementation", "$DEPENDENCY_GROUP:$DEPENDENCY_CORE:0.15.0")
      }
    }

    val variants = when {
      plugins.hasPlugin("com.android.application") ->
        extensions.findByType(AppExtension::class.java)!!.testVariants
      plugins.hasPlugin("com.android.library") ->
        extensions.findByType(LibraryExtension::class.java)!!.testVariants
      else -> throw IllegalArgumentException("Screenshot Test plugin requires Android's plugin")
    }

    variants.all { generateTasksFor(project, it) }
  }

  private fun <T : ScreenshotTask> registerTask(
      project: Project,
      name: String,
      variant: TestVariant,
      clazz: Class<T>
  ): TaskProvider<T> {
    return project.tasks.register(name, clazz).apply {
      configure { it.init(variant, screenshotExtensions) }
    }
  }

  private fun generateTasksFor(project: Project, variant: TestVariant) {
    variant.outputs.all {
      if (it is ApkVariantOutput) {
        val cleanScreenshots = registerTask(
            project,
            CleanScreenshotsTask.taskName(variant),
            variant,
            CleanScreenshotsTask::class.java)
        registerTask(
            project,
            PullScreenshotsTask.taskName(variant),
            variant,
            PullScreenshotsTask::class.java).dependsOn(cleanScreenshots)

        registerTask(
            project,
            RunScreenshotTestTask.taskName(variant),
            variant,
            RunScreenshotTestTask::class.java)

        registerTask(
            project,
            RecordScreenshotTestTask.taskName(variant),
            variant,
            RecordScreenshotTestTask::class.java)

        registerTask(
            project,
            SingleRecordScreenshotTestTask.taskName(variant),
            variant,
            SingleRecordScreenshotTestTask::class.java)

        registerTask(
            project,
            VerifyScreenshotTestTask.taskName(variant),
            variant,
            VerifyScreenshotTestTask::class.java)
      }
    }
  }
}
