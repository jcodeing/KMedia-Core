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
package com.jcodeing.kmedia.video;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import androidx.annotation.IntDef;
import com.jcodeing.kmedia.R;
import com.jcodeing.kmedia.view.LocalFrameLayout;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class AspectRatioView extends LocalFrameLayout {

  public AspectRatioView(Context context) {
    this(context, null);
  }

  public AspectRatioView(Context context, AttributeSet attrs) {
    super(context, attrs);
    resizeMode = RESIZE_MODE_FIT;
    if (attrs != null) {
      TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
          R.styleable.AspectRatioView, 0, 0);
      try {
        resizeMode = a
            .getInt(R.styleable.AspectRatioView_resize_mode,
                RESIZE_MODE_FIT);
      } finally {
        a.recycle();
      }
    }
  }

  // ============================@ResizeMode@============================
  private int resizeMode;

  @Retention(RetentionPolicy.SOURCE)
  @IntDef({RESIZE_MODE_FIT, RESIZE_MODE_FIXED_WIDTH, RESIZE_MODE_FIXED_HEIGHT, RESIZE_MODE_FILL})
  public @interface ResizeMode {

  }

  /**
   * Either the width or height is decreased to obtain the desired aspect ratio.
   */
  public static final int RESIZE_MODE_FIT = 0;
  /**
   * The width is fixed and the height is increased or decreased to obtain the desired aspect
   * ratio.
   */
  public static final int RESIZE_MODE_FIXED_WIDTH = 1;
  /**
   * The height is fixed and the width is increased or decreased to obtain the desired aspect
   * ratio.
   */
  public static final int RESIZE_MODE_FIXED_HEIGHT = 2;
  /**
   * The specified aspect ratio is ignored.
   */
  public static final int RESIZE_MODE_FILL = 3;

  /**
   * Sets the resize mode.
   *
   * @param resizeMode The resize mode.
   */
  public void setResizeMode(@ResizeMode int resizeMode) {
    if (this.resizeMode != resizeMode) {
      this.resizeMode = resizeMode;
      requestLayout();
    }
  }


  // ============================@Display Size Aspect Ratio@============================
  private int displayWidth;
  private int displayHeight;
  private float displayAspectRatio;

  /**
   * Set the aspect ratio that this view should satisfy. <ul> <li>width / height</li> <li>16.0f /
   * 9.0f</li> <li>4.0f / 3.0f</li> <li>.... / ....</li> </ul>
   *
   * @param aspectRatio The width to height ratio.
   */
  public void setAspectRatio(float aspectRatio) {
    if (resizeMode != RESIZE_MODE_FILL && //this mode aspect ratio is ignored.
        displayAspectRatio != aspectRatio) {
      displayAspectRatio = aspectRatio;
      requestLayout();
    }
  }

  /**
   * Set itself size (The last display size really depends {@link #setResizeMode(int)})
   *
   * @param width Want to display the width
   * @param height Want to display the height
   * @param aspectRatio Want to display the aspect ratio {@link #setAspectRatio(float)}
   */
  public void setSize(int width, int height, float aspectRatio) {
    if (resizeMode != RESIZE_MODE_FILL && //this mode aspect ratio is ignored.
        (displayWidth != width || displayHeight != height || displayAspectRatio != aspectRatio)) {
      displayWidth = width;
      displayHeight = height;
      displayAspectRatio = aspectRatio;
      requestLayout();
    }
  }

  /**
   * Set the display size (The last display size really depends {@link #setResizeMode(int)}) <p>*at
   * the same time with set default aspect ratio (width / height)</p>
   *
   * @param width Want to display the width
   * @param height Want to display the height
   */
  public void setSize(int width, int height) {
    setSize(width, height, (float) width / height);
  }

  /**
   * The {@link FrameLayout} will not resize itself if the fractional difference between its natural
   * aspect ratio and the requested aspect ratio falls below this threshold. <p> This tolerance
   * allows the view to occupy the whole of the screen when the requested aspect ratio is very
   * close, but not exactly equal to, the aspect ratio of the screen. This may reduce the number of
   * view layers that need to be composited by the underlying system, which can help to reduce power
   * consumption.
   */
  private static final float MAX_ASPECT_RATIO_DEFORMATION_FRACTION = 0.01f;

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    if (resizeMode == RESIZE_MODE_FILL || displayAspectRatio <= 0) {
      // Aspect ratio not set.
      return;
    }

    int width = getMeasuredWidth();
    int height = getMeasuredHeight();
    float viewAspectRatio = (float) width / height;
    float aspectDeformation = displayAspectRatio / viewAspectRatio - 1;
    if (Math.abs(aspectDeformation) <= MAX_ASPECT_RATIO_DEFORMATION_FRACTION) {
      // We're within the allowed tolerance.
      return;
    }

    switch (resizeMode) {
      case RESIZE_MODE_FIXED_WIDTH:
        height = (int) (width / displayAspectRatio);
        break;
      case RESIZE_MODE_FIXED_HEIGHT:
        width = (int) (height * displayAspectRatio);
        break;
      case RESIZE_MODE_FIT:
      default:
        if (aspectDeformation > 0) {
          width = Math.min(displayWidth, width);
          height = (int) (width / displayAspectRatio);
        } else {
          height = Math.min(displayHeight, height);
          width = (int) (height * displayAspectRatio);
        }
        break;
    }
    super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
        MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
  }
}