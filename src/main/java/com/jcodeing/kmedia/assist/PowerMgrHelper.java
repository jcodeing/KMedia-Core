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

import android.os.PowerManager;
import android.text.TextUtils;

/**
 * Power Manager Helper. <p /> Assist maintain android.os.PowerManager.
 */
public class PowerMgrHelper {

  private final PowerManager mPowerManager;

  public PowerManager powerManager() {
    return mPowerManager;
  }

  /**
   * @param powerManager (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
   */
  public PowerMgrHelper(PowerManager powerManager) {
    mPowerManager = powerManager;
  }

  // ============================@WakeLock@============================

  private PowerManager.WakeLock mWakeLock = null;

  public void setWakeLock(PowerManager.WakeLock wakeLock) {
    this.mWakeLock = wakeLock;
  }

  /**
   * sampleCode{ newWakeLock(levelAndFlags | PowerManager.ON_AFTER_RELEASE, TAG); }
   *
   * @param levelAndFlags Combination of wake lock level and flag values defining the requested
   * behavior of the WakeLock.
   * @param tag Your class name (or other tag) for debugging purposes.
   * @see android.os.PowerManager
   */
  public void newWakeLock(int levelAndFlags, String tag) {
    boolean wasHeld = false;
    if (mWakeLock != null) {
      if (mWakeLock.isHeld()) {
        wasHeld = true;
        mWakeLock.release();
      }
      mWakeLock = null;
    }

    mWakeLock = mPowerManager
        .newWakeLock(levelAndFlags,
            TextUtils.isEmpty(tag) ? "KMediaWakeLock" : tag);
    mWakeLock.setReferenceCounted(false);
    if (wasHeld) {
      mWakeLock.acquire();
    }
  }

  public void stayAwake(boolean awake) {
    if (mWakeLock != null) {
      if (awake && !mWakeLock.isHeld()) {
        mWakeLock.acquire();
      } else if (!awake && mWakeLock.isHeld()) {
        mWakeLock.release();
      }
    }
  }
}