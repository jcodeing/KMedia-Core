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
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.jcodeing.kmedia.assist.TouchListenerExtend;
import com.jcodeing.kmedia.utils.Assert;
import com.jcodeing.kmedia.utils.Metrics;
import com.jcodeing.kmedia.view.LocalFrameLayout;

public class FloatingWindowView extends LocalFrameLayout {

  public FloatingWindowView(Context context) {
    super(context);
  }

  public FloatingWindowView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public FloatingWindowView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void initialize(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super.initialize(attrs, defStyleAttr, defStyleRes);
    displayHeight = Metrics.heightPx(getContext());
    displayWidth = Metrics.widthPx(getContext());
  }

  @Override
  protected void initConfig() {
    initConfigGetDragLocationView().setOnTouchListener(initConfigGetDragLocationTouchListener());
    if (initConfigGetDragSizeView() != null) {
      initConfigGetDragSizeView().setOnTouchListener(initConfigGetDragSizeTouchListener());
    }
  }

  @Override
  protected void onConfigurationChanged(Configuration newConfig) {
    if (newConfig == null || newConfig.orientation == Configuration.ORIENTATION_UNDEFINED) {
      return;
    }//changed based on orientation
    displayHeight = Metrics.heightPx(getContext());
    displayWidth = Metrics.widthPx(getContext());
    WindowManager.LayoutParams layoutParams;
    if (floatingWindow != null && (layoutParams = floatingWindow.getLayoutParams()) != null) {
      int width = -1, height = -1;//unset -1
      if (Assert.checkNotSpecialWidthHeight(layoutParams.width)) {
        //noinspection SuspiciousNameCombination    //0 <= width <= displayWidth
        width = Assert.reviseInterval(layoutParams.width, 0, displayWidth, false, false);
      }
      if (Assert.checkNotSpecialWidthHeight(layoutParams.height)) {
        //noinspection SuspiciousNameCombination    //0 <= height <= displayHeight
        height = Assert.reviseInterval(layoutParams.height, 0, displayHeight, false, false);
      }
      if ((width != -1 || height != -1) &&//!unset && !origin
          (layoutParams.width != width || layoutParams.height != height)) {
        floatingWindow.setLayoutParamsSize(
            width != -1 ? width : layoutParams.width,
            height != -1 ? height : layoutParams.height);
      }
    }
  }

  /**
   * Screen display height <p /> changed based on orientation(onConfigurationChanged), value will
   * update
   */
  protected int displayHeight;
  /**
   * Screen display width <p />changed based on orientation(onConfigurationChanged), value will
   * update
   */
  protected int displayWidth;

  // ============================@Drag View@============================
  protected View initConfigGetDragLocationView() {
    return this;
  }

  protected OnTouchListener initConfigGetDragLocationTouchListener() {
    return new DragLocationViewTouch(getContext());
  }

  protected class DragLocationViewTouch extends LocalTouchListenerExtend {

    public DragLocationViewTouch(Context context) {
      super(context);
    }

    private int[] locationOnScreen = new int[2];

    @Override
    protected boolean onTouchMove(View v, MotionEvent event) {
      if (floatingWindow == null) {
        return true;
      }

      int dealX = (int) (nowX - lastX);
      int dealY = (int) (nowY - lastY);

      // =========@Second threshold@=========
      if (Math.abs(dealX) >= 1 || Math.abs(dealY) >= 1) {

        // =========@boundary treatment
        getLocationOnScreen(locationOnScreen);

        boolean isValid = false;
        if (dealX > 0) {//→ right
          //view right location[x] -> locationOnScreen[0] + getRight()
          if (locationOnScreen[0] + getRight() + dealX > displayWidth) {
            dealX = 0;//invalid
          } else {
            isValid = true;
          }
        } else if (dealX < 0) {//← left
          //view left location[x] -> locationOnScreen[0]
          if (locationOnScreen[0] + dealX < 0) {
            dealX = 0;
          } else {
            isValid = true;
          }
        }
        if (dealY > 0) {//↓ bottom
          //view bottom location[y] -> locationOnScreen[1] + getBottom()
          if (locationOnScreen[1] + getBottom() + dealY > displayHeight) {
            dealY = 0;
          } else {
            isValid = true;
          }
        } else if (dealY < 0) {//↑ top
          //view top location[y] -> locationOnScreen[1]
          if (locationOnScreen[1] + dealY < 0) {
            dealY = 0;
          } else {
            isValid = true;
          }
        }

        if (isValid) {
          floatingWindow.setLayoutParamsXY(dealX, dealY, true, true);
        }
      }

      return true;
    }

  }

  protected View initConfigGetDragSizeView() {
    return null;
  }

  protected OnTouchListener initConfigGetDragSizeTouchListener() {
    return new DragSizeViewTouch(getContext());
  }

  protected class DragSizeViewTouch extends LocalTouchListenerExtend {

    public DragSizeViewTouch(Context context) {
      super(context);
    }

    @Override
    protected boolean onTouchMove(View v, MotionEvent event) {
      if (floatingWindow == null) {
        return true;
      }

      int dealX = (int) (nowX - lastX);
      int dealY = (int) (nowY - lastY);

      // =========@Second threshold@=========
      if (Math.abs(dealX) >= 1 || Math.abs(dealY) >= 1) {
        setViewSize(dealX, -dealY, true, true);
      }

      return true;
    }

    protected void setViewSize(int width, int height, boolean widthIsAscending,
        boolean heightIsAscending) {
      floatingWindow.setLayoutParamsSize(width, height, widthIsAscending, heightIsAscending);
    }
  }

  // ============================@Callback@============================
  protected FloatingWindow floatingWindow;

  /**
   * this view has been set to-> floating window
   *
   * @param floatingWindow floating Window
   * @see FloatingWindow
   */
  protected void onSet(FloatingWindow floatingWindow) {
    this.floatingWindow = floatingWindow;
  }

  /**
   * this. view has been added
   *
   * @see FloatingWindow
   * @see android.view.ViewManager#addView(View, ViewGroup.LayoutParams)
   */
  public void onAdded() {
    //Do nothing
  }

  /**
   * this. view has been updated
   *
   * @see FloatingWindow
   * @see android.view.ViewManager#updateViewLayout(View, ViewGroup.LayoutParams)
   */
  public void onUpdated() {
    //Do nothing
  }

  /**
   * this. view has been removed
   *
   * @see FloatingWindow
   * @see android.view.ViewManager#removeView(View)
   */
  protected void onRemoved() {
    //Do nothing
  }

  // ============================@Assist@============================
  protected abstract class LocalTouchListenerExtend extends TouchListenerExtend {

    //Not valid move handle (dispatch to the child view)
    MotionEvent simulationDownEvent;
    boolean handleSimulationEvent;

    LocalTouchListenerExtend(Context context) {
      super(context);
    }

    @Override
    protected boolean onTouchDown(View v, MotionEvent event) {
      if (handleSimulationEvent) {
        return handleSimulationEvent = false;
      } else {//save downEvent for simulation handle
        simulationDownEvent = MotionEvent.obtain(event);
        //dispatchTouchEvent(.)->dispatchTransformedTouchEvent(....) {
        //  internal e.offsetLocation(mScrollX - child.mLeft , mScrollY - child.mTop) }
        simulationDownEvent //location revert to original dispatch X coordinate of this event
            .setLocation(simulationDownEvent.getRawX() - dispatchEvRawOffsetX,
                simulationDownEvent.getRawY() - dispatchEvRawOffsetY);
        return true;//first return true, receive move event
      }
    }

    @Override
    protected boolean onTouchUp(View v, MotionEvent event) {
      if (isValidMove) {
        return true;
      } else {//handle simulation event
        handleSimulationEvent = true;
        dispatchTouchEvent(simulationDownEvent);
        dispatchTouchEvent(event);
        return false;
      }
    }
  }

  protected float dispatchEvRawOffsetX;
  protected float dispatchEvRawOffsetY;

  @Override
  public boolean dispatchTouchEvent(MotionEvent ev) {
    dispatchEvRawOffsetX = ev.getRawX() - ev.getX();
    dispatchEvRawOffsetY = ev.getRawY() - ev.getY();
    return super.dispatchTouchEvent(ev);
  }
}