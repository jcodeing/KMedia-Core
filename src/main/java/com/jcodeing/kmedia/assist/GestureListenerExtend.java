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

import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Extend GestureDetector.SimpleOnGestureListener. <p /> Mainly to add on scroll
 * Longitudinal/Longitudinal
 */
public class GestureListenerExtend extends GestureDetector.SimpleOnGestureListener
    implements GestureDetectorHelper.IGestureListenerExtend {

  protected float xAxisMidpoint;

  public void setGestureAreaWidth(int width) {
    xAxisMidpoint = (float) width / 2;
  }

  /**
   * <ul> <li>0:unset <li>1:longitudinal <li>2:crosswise <ul/>
   */
  protected int scrollType = 0;
  /**
   * <ul> <li>11:left longitudinal <li>11:right longitudinal <ul/>
   */
  protected int scrollSubtype = 0;

  /**
   * last motion event
   */
  private MotionEvent e3;

  @Override
  public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
    if (scrollType == 1 ||
        (Math.abs(distanceY) > Math.abs(distanceX) && scrollType == 0)) {
      scrollType = 1;//longitudinal
      onScrollLongitudinal(e1, e2, e3 != null ? e3 : e1, distanceX, distanceY);
      if (scrollSubtype == 11 || (e1.getX() < xAxisMidpoint && scrollSubtype == 0)) {
        scrollSubtype = 11;//left
        onScrollLongitudinalLeft(e1, e2, e3 != null ? e3 : e1, distanceX, distanceY);
      } else if (scrollSubtype == 12 || (e1.getX() > xAxisMidpoint && scrollSubtype == 0)) {
        scrollSubtype = 12;//right
        onScrollLongitudinalRight(e1, e2, e3 != null ? e3 : e1, distanceX, distanceY);
      }
    } else if (scrollType == 2 ||
        (Math.abs(distanceY) < Math.abs(distanceX) && scrollType == 0)) {
      scrollType = 2;//crosswise
      onScrollCrosswise(e1, e2, e3 != null ? e3 : e1, distanceX, distanceY);
    }
    e3 = MotionEvent.obtain(e2);
    return true;
  }

  @Override
  public boolean onDoubleTap(MotionEvent e) {
    return onDoubleClick(e);
  }

  @Override
  public boolean onSingleTapConfirmed(MotionEvent e) {
    return onSingleClick(e);
  }

  @Override
  public boolean onDown(MotionEvent e) {
    scrollType = 0;
    scrollSubtype = 0;
    e3 = null;
    return true;
  }

  // ============================@Extend@============================

  /**
   * Proxy priority handle. <p /> SubClass override below code refer
   * <pre>
   * if(!super.onTouchEvent(e)){
   *    //Do something
   * }
   * </pre>
   */
  @Override
  public boolean onTouchEvent(MotionEvent e) {
    return gestureProxy != null &&
        gestureProxy.onTouchEvent(e);
  }

  @Override
  public boolean onScrollLongitudinal(MotionEvent e1, MotionEvent e2, MotionEvent e3,
      float distanceX, float distanceY) {
    return gestureProxy != null &&
        gestureProxy.onScrollLongitudinal(e1, e2, e3, distanceX, distanceY);
  }

  @Override
  public boolean onScrollLongitudinalLeft(MotionEvent e1, MotionEvent e2, MotionEvent e3,
      float distanceX, float distanceY) {
    return gestureProxy != null &&
        gestureProxy.onScrollLongitudinalLeft(e1, e2, e3, distanceX, distanceY);
  }

  @Override
  public boolean onScrollLongitudinalRight(MotionEvent e1, MotionEvent e2, MotionEvent e3,
      float distanceX, float distanceY) {
    return gestureProxy != null &&
        gestureProxy.onScrollLongitudinalRight(e1, e2, e3, distanceX, distanceY);
  }

  @Override
  public boolean onScrollCrosswise(MotionEvent e1, MotionEvent e2, MotionEvent e3,
      float distanceX, float distanceY) {
    return gestureProxy != null &&
        gestureProxy.onScrollCrosswise(e1, e2, e3, distanceX, distanceY);
  }

  @Override
  public boolean onDoubleClick(MotionEvent e) {
    return gestureProxy != null && gestureProxy.onDoubleClick(e);
  }

  @Override
  public boolean onSingleClick(MotionEvent e) {
    return gestureProxy != null && gestureProxy.onSingleClick(e);
  }

  protected GestureDetectorHelper.IGestureListenerExtendProxy gestureProxy;

  public void setGestureProxy(
      GestureDetectorHelper.IGestureListenerExtendProxy gestureProxy) {
    this.gestureProxy = gestureProxy;
  }
}