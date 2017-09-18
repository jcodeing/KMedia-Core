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
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.support.annotation.NonNull;
import com.jcodeing.kmedia.utils.Assert;
import com.jcodeing.kmedia.utils.L;

/**
 * Audio Manager Helper. <p /> Assist maintain android.media.AudioManager.
 */
public class AudioMgrHelper {

  protected static final String TAG = L.makeTag(AudioMgrHelper.class);

  private static class SingletonHolder {

    private static final AudioMgrHelper INSTANCE =
        new AudioMgrHelper();
  }

  /**
   * @return instance
   */
  public static AudioMgrHelper i() {
    return AudioMgrHelper.SingletonHolder.INSTANCE;
  }

  private AudioMgrHelper() {
  }//singleton

  /**
   * @param audioManager (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE)
   */
  public AudioMgrHelper init(@NonNull AudioManager audioManager) {
    if (this.audioManager == null) {
      this.audioManager = audioManager;
    }//internal maintain
    return this;
  }

  public AudioMgrHelper init(@NonNull Context context) {
    return init((AudioManager) context.getSystemService(Context.AUDIO_SERVICE));
  }

  // ============================@AudioManager@============================
  private AudioManager audioManager;

  public AudioManager audioManager() {
    return audioManager;
  }

  public AudioManager audioManager(@NonNull Context context) {
    return init(context).audioManager;
  }

  // ============================@Volume
  private int maxVolumeIndex = C.INDEX_UNSET;

  /**
   * Returns the maximum volume index for a music stream.
   *
   * @see AudioManager#getStreamMaxVolume(int)
   */
  public int getMaxVolume() {
    if (audioManager != null) {
      return maxVolumeIndex != C.INDEX_UNSET ? maxVolumeIndex
          : (maxVolumeIndex = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
    }
    return 0;
  }

  /**
   * Returns the current volume index for a music stream.
   *
   * @see AudioManager#getStreamVolume(int)
   */
  public int getVolume() {
    if (audioManager != null) {
      return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }
    return 0;
  }

  /**
   * Sets the volume index for a music stream.
   *
   * @param index The volume index to set.
   * @param flags One or more flags.
   * @param isIndexIncrement is index increment (+origin)[0<= index <= max]
   * @return deal volume index for a music stream.
   * @see AudioManager#setStreamVolume(int, int, int)
   */
  public int setVolume(int index, int flags, boolean isIndexIncrement) {
    if (audioManager != null) {
      if (isIndexIncrement && index != 0) {
        int indexOrigin = getVolume();//+origin
        index = Assert.reviseInterval(indexOrigin + index,
            0, getMaxVolume(), false, false);//0<= index <= max
        if (index != indexOrigin) {
          audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, flags);
        }
        return index;
      } else if (!isIndexIncrement) {
        int indexOrigin = getVolume();
        index = Assert.reviseInterval(index,
            0, getMaxVolume(), false, false);
        if (index != indexOrigin) {//0<= index <= max
          audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, flags);
        }
        return index;
      }
    }
    return index;
  }

  // ============================@AudioFocus
  public int requestAudioFocus(OnAudioFocusChangeListener audioFocusChangeListener) {
    try {
      if (audioManager != null && audioFocusChangeListener != null) {
        L.d(TAG,
            "Request audio focus(" + audioFocusChangeListener.getClass().getName() + ")");
        int status = audioManager.requestAudioFocus(audioFocusChangeListener,
            AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (status == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
          L.d(TAG, "Request audio focus granted(" + status + ")");
        } else {//AUDIOFOCUS_REQUEST_FAILED
          L.d(TAG, "Request audio focus fail(" + status + ")");
        }
        return status;
      }
    } catch (Exception e) {
      L.printStackTrace(e);
    }
    return AudioManager.AUDIOFOCUS_REQUEST_FAILED;
  }

  public int abandonAudioFocus(OnAudioFocusChangeListener audioFocusChangeListener) {
    try {
      if (audioManager != null && audioFocusChangeListener != null) {
        L.d(TAG,
            "Abandon audio focus(" + audioFocusChangeListener.getClass().getName() + ")");
        int status = audioManager.abandonAudioFocus(audioFocusChangeListener);
        if (status == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
          L.d(TAG, "Abandon audio focus granted(" + status + ")");
        } else {
          L.d(TAG, "Abandon audio focus fail(" + status + ")");
        }
      }
    } catch (Exception e) {
      L.printStackTrace(e);
    }
    return AudioManager.AUDIOFOCUS_REQUEST_FAILED;
  }
}