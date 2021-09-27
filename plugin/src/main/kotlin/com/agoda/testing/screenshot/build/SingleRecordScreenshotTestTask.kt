package com.agoda.testing.screenshot.build

import com.android.build.gradle.api.TestVariant


open class SingleRecordScreenshotTestTask : RunScreenshotTestTask() {

  companion object {
    fun taskName(variant: TestVariant) = "singleRecord${variant.name.capitalize()}ScreenshotTest"
  }

  init {
    description = "Installs and runs screenshot tests, then records their output for later verification." +
        "This task doesn't clean the output directory before recording."
    group = ScreenshotsPlugin.GROUP
    keepOldRecord = true
  }

  override fun init(variant: TestVariant, extension: ScreenshotsPluginExtension) {
    super.init(variant, extension)
    record = true
  }
}
