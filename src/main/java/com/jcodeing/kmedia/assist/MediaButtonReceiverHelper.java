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

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.view.KeyEvent;
import com.jcodeing.kmedia.IPlayer;

/**
 * Media Key Receiver Helper
 *
 * A media button receiver helper. <p /> You can add this MediaButtonReceiverHelper$MBR to your app
 * by adding it directly to your AndroidManifest.xml:
 * <pre>
 * &lt;receiver android:name="com.jcodeing.kmedia.assist.MediaButtonReceiverHelper$MBR" &gt;
 *   &lt;intent-filter&gt;
 *     &lt;action android:name="android.intent.action.MEDIA_BUTTON" /&gt;
 *   &lt;/intent-filter&gt;
 * &lt;/receiver&gt;
 * </pre>
 * WARNING: use default media button receiver({@link MBR}), be sure to　{@link
 * #setPlayerAskFor(PlayerAskFor)} or {@link #setOnMediaButtonEventListener(OnMediaButtonEventListener)}
 * go to complete a full feats, if not will be a waste of resources.
 */
public class MediaButtonReceiverHelper {

  /**
   * @see AudioManager#registerMediaButtonEventReceiver(ComponentName)
   * @see AudioManager#registerMediaButtonEventReceiver(PendingIntent)
   */
  @SuppressWarnings("deprecation")
  public static void registerMediaButtonEventReceiver(Context context,
      PendingIntent pIntent, ComponentName cName) {
    AudioManager am = AudioMgrHelper.i().audioManager();
    if (am == null && context != null) {
      am = AudioMgrHelper.i().audioManager(context);
    } else if (am == null) {
      return;
    }

    if (Build.VERSION.SDK_INT >= 18 && pIntent != null) {
      try {
        am.registerMediaButtonEventReceiver(pIntent);
      } catch (Exception e) {//special: api internal not job
        if ((cName != null)) {
          am.registerMediaButtonEventReceiver(cName);
        }
      }
    } else if (cName != null) {
      am.registerMediaButtonEventReceiver(cName);
    }
  }

  /**
   * @see AudioManager#unregisterMediaButtonEventReceiver(ComponentName)
   * @see AudioManager#unregisterMediaButtonEventReceiver(PendingIntent)
   */
  @SuppressWarnings("deprecation")
  public static void unregisterMediaButtonEventReceiver(Context context,
      PendingIntent pIntent, ComponentName cName) {
    AudioManager am = AudioMgrHelper.i().audioManager();
    if (am == null && context != null) {
      am = AudioMgrHelper.i().audioManager(context);
    } else if (am == null) {
      return;
    }

    if (Build.VERSION.SDK_INT >= 18 && pIntent != null) {
      try {
        am.unregisterMediaButtonEventReceiver(pIntent);
      } catch (Exception e) {
        if (cName != null) {
          am.unregisterMediaButtonEventReceiver(cName);
        }
      }
    } else if (cName != null) {
      am.unregisterMediaButtonEventReceiver(cName);
    }
  }

  // ============================@Static Singleton@============================
  private static class SingletonHolder {

    private static final MediaButtonReceiverHelper INSTANCE =
        new MediaButtonReceiverHelper();
  }

  /**
   * @return instance
   */
  public static MediaButtonReceiverHelper i() {
    return MediaButtonReceiverHelper.SingletonHolder.INSTANCE;
  }

  public MediaButtonReceiverHelper() {
  }

  // ============================@MediaButton@============================
  // ============================@Receiver
  private ComponentName cName;
  private PendingIntent pIntent;

  /**
   * Sets default media button receiver({@link MBR}) to to be the sole receiver of MEDIA_BUTTON
   * intents.
   */
  public void setDefaultMediaButtonReceiverToSole(Context context, boolean isSole) {
    if (context == null) {
      return;
    }
    if (isSole) {
      cName = new ComponentName(
          context.getPackageName(), MBR.class.getName());
      if (Build.VERSION.SDK_INT >= 18) {
        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setComponent(cName);
        pIntent = PendingIntent//0->ignored
            .getBroadcast(context, 0, mediaButtonIntent, 0);
        registerMediaButtonEventReceiver(context, pIntent, cName);
      } else {
        registerMediaButtonEventReceiver(context, null, cName);
      }
    } else {
      unregisterMediaButtonEventReceiver(context, pIntent, cName);
    }
  }

  /**
   * Default Media Button Receiver
   */
  public static class MBR extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent != null &&
          Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction()) &&
          intent.hasExtra(Intent.EXTRA_KEY_EVENT)) {
        KeyEvent ke = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        MediaButtonReceiverHelper.i().onMediaButtonEvent(ke);
      }
    }
  }

  // ============================@Event
  // =========@PlayerAskFor@=========
  private PlayerAskFor playerAskFor;

  /**
   * assist internal complete a full feats. <p>mainly applied to auto processing default {@link
   * MBR}->{@link #onMediaButtonEvent(KeyEvent)}->KeyCode</p>
   */
  public void setPlayerAskFor(PlayerAskFor playerAskFor) {
    this.playerAskFor = playerAskFor;
  }

  public interface PlayerAskFor {

    IPlayer player();
  }

  // =========@Listener@=========
  private OnMediaButtonEventListener onMediaButtonEventListener;

  /**
   * Default media button receiver -> onReceive, priority to callback listener
   */
  public void setOnMediaButtonEventListener(OnMediaButtonEventListener onMediaButtonEventListener) {
    this.onMediaButtonEventListener = onMediaButtonEventListener;
  }

  public interface OnMediaButtonEventListener {

    /**
     * @param keyEvent media key event
     * @return Whether the key event was handled
     * @see #setOnMediaButtonEventListener(OnMediaButtonEventListener)
     */
    boolean onMediaButtonEvent(KeyEvent keyEvent);

  }

  // KeyEvent constants only available on API 11+
  public static final int KEYCODE_MEDIA_PLAY = 126;
  public static final int KEYCODE_MEDIA_PAUSE = 127;

  /**
   * Called to process media key events. Only media key events will be handled. <p /> WARNING:
   * priority to callback media button event listener({@link OnMediaButtonEventListener#onMediaButtonEvent(KeyEvent)}),
   * if there is no handled, with me handle(*Does nothing if not invoke {@link
   * #setPlayerAskFor(PlayerAskFor)})
   *
   * @return Whether the key event was handled
   */
  public boolean onMediaButtonEvent(KeyEvent keyEvent) {
    boolean was_handled = false;
    if (onMediaButtonEventListener != null) {
      was_handled = onMediaButtonEventListener.onMediaButtonEvent(keyEvent);
    }
    if (!was_handled) {
      //If there is no handled, with so me handle
      IPlayer player;
      if (keyEvent == null || keyEvent.getAction() != KeyEvent.ACTION_DOWN ||
          playerAskFor == null || (player = playerAskFor.player()) == null) {
        return false;
      }
      switch (keyEvent.getKeyCode()) {
        // Note KeyEvent.KEYCODE_MEDIA_PLAY is API 11+
        case KEYCODE_MEDIA_PLAY:
          player.start();
          break;
        // Note KeyEvent.KEYCODE_MEDIA_PAUSE is API 11+
        case KEYCODE_MEDIA_PAUSE:
          player.pause();
          break;
        case KeyEvent.KEYCODE_MEDIA_NEXT:
          player.getMediaQueue().skipToNext();
          break;
        case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
          player.getMediaQueue().skipToPrevious();
          break;
        case KeyEvent.KEYCODE_MEDIA_STOP:
          player.stop();
          break;
        case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
          player.fastForwardRewind(5000);//5s
          break;
        case KeyEvent.KEYCODE_MEDIA_REWIND:
          player.fastForwardRewind(-5000);
          break;
        case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
        case KeyEvent.KEYCODE_HEADSETHOOK:
          if (player.isPlaying()) {
            player.pause();
          } else if (player.isPlayable()) {
            player.start();
          }
          break;
      }
    }
    return true;
  }
}