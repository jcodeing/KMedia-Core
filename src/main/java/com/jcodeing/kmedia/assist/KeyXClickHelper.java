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

import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import java.util.Calendar;

/**
 * Key X Click Helper help you dispatch continuously/long click {@link KeyEvent}
 */
public class KeyXClickHelper {

  // ============================@Static Singleton@============================
  private KeyXClickHelper() {
  }

  private static class SingletonHolder {

    private static final KeyXClickHelper INSTANCE =
        new KeyXClickHelper();
  }

  /**
   * @return instance
   */
  public static KeyXClickHelper i() {
    return SingletonHolder.INSTANCE;
  }

  private final Handler handler = new Handler(Looper.myLooper());
  // ============================@Click@============================
  private static final long CONTINUOUSLY_CLICK_INTERVAL = 300;
  private static final long LONG_CLICK_THRESHOLD = 500;//milliseconds
  /**
   * click count tag
   */
  private int clickCount = 0;
  private long currentClickTime;

  private KeyEvent keyEvent;

  public void xClick(KeyEvent keyEvent) {
    if ((this.keyEvent = keyEvent) == null) {
      return;
    }

    switch (keyEvent.getAction()) {
      case KeyEvent.ACTION_DOWN:
        currentClickTime = Calendar.getInstance().getTimeInMillis();
        clickCount++;
        handler.removeCallbacks(CLICK_CALLBACK);
        handler.postDelayed(LONG_CLICK_CALLBACK, LONG_CLICK_THRESHOLD);
        break;
      case KeyEvent.ACTION_UP: {
        if (Calendar.getInstance().getTimeInMillis() - currentClickTime < LONG_CLICK_THRESHOLD) {
          handler.removeCallbacks(LONG_CLICK_CALLBACK);
          handler.postDelayed(CLICK_CALLBACK, CONTINUOUSLY_CLICK_INTERVAL);
        }
        break;
      }
    }
  }

  // ============================@Callback
  private OnXClickCallback onXClickCallback;

  public void setOnXClickCallback(OnXClickCallback onXClickCallback) {
    this.onXClickCallback = onXClickCallback;
  }

  public interface OnXClickCallback {

    int STATE_CLICK = 1;
    int STATE_LONG_CLICK = 2;

    /**
     * @param state xClick<ul> <li>{@link #STATE_CLICK}</li> <li>{@link #STATE_CLICK}</li> </ul>
     * @param count xClick count > 1 (continuously click)
     * @param keyEvent key event
     */
    void onXClick(int state, int count, KeyEvent keyEvent);
  }

  private final Runnable LONG_CLICK_CALLBACK = new Runnable() {

    @Override
    public void run() {
      if (onXClickCallback != null) {
        onXClickCallback.onXClick(OnXClickCallback.STATE_LONG_CLICK, clickCount, keyEvent);
      }
      clickCount = 0;//reset
    }
  };

  private final Runnable CLICK_CALLBACK = new Runnable() {
    @Override
    public void run() {
      if (onXClickCallback != null) {
        onXClickCallback.onXClick(OnXClickCallback.STATE_CLICK, clickCount, keyEvent);
      }
      clickCount = 0;//reset
    }
  };
}