package com.agoda.testing.screenshot.build

import com.android.build.gradle.api.TestVariant
import com.agoda.testing.screenshot.generated.ScreenshotTestBuildConfig

open class RunScreenshotTestTask : PullScreenshotsTask() {
  companion object {
    fun taskName(variant: TestVariant) = "run${variant.name.capitalize()}ScreenshotTest"
  }

  init {
    description = "Installs and runs screenshot tests, then generates a report"
    group = ScreenshotsPlugin.GROUP
  }

  override fun init(variant: TestVariant, extension: ScreenshotsPluginExtension) {
    super.init(variant, extension)

    if (verify && extension.referenceDir != null) {
      return
    }

    dependsOn(variant.connectedInstrumentTestProvider)
    mustRunAfter(variant.connectedInstrumentTestProvider)
  }
}
