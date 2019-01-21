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
package com.jcodeing.kmedia.worker;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Builder;
import androidx.media.app.NotificationCompat.MediaStyle;
import com.jcodeing.kmedia.R;
import com.jcodeing.kmedia.assist.NotificationMgrHelper;
import com.jcodeing.kmedia.definition.IMediaItem;
import com.jcodeing.kmedia.service.PlayerService;
import com.jcodeing.kmedia.utils.L;

public abstract class ANotifier extends BroadcastReceiver {

  protected static final String TAG = L.makeTag("AudioNotifier");

  protected PlayerService playerService;
  protected NotificationMgrHelper helper;

  public void init(PlayerService playerService) {
    this.playerService = playerService;
    helper = new NotificationMgrHelper(playerService);
    helper.cancelAll();
    initPendingIntent();
  }

  public boolean isInitSuccess() {
    return playerService != null && playerService.player() != null && helper != null;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    if (!isInitSuccess()) {
      L.w(TAG, "InitFailure");
      return;
    }
    final String action = intent.getAction();
    L.d(TAG, "Received intent with action " + action);
    switch (action) {
      case ACTION_PAUSE:
        playerService.player().pause();
        break;
      case ACTION_PLAY:
        playerService.player().start();
        break;
      case ACTION_NEXT:
        playerService.player().getMediaQueue().skipToNext();
        break;
      case ACTION_PREV:
        playerService.player().getMediaQueue().skipToPrevious();
        break;
      case ACTION_STOP:
        playerService.player().stop();
        break;
    }
  }

  // ============================@Notification@============================
  protected static final int NOTIFICATION_ID = ANotifier.class.hashCode();
  protected static final int REQUEST_CODE = NOTIFICATION_ID - 1;

  // =========@Audio Base Control Action@=========
  public static final String ACTION_PAUSE = "com.jcodeing.kmedia.action.pause";
  public static final String ACTION_PLAY = "com.jcodeing.kmedia.action.play";
  public static final String ACTION_PREV = "com.jcodeing.kmedia.action.prev";
  public static final String ACTION_NEXT = "com.jcodeing.kmedia.action.next";
  public static final String ACTION_STOP = "com.jcodeing.kmedia.action.stop";

  protected IntentFilter getAudioControlsIntentFilter() {
    IntentFilter filter = new IntentFilter();
    filter.addAction(ACTION_NEXT);
    filter.addAction(ACTION_PAUSE);
    filter.addAction(ACTION_PLAY);
    filter.addAction(ACTION_PREV);
    filter.addAction(ACTION_STOP);
    return filter;
  }

  protected PendingIntent pausePIntent;
  protected PendingIntent playPIntent;
  protected PendingIntent prevPIntent;
  protected PendingIntent nextPIntent;
  protected PendingIntent stopPIntent;

  protected void initPendingIntent() {
    String pkg = playerService.getPackageName();

    pausePIntent = PendingIntent.getBroadcast(playerService, REQUEST_CODE,
        new Intent(ACTION_PAUSE).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
    playPIntent = PendingIntent.getBroadcast(playerService, REQUEST_CODE,
        new Intent(ACTION_PLAY).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
    prevPIntent = PendingIntent.getBroadcast(playerService, REQUEST_CODE,
        new Intent(ACTION_PREV).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
    nextPIntent = PendingIntent.getBroadcast(playerService, REQUEST_CODE,
        new Intent(ACTION_NEXT).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
    stopPIntent = PendingIntent.getBroadcast(playerService, REQUEST_CODE,
        new Intent(ACTION_STOP).setPackage(pkg), PendingIntent.FLAG_CANCEL_CURRENT);
  }

  // ============================@Start/Stop/Update
  private boolean started = false;

  /**
   * @return State <ul> <li>0:failure <li>1:succeed <li>2:started <ul/>
   */
  public int startNotification() {
    if (!isInitSuccess()) {
      L.w(TAG, "InitFailure");
      return 0;//failure
    }
    if (!started) {
      // The notification must be updated after setting started to true
      if ((notification = createNotification()) != null) {
        playerService.registerReceiver(this, getAudioControlsIntentFilter());
        playerService.startForeground(NOTIFICATION_ID, notification);
        started = true;
        return 1;//succeed
      }
      return 0;
    } else {
      return 2;//started
    }
  }

  public void stopNotification() {
    if (!isInitSuccess()) {
      L.w(TAG, "InitFailure");
      return;
    }
    if (started) {
      started = false;
      try {
        helper.cancel(NOTIFICATION_ID);
        playerService.unregisterReceiver(this);
      } catch (IllegalArgumentException ex) {
        // ignore if the receiver is not registered.
      }
      playerService.stopForeground(true);
      notification = null;
    }
  }

  public void updateNotification() {
    if (isInitSuccess()) {
      IMediaItem currentMediaItem = playerService.player().getMediaQueue().getCurrentMediaItem();
      if (mediaItem == currentMediaItem) {
        updateNotification(false);
      } else {
        mediaItem = currentMediaItem;
        updateNotification(true);
      }
      helper.notify(NOTIFICATION_ID, notification);
    }
  }

  /**
   * Simple template
   * <pre>
   * &#64Override
   * public void updateNotification(boolean mediaItemChanged) {
   *    //Update state(play/pause)
   *    builder.mActions.clear();
   *    addSimpleMediaAction(builder);
   *    if (mediaItemChanged) {
   *      //Update info(title/icon)
   *      setSimpleMediaInfo(builder, mediaItem);
   *    }
   *    notification = builder.build();
   * }
   * </pre>
   */
  protected abstract void updateNotification(boolean mediaItemChanged);

  // ============================@Create
  protected IMediaItem mediaItem;
  protected Notification notification;

  /**
   * Simple template
   * <pre>
   * &#64Override
   * protected Notification createNotification(IMediaItem item) {
   *    return createSimpleMediaNotificationBuilder(item).build();
   * }
   * </pre>
   */
  protected abstract Notification createNotification(IMediaItem mediaItem);

  protected Notification createNotification() {
    if (isInitSuccess()) {
      mediaItem = playerService.player().getMediaQueue().getCurrentMediaItem();
      if (mediaItem != null) {
        return createNotification(mediaItem);
      }
    }
    return null;
  }

  protected Builder createSimpleMediaNotificationBuilder(@NonNull IMediaItem mediaItem) {
    NotificationCompat.Builder builder = helper.builderCompat();
    builder.setWhen(0).setShowWhen(false).setUsesChronometer(false);
    addSimpleMediaAction(builder);
    builder
        .setSmallIcon(R.drawable.k_ic_media)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setStyle(new MediaStyle());
    setSimpleMediaInfo(builder, mediaItem);
    return builder;
  }

  /**
   * WARNING: internal not handle setLargeIcon(.)
   */
  protected void setSimpleMediaInfo(@NonNull Builder builder, @NonNull IMediaItem mediaItem) {
    builder.setContentTitle(mediaItem.getTitle()).setContentText(mediaItem.getDescription());
  }

  /**
   * Base on media queue size and player state add action <p /> WARNING: if builder.mActions.size()
   * > 0 ? builder.mActions.clear()
   */
  protected void addSimpleMediaAction(@NonNull Builder builder) {
    if (builder.mActions.size() > 0) {
      builder.mActions.clear();
    }
    if (playerService.player().isPlaying()) {
      if (playerService.player().getMediaQueue().size() > 1) {
        builder.addAction(R.drawable.k_ic_prev,
            playerService.getString(R.string.k_label_previous), prevPIntent);

        builder.addAction(new NotificationCompat.Action(R.drawable.k_ic_pause,
            playerService.getString(R.string.k_label_pause), pausePIntent));

        builder.addAction(R.drawable.k_ic_next,
            playerService.getString(R.string.k_label_next), nextPIntent);
      } else {
        builder.addAction(new NotificationCompat.Action(R.drawable.k_ic_pause,
            playerService.getString(R.string.k_label_pause), pausePIntent));

        builder.addAction(R.drawable.k_ic_close,
            playerService.getString(R.string.k_label_stop), stopPIntent);
      }
    } else {
      builder.addAction(new NotificationCompat.Action(R.drawable.k_ic_play,
          playerService.getString(R.string.k_label_play), playPIntent));

      builder.addAction(R.drawable.k_ic_close,
          playerService.getString(R.string.k_label_stop), stopPIntent);
    }
  }
}