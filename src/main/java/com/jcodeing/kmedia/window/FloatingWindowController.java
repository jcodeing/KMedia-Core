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
package com.jcodeing.kmedia.window;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import androidx.annotation.NonNull;
import com.jcodeing.kmedia.utils.Metrics;
import com.jcodeing.kmedia.utils.OS;

public abstract class FloatingWindowController {

  protected Context context;

  public FloatingWindowController(@NonNull Context context) {
    this.context = context;
    floatingWindow = new FloatingWindow(context);
  }

  // =========@Window@=========
  protected FloatingWindow floatingWindow;

  private boolean isShown;

  public boolean isShown() {
    return isShown;
  }

  /**
   * internal call {@link #getFloatingWindowView()} -> {@link #configFloatingWindowViewParams()}
   */
  public boolean show() {
    if (isShown) {
      return true;
    }

    FloatingWindowView floatingWindowView = getFloatingWindowView();
    if (floatingWindowView == null || floatingWindowView.isShown()) {
      return false;
    }

    LayoutParams layoutParams = configFloatingWindowViewParams();
    floatingWindow.addView(floatingWindowView, layoutParams);
    isShown = true;
    return true;
  }

  /**
   * internal call {@link #getFloatingWindowView()}
   */
  public boolean hide() {
    if (!isShown) {
      onHide();
      return true;
    }

    FloatingWindowView floatingWindowView = getFloatingWindowView();
    if (floatingWindowView == null || !floatingWindowView.isShown()) {
      return false;
    }

    floatingWindow.removeView(floatingWindowView);
    isShown = false;
    onHide();
    return true;
  }

  public abstract FloatingWindowView getFloatingWindowView();


  // ============================@LayoutParams@============================
  protected WindowManager.LayoutParams layoutParams;

  protected int width = 0;
  protected int height = 0;
  protected int x = 0;
  protected int y = 0;
  protected int gravity = Gravity.NO_GRAVITY;

  public void setFloatingWindowSize(int width, int height) {
    this.width = width;
    this.height = height;
    if (isShown) {
      floatingWindow.setLayoutParamsSize(width, height);
    }
  }

  public void setFloatingWindowXY(int x, int y) {
    this.x = x;
    this.y = y;
    if (isShown) {
      floatingWindow.setLayoutParamsXY(x, y, false, false);
    }
  }

  /**
   * Set Floating Window gravity, before the show.
   *
   * @param gravity {@link LayoutParams#gravity}
   */
  public FloatingWindowController setGravity(int gravity) {
    this.gravity = gravity;
    return this;
  }

  public WindowManager.LayoutParams configFloatingWindowViewParams() {
    if (layoutParams == null) {
      layoutParams = new WindowManager.LayoutParams();

      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT || OS.i().isMIUI()) {
        //<!--Using WindowManager.LayoutParams.TYPE_PHONE For Floating　Window　View-->
        //<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
        layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;//2002
      } else {
        layoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;//2005
      }
      layoutParams.format = PixelFormat.RGBA_8888;
    }

    layoutParams.width =
        width != 0 ? width : Metrics.widthPx(context) - Metrics.dp2px(context, 77f);
    layoutParams.height =
        height != 0 ? height : Metrics.dp2px(context, 200f);

    layoutParams.gravity = gravity;
    layoutParams.x = x;
    layoutParams.y = y;

    layoutParams.flags =
        LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE; //40(32|8)

    /*layoutParams.flags =
        LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE |
            LayoutParams.FLAG_NOT_TOUCHABLE; //56(32|8|16)*/
    return layoutParams;
  }

  // ============================@Listener@============================
  protected Listener listener;

  public void setListener(Listener listener) {
    this.listener = listener;
  }

  public interface Listener {

    void onHide(FloatingWindowController controller);
  }

  protected void onHide() {
    if (listener != null) {
      listener.onHide(this);
    }
  }
}