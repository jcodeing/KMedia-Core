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
package com.jcodeing.kmedia.utils;

import android.text.TextUtils;
import java.util.Formatter;
import java.util.Locale;

public class TimeProgress {

  private static StringBuilder formatBuilder;
  private static Formatter formatter;

  public static String stringForTime(long timeMs) {
    if (formatBuilder == null) {
      formatBuilder = new StringBuilder();
    }
    if (formatter == null) {
      formatter = new Formatter(formatBuilder, Locale.getDefault());
    }
    if (timeMs < 0) {
      timeMs = 0;
    }

    long totalSeconds = (timeMs + 500) / 1000;
    long seconds = totalSeconds % 60;
    long minutes = (totalSeconds / 60) % 60;
    long hours = totalSeconds / 3600;
    formatBuilder.setLength(0);
    return hours > 0 ? formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
        : formatter.format("%02d:%02d", minutes, seconds).toString();
  }

  /**
   * @param timeStr 00:00:00.000 HH:mm:ss.SSS
   * @return timeMs
   */
  public static int timeForString(String timeStr) {
    if (TextUtils.isEmpty(timeStr)) {
      return 0;
    }

    try {
      String[] timeSplit = timeStr.split(":");
      if (timeSplit.length == 2) {
        // =========@mm:ss.SSS@=========
        int minute = Integer.parseInt(timeSplit[0]);
        float second = Float.parseFloat(timeSplit[1]);
        float timeMillisecond = (minute * 60 + second) * 1000;
        return (int) timeMillisecond;
      } else if (timeSplit.length == 3) {
        // =========@HH:mm:ss.SSS@=========
        int hour = Integer.parseInt(timeSplit[0]);
        int minute = Integer.parseInt(timeSplit[1]);
        float second = Float.parseFloat(timeSplit[2]);
        float timeMillisecond = ((hour * 60 + minute) * 60 + second) * 1000;
        return (int) timeMillisecond;
      }
    } catch (Exception e) {
      //NumberFormatException
      return 0;
    }
    return 0;
  }

  /**
   * @return progress [duration <= 0 ? 0 : (int) ((position * progressMax) / duration)]
   */
  public static int progressValue(long position, long duration, int progressMax) {
    return duration <= 0 ? 0 : (int) ((position * progressMax) / duration);
  }

  /**
   * @return position [duration <= 0 ? 0 : ((duration * progress) / progressMax)]
   */
  public static long positionValue(int progress, long duration, int progressMax) {
    return duration <= 0 ? 0 : ((duration * progress) / progressMax);
  }
}