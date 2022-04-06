package com.agoda.testing.screenshot.build

import com.android.build.gradle.api.TestVariant

open class VerifyScreenshotTestTask : RunScreenshotTestTask() {
  companion object {
    fun taskName(variant: TestVariant) = "verify${variant.name.capitalize()}ScreenshotTest"
  }

  init {
    description =
        "Installs and runs screenshot tests, then verifies their output against previously recorded screenshots"
    group = ScreenshotsPlugin.GROUP
    verify = true
  }
}