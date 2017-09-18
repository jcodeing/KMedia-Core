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

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.SensorManager;
import android.view.OrientationEventListener;

/**
 * Orientation Helper. <p /> Assist sensor listener(OrientationEventListener).
 */
public class OrientationHelper {

  public enum Orientation {
    UNKNOWN,

    PORTRAIT,
    //		     _______
    //        | _____ |
    //        ||     ||
    //        ||_____||
    //        |       |
    //        |       |
    //        |_______|

    LANDSCAPE,
    //		 __________________
    //		｜ __              |
    //		｜|  |             |
    //		｜|__|             |
    //		｜_________________|

    REVERSE_PORTRAIT,
    //		     _______
    //        |       |
    //        |       |
    //        | _____ |
    //        ||     ||
    //        ||_____||
    //        |_______|

    REVERSE_LANDSCAPE
    //	   __________________
    //		｜             __  |
    //		｜            |  | |
    //		｜            |__| |
    //		｜_________________|
  }

  private Activity activity;

  public OrientationHelper(Activity activity) {
    this.activity = activity;
  }

  // ============================@Orientation@============================
  private Orientation currentOrientation = Orientation.UNKNOWN;

  public Orientation getCurrentOrientation() {
    return currentOrientation;
  }

  // ============================@Go...
  public void goPortrait() {
    if (activity != null &&
        activity.getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
      activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
      currentOrientation = Orientation.PORTRAIT;
      if (orientationChangeListener != null) {
        orientationChangeListener.onPortrait(false);
      }
    }
  }

  public void goLandscape() {
    if (activity != null &&
        activity.getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
      activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
      currentOrientation = Orientation.LANDSCAPE;
      if (orientationChangeListener != null) {
        orientationChangeListener.onLandscape(false);
      }
    }
  }

  public void goReversePortrait() {
    if (activity != null &&
        activity.getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
      activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
      currentOrientation = Orientation.REVERSE_PORTRAIT;
      if (orientationChangeListener != null) {
        orientationChangeListener.onReversePortrait(false);
      }
    }
  }

  public void goReverseLandscape() {
    if (activity != null &&
        activity.getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
      activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
      currentOrientation = Orientation.REVERSE_LANDSCAPE;
      if (orientationChangeListener != null) {
        orientationChangeListener.onReverseLandscape(false);
      }
    }
  }

  // ============================@ChangeListener
  public interface OrientationChangeListener {

    void onPortrait(boolean isFromSensor);

    void onLandscape(boolean isFromSensor);

    void onReversePortrait(boolean isFromSensor);

    void onReverseLandscape(boolean isFromSensor);
  }

  private OrientationChangeListener orientationChangeListener;

  public void setOrientationChangeListener(OrientationChangeListener orientationChangeListener) {
    if (this.orientationChangeListener != orientationChangeListener) {
      this.orientationChangeListener = orientationChangeListener;
    }
  }

  private class DefaultOrientationChangeListener implements OrientationChangeListener {

    @Override
    public void onPortrait(boolean isFromSensor) {
      if (isFromSensor) {
        goPortrait();
      }
    }

    @Override
    public void onLandscape(boolean isFromSensor) {
      if (isFromSensor) {
        goLandscape();
      }
    }

    @Override
    public void onReversePortrait(boolean isFromSensor) {
      if (isFromSensor) {
        goReversePortrait();
      }
    }

    @Override
    public void onReverseLandscape(boolean isFromSensor) {
      if (isFromSensor) {
        goReverseLandscape();
      }
    }
  }

  // ============================@Enable
  private MyOrientationEventListener orientationEventListener;

  public void enableFromSensorOrientationChange(boolean isEnable) {
    if (isEnable) {
      // =========@Init@=========
      if (orientationEventListener == null) {
        orientationEventListener = new MyOrientationEventListener(
            activity, SensorManager.SENSOR_DELAY_NORMAL);
      }
      if (orientationChangeListener == null) {
        orientationChangeListener = new DefaultOrientationChangeListener();
      }
      // =========@enable@=========
      if (orientationEventListener != null) {
        orientationEventListener.enable();
      }
    } else {
      // =========@disable@=========
      if (orientationEventListener != null) {
        orientationEventListener.disable();
      }
    }
  }

  public void release() {
    enableFromSensorOrientationChange(false);
    orientationEventListener = null;
    activity = null;
  }


  // ============================@OrientationEventListener@============================
  private class MyOrientationEventListener extends OrientationEventListener {

    MyOrientationEventListener(Context context, int rate) {
      super(context, rate);
    }

    private Orientation currentOrientationFromSensor = Orientation.UNKNOWN;

    void setCurrentOrientation(
        Orientation orientation) {
      currentOrientation = orientation;
      currentOrientationFromSensor = orientation;
    }

    @Override
    public void onOrientationChanged(int orientation) {
      if (currentOrientationFromSensor == Orientation.UNKNOWN) {
        initOrientation(orientation);
      }

      if ((orientation >= 0 && orientation <= 30) || (orientation >= 330 && orientation <= 360)) {
        if (currentOrientationFromSensor != Orientation.PORTRAIT) {
          setCurrentOrientation(Orientation.PORTRAIT);
          if (orientationChangeListener != null) {
            orientationChangeListener.onPortrait(true);
          }
        }
      } else if ((orientation >= 60) && orientation <= 120) {
        if (currentOrientationFromSensor != Orientation.REVERSE_LANDSCAPE) {
          setCurrentOrientation(Orientation.REVERSE_LANDSCAPE);
          if (orientationChangeListener != null) {
            orientationChangeListener.onReverseLandscape(true);
          }
        }
      } else if (orientation >= 150 && orientation <= 210) {
        if (currentOrientationFromSensor != Orientation.REVERSE_PORTRAIT) {
          setCurrentOrientation(Orientation.REVERSE_PORTRAIT);
          if (orientationChangeListener != null) {
            orientationChangeListener.onReversePortrait(true);
          }
        }
      } else if (orientation >= 240 && orientation <= 300) {
        if (currentOrientationFromSensor != Orientation.LANDSCAPE) {
          setCurrentOrientation(Orientation.LANDSCAPE);
          if (orientationChangeListener != null) {
            orientationChangeListener.onLandscape(true);
          }
        }
      }
    }

    private void initOrientation(int orientation) {
      if ((orientation >= 0 && orientation <= 30) || (orientation >= 330 && orientation <= 360)) {
        currentOrientationFromSensor = Orientation.PORTRAIT;
      } else if ((orientation >= 60) && orientation <= 120) {
        currentOrientationFromSensor = Orientation.REVERSE_LANDSCAPE;
      } else if (orientation >= 150 && orientation <= 210) {
        currentOrientationFromSensor = Orientation.REVERSE_PORTRAIT;
      } else if (orientation >= 240 && orientation <= 300) {
        currentOrientationFromSensor = Orientation.LANDSCAPE;
      }
    }

    @Override
    public void disable() {
      super.disable();
      currentOrientationFromSensor = Orientation.UNKNOWN;
    }
  }
}