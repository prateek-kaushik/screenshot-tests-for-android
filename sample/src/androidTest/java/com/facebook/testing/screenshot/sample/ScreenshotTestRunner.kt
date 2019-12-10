/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the
 * LICENSE-examples file in the root directory of this source tree.
 */

package com.github.kajornsak.screenshot.sample

import android.os.Bundle
import androidx.test.runner.AndroidJUnitRunner
import com.facebook.litho.config.ComponentsConfiguration
import com.github.kajornsak.screenshot.ScreenshotRunner
import com.github.kajornsak.screenshot.layouthierarchy.LayoutHierarchyDumper
import com.github.kajornsak.screenshot.layouthierarchy.litho.LithoAttributePlugin
import com.github.kajornsak.screenshot.layouthierarchy.litho.LithoHierarchyPlugin

class ScreenshotTestRunner : AndroidJUnitRunner() {
  companion object {
    init {
      ComponentsConfiguration.isDebugModeEnabled = true
      LayoutHierarchyDumper.addGlobalHierarchyPlugin(LithoHierarchyPlugin.getInstance())
      LayoutHierarchyDumper.addGlobalAttributePlugin(LithoAttributePlugin.getInstance())
    }
  }

  override fun onCreate(arguments: Bundle) {
    ScreenshotRunner.onCreate(this, arguments)
    super.onCreate(arguments)
  }

  override fun finish(resultCode: Int, results: Bundle) {
    ScreenshotRunner.onDestroy()
    super.finish(resultCode, results)
  }
}

