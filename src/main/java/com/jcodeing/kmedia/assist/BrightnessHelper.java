/*
 * Copyright (c) 2017 K Sun <jcodeing@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jcodeing.kmedia.assist;

import android.support.annotation.NonNull;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import com.jcodeing.kmedia.utils.Assert;

public class BrightnessHelper {

  /**
   * Sets the brightness of the screen.
   *
   * @param screenBrightness 0 to 1 adjusts the brightness from dark to full bright. <p /> less than
   * 0, the default({@link LayoutParams#BRIGHTNESS_OVERRIDE_NONE}), means to use the preferred
   * screen brightness.
   * @param isBrightnessIncrement is brightness increment (+origin)[0.01f <= screenBrightness <=
   * 1.0f]
   * @return deal screen brightness
   */
  public static float setBrightness(@NonNull Window window,
      float screenBrightness, boolean isBrightnessIncrement) {
    if (isBrightnessIncrement && screenBrightness != 0) {
      LayoutParams attr = window.getAttributes();//+origin
      screenBrightness = Assert.reviseInterval(attr.screenBrightness + screenBrightness,
          0.01f, 1.0f, false, false);//0.01f <= screenBrightness <= 1.0f
      if (attr.screenBrightness != screenBrightness) {
        attr.screenBrightness = screenBrightness;
        window.setAttributes(attr);
      }
      return screenBrightness;
    } else if (!isBrightnessIncrement) {
      LayoutParams attr = window.getAttributes();
      //do not revise, because need support BRIGHTNESS_OVERRIDE_NONE(-1.0f).
      if (attr.screenBrightness != screenBrightness) {
        attr.screenBrightness = screenBrightness;
        window.setAttributes(attr);
      }
      return screenBrightness;
    }
    return screenBrightness;
  }
}