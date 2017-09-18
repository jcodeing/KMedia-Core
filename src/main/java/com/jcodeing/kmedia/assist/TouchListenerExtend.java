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

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;

public abstract class TouchListenerExtend implements OnTouchListener {

  /**
   * Whether floating window locked, if is locked will not be able to touch slide
   */
  public boolean isLocked;

  /**
   * Moving first threshold <p>Distance in pixels a touch can wander before we think the user is
   * scrolling</p>
   */
  protected int scaledTouchSlop;

  public TouchListenerExtend(Context context) {
    // =========@Init@=========
    scaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    if (scaledTouchSlop != 0) {
      //Micro adjustment [scaledTouchSlop/2]
      scaledTouchSlop >>= 1;
    }
    if (scaledTouchSlop < 5) {
      //The minimum limit
      scaledTouchSlop = 5;
    }
  }

  protected float nowX;
  protected float nowY;
  protected float downX;
  protected float downY;
  protected float lastX;
  protected float lastY;

  protected boolean isValidMove;
  protected boolean isMoving;

  @Override
  public boolean onTouch(View v, MotionEvent event) {
    boolean hasConsumed = false;
    if (!isLocked) {
      int action = event.getAction();
      if (action == MotionEvent.ACTION_DOWN) {
        // =========@Action Down@=========
        downX = nowX = event.getRawX();
        downY = nowY = event.getRawY();
        hasConsumed = onTouchDown(v, event);
      } else if (action == MotionEvent.ACTION_MOVE) {
        // =========@Action Move@=========
        nowX = event.getRawX();
        nowY = event.getRawY();
        if (isMoving ||
            Math.abs(downX - nowX) >= scaledTouchSlop ||
            Math.abs(downY - nowY) >= scaledTouchSlop) {
          isMoving = true;
          isValidMove = hasConsumed = onTouchMove(v, event);
        } else {
          return true;
        }
      } else if (action == MotionEvent.ACTION_UP) {
        // =========@Action Up@=========
        nowX = event.getRawX();
        nowY = event.getRawY();
        isMoving = false;
        hasConsumed = onTouchUp(v, event);
        isValidMove = false;
      }
      lastX = nowX;
      lastY = nowY;
    }
    return hasConsumed;
  }

  protected abstract boolean onTouchDown(View v, MotionEvent event);

  /**
   * First of all by moving first threshold {@link #scaledTouchSlop} go into moving state, callback
   * <p>You can add in your code second threshold, to reduce the unnecessary cost</p>
   * <pre>
   * ...
   * if (Math.abs(lastX - nowX) >= 1 || Math.abs(lastY - nowY) >= 1) {
   *   //Do something
   * }
   * ...
   * </pre>
   */
  protected abstract boolean onTouchMove(View v, MotionEvent event);

  protected abstract boolean onTouchUp(View v, MotionEvent event);

}