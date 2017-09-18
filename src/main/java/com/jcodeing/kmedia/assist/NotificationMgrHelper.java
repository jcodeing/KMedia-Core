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

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.NotificationCompat;

/**
 * Notification Manager Helper. <p /> Assist maintain android.app.NotificationManager.
 */
public class NotificationMgrHelper {

  private final NotificationManager notificationMgr;

  public NotificationManager notificationMgr() {
    return notificationMgr;
  }

  private final Context context;

  public NotificationMgrHelper(@NonNull Context context) {
    this.context = context;
    notificationMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
  }

  // ============================@NotificationMgr@============================
  public static final int ID = NotificationMgrHelper.class.hashCode();//11145491

  // ============================@Base
  public void notify(int id, Notification notification) {
    notificationMgr.notify(id, notification);
  }

  public void cancel(int id) {
    notificationMgr.cancel(id);
  }

  public void cancelAll() {
    notificationMgr.cancelAll();
  }

  // ============================@Extend
  public void notify(int id, String contentTitle, String contentText, @DrawableRes int smallIcon) {
    notificationMgr.notify(id, builderCompat()
        .setOngoing(true)
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setContentTitle(contentTitle)
        .setContentText(contentText)
        .setSmallIcon(smallIcon)
        .build());
  }

  public void notify(String contentTitle, String contentText, @DrawableRes int smallIcon) {
    notify(ID, contentTitle, contentText, smallIcon);
  }

  // ============================@Assist

  /**
   * . < 11(3.0)
   *
   * @return NotificationCompat.Builder
   * @see VERSION_CODES
   */
  public NotificationCompat.Builder builderCompat() {
    return new NotificationCompat.Builder(context);
  }

  /**
   * . >= 11(3.0) !-[ . >= 16(4.1) ? builder.build() : builder.getNotification() ] <p /> WARNING:
   * VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB(11)
   *
   * @return Notification.Builder
   * @see VERSION_CODES
   */
  @RequiresApi(api = VERSION_CODES.HONEYCOMB)
  public Builder builder() {
    return new Builder(context);
  }
}