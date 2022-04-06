package com.agoda.testing.screenshot.build

import com.android.build.gradle.api.TestVariant

open class RecordScreenshotTestTask : RunScreenshotTestTask() {
  companion object {
    fun taskName(variant: TestVariant) = "record${variant.name.capitalize()}ScreenshotTest"
  }

  init {
    description =
        "Installs and runs screenshot tests, then records their output for later verification"
    group = ScreenshotsPlugin.GROUP
  }

  override fun init(variant: TestVariant, extension: ScreenshotsPluginExtension) {
    super.init(variant, extension)
    record = true
  }
}