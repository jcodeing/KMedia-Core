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

import android.view.MotionEvent;

/**
 * Gesture Detector Helper. <p /> Assist GestureListenerExtend
 */
public class GestureDetectorHelper {

  public interface IGestureListenerExtend {

    boolean onTouchEvent(MotionEvent e);

    /**
     * @param e1 first MotionEvent
     * @param e2 current MotionEvent
     * @param e3 last MotionEvent
     */
    boolean onScrollLongitudinal(MotionEvent e1, MotionEvent e2, MotionEvent e3,
        float distanceX, float distanceY);

    /**
     * @param e1 first MotionEvent
     * @param e2 current MotionEvent
     * @param e3 last MotionEvent
     */
    boolean onScrollLongitudinalLeft(MotionEvent e1, MotionEvent e2, MotionEvent e3,
        float distanceX, float distanceY);

    /**
     * @param e1 first MotionEvent
     * @param e2 current MotionEvent
     * @param e3 last MotionEvent
     */
    boolean onScrollLongitudinalRight(MotionEvent e1, MotionEvent e2, MotionEvent e3,
        float distanceX, float distanceY);

    /**
     * @param e1 first MotionEvent
     * @param e2 current MotionEvent
     * @param e3 last MotionEvent
     */
    boolean onScrollCrosswise(MotionEvent e1, MotionEvent e2, MotionEvent e3,
        float distanceX, float distanceY);

    boolean onDoubleClick(MotionEvent e);

    boolean onSingleClick(MotionEvent e);
  }

  public interface IGestureListenerExtendProxy extends IGestureListenerExtend {
    //Gesture listener extend proxy
  }

  public static class SimpleGestureListenerExtendProxy implements IGestureListenerExtendProxy {

    @Override
    public boolean onScrollLongitudinal(MotionEvent e1, MotionEvent e2, MotionEvent e3,
        float distanceX, float distanceY) {
      return false;
    }

    @Override
    public boolean onScrollLongitudinalLeft(MotionEvent e1, MotionEvent e2, MotionEvent e3,
        float distanceX, float distanceY) {
      return false;
    }

    @Override
    public boolean onScrollLongitudinalRight(MotionEvent e1, MotionEvent e2, MotionEvent e3,
        float distanceX, float distanceY) {
      return false;
    }

    @Override
    public boolean onScrollCrosswise(MotionEvent e1, MotionEvent e2, MotionEvent e3,
        float distanceX, float distanceY) {
      return false;
    }

    @Override
    public boolean onDoubleClick(MotionEvent e) {
      return false;
    }

    @Override
    public boolean onSingleClick(MotionEvent e) {
      return false;
    }

    /**
     * @return true/false: enable/disable super handle
     */
    @Override
    public boolean onTouchEvent(MotionEvent e) {
      return false;
    }
  }
}