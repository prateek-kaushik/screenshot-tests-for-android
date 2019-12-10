/**
 * Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
 *
 * <p>This source code is licensed under the license found in the LICENSE-examples file in the root
 * directory of this source tree.
 */
package com.github.kajornsak.screenshot.sample;

import android.content.Context;
import android.view.LayoutInflater;
import androidx.test.platform.app.InstrumentationRegistry;
import com.facebook.litho.LithoView;
import com.github.kajornsak.screenshot.Screenshot;
import com.github.kajornsak.screenshot.ViewHelpers;
import org.junit.Test;

public class ImageRowScreenshotTest {
  @Test
  public void testDefault() {
    Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    LayoutInflater inflater = LayoutInflater.from(targetContext);
    LithoView view = (LithoView) inflater.inflate(R.layout.litho_view, null, false);

    view.setComponent(ImageRow.create(view.getComponentContext()).build());

    ViewHelpers.setupView(view).setExactWidthDp(300).layout();
    Screenshot.snap(view).record();
  }
}
