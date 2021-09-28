package com.agoda.testing.screenshot.build

import com.android.build.gradle.api.TestVariant
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input

open class ScreenshotTask : DefaultTask() {

    @Input protected lateinit var extension: ScreenshotsPluginExtension
    @Input protected lateinit var variant: TestVariant

  open fun init(variant: TestVariant, extension: ScreenshotsPluginExtension) {
      this.extension = extension
      this.variant = variant
  }
}
