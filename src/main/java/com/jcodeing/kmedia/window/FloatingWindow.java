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
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import androidx.annotation.NonNull;
import com.jcodeing.kmedia.assist.C;
import com.jcodeing.kmedia.utils.Assert;

public class FloatingWindow {

  private WindowManager windowManager;

  public FloatingWindow(@NonNull Context context) {
    windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
  }

  // ============================@View@============================
  private FloatingWindowView view;

  /**
   * set a floating window view for floating window
   */
  public void setView(FloatingWindowView view) {
    if (view != this.view) {
      this.view = view;
      if (this.view != null) {
        this.view.onSet(this);
      }
    }
  }

  /**
   * Add from {@link #setView(FloatingWindowView)}->view of to the window <p>At the same time need
   * from {@link #setLayoutParams(LayoutParams)}->layoutParams</p>
   */
  public void addView() {
    if (view == null || layoutParams == null) {
      return;
    }
    try {
      windowManager.addView(view, layoutParams);
      view.onAdded();
    } catch (Exception e) {
      //Do nothing
    }
  }

  /**
   * method internal operation
   * <pre>
   * setView(view);
   * setLayoutParams(layoutParams);
   * addView();
   * </pre>
   *
   * @param view Floating Window View
   * @param layoutParams Window Manager Layout Params
   */
  public void addView(FloatingWindowView view, WindowManager.LayoutParams layoutParams) {
    setView(view);
    setLayoutParams(layoutParams);
    addView();
  }

  /**
   * Update from {@link #setView(FloatingWindowView)}->view of to the window <p>At the same time
   * need from {@link #setLayoutParams(LayoutParams)}->layoutParams</p>
   */
  public void updateViewLayout() {
    if (view == null || layoutParams == null) {
      return;
    }
    try {
      windowManager.updateViewLayout(view, layoutParams);
      view.onUpdated();
    } catch (Exception e) {//IllegalArgument
      try {
        windowManager.addView(view, layoutParams);
      } catch (Exception ee) {
        //Do nothing
      }
    }
  }

  public void removeView(FloatingWindowView view) {
    if (view == null) {
      return;
    }
    try {
      windowManager.removeView(view);
      view.onRemoved();
    } catch (Exception e) {
      //Do nothing
    }
  }

  /**
   * Remove from {@link #setView(FloatingWindowView)}->view of to the window
   */
  public void removeView() {
    removeView(view);
    view = null;
  }

  // ============================@Layout Params@============================
  private WindowManager.LayoutParams layoutParams;

  /**
   * @param layoutParams The LayoutParams to assign to view.
   */
  public void setLayoutParams(LayoutParams layoutParams) {
    if (layoutParams != this.layoutParams) {
      this.layoutParams = layoutParams;
      checkMinMaxWidthHeight();
    }
  }

  public WindowManager.LayoutParams getLayoutParams() {
    return layoutParams;
  }

  public int getLayoutParamsY() {
    return layoutParams != null ? layoutParams.y : 0;
  }

  /**
   * WARNING: is ascending, x/y !=0 go job
   */
  public void setLayoutParamsXY(int x, int y, boolean xIsAscending, boolean yIsAscending) {
    if (layoutParams != null) {

      boolean isValid = false;
      if (xIsAscending) {
        if (x != 0) {
          layoutParams.x += x;
          isValid = true;
        }
      } else {
        layoutParams.x = x;
        isValid = true;
      }

      if (yIsAscending) {
        if (y != 0) {
          layoutParams.y += y;
          isValid = true;
        }
      } else {
        layoutParams.y = y;
        isValid = true;
      }

      if (isValid) {
        updateViewLayout();
      }
    }
  }

  // ============================@Size
  private int minWidth = 111;
  private int minHeight = 111;
  private int maxWidth = -1;//unset (maxWidth < minWidth)
  private int maxHeight = -1;//unset (maxHeight < minHeight)

  /**
   * *param all can use {@link C#PARAM_ORIGINAL} keeping the original values <p />*param all can use
   * {@link C#PARAM_UNSET}/{@link C#PARAM_RESET} unset/reset values
   */
  public void setMinWidthHeight(int minWidth, int minHeight) {
    //Support use C.PARAM_RESET constant reset values.
    if (minWidth == C.PARAM_RESET) {
      this.minWidth = 111;
    }
    if (minHeight == C.PARAM_RESET) {
      this.minHeight = 111;
    }
    //Support use C.PARAM_ORIGINAL constant keeping the original values
    if (minWidth != C.PARAM_ORIGINAL) {
      this.minWidth = minWidth;
    }
    if (minHeight != C.PARAM_ORIGINAL) {
      this.minHeight = minHeight;
    }
  }

  /**
   * *param all can use {@link C#PARAM_ORIGINAL} keeping the original values <p />*param all can use
   * {@link C#PARAM_UNSET}/{@link C#PARAM_RESET} unset/reset values
   */
  public void setMaxWidthHeight(int maxWidth, int maxHeight) {
    //Support use C.PARAM_RESET constant reset values.
    if (maxWidth == C.PARAM_RESET) {
      this.maxWidth = -1;
    }
    if (maxHeight == C.PARAM_RESET) {
      this.maxHeight = -1;
    }
    //Support use C.PARAM_ORIGINAL constant keeping the original values
    if (maxWidth != C.PARAM_ORIGINAL) {
      this.maxWidth = maxWidth;
    }
    if (maxHeight != C.PARAM_ORIGINAL) {
      this.maxHeight = maxHeight;
    }
  }

  public void checkMinMaxWidthHeight() {
    if (layoutParams != null) {
      if (Assert.checkNotSpecialWidthHeight(layoutParams.width)) {
        if (layoutParams.width < minWidth) {
          layoutParams.width = minWidth;
        } else if (layoutParams.width > maxWidth && maxWidth > minWidth) {
          layoutParams.width = maxWidth;//&& !unset
        }
      }
      if (Assert.checkNotSpecialWidthHeight(layoutParams.height)) {
        if (layoutParams.height < minHeight) {
          layoutParams.height = minHeight;
        } else if (layoutParams.height > maxHeight && maxHeight > minHeight) {
          layoutParams.height = maxHeight;//&& !unset
        }
      }
    }
  }

  public void setLayoutParamsSize(int width, int height) {
    if (layoutParams != null) {
      layoutParams.width = width;
      layoutParams.height = height;
      checkMinMaxWidthHeight();
      updateViewLayout();
    }
  }

  /**
   * WARNING: param width/height not support {@link LayoutParams#MATCH_PARENT} or {@link
   * LayoutParams#WRAP_CONTENT}, see {@link #setLayoutParamsSize(int, int)} support.
   *
   * @param width relative width (!=0 go job)
   * @param height relative height (!=0 go job)
   * @param widthIsAscending base on origin width ascending (originWidth += width)
   * @param heightIsAscending base on origin height ascending (originHeight += height)
   */
  public void setLayoutParamsSize(int width, int height, boolean widthIsAscending,
      boolean heightIsAscending) {
    if (layoutParams != null) {
      // =========@width@=========
      if (width != 0) {
        if (widthIsAscending) {
          layoutParams.width += width;
        } else {
          if (width > 0) {
            layoutParams.width = width;
          }
        }
        checkMinMaxWidthHeight();
      }
      // =========@height@=========
      if (height != 0) {
        if (heightIsAscending) {
          layoutParams.height += height;
        } else {
          if (height > 0) {
            layoutParams.height = height;
          }
        }
        checkMinMaxWidthHeight();
      }
      updateViewLayout();
    }
  }

  public void setLayoutParamsFlags(int flags) {
    if (getLayoutParams() != null) {
      getLayoutParams().flags = flags;
      updateViewLayout();
    }
  }
}