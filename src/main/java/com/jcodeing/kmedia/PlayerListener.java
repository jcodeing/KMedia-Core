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
package com.jcodeing.kmedia;

import android.content.Intent;
import com.jcodeing.kmedia.assist.C;

/**
 * Simple player listener
 *
 * @see IPlayer#addListener(IPlayer.Listener)
 */
public class PlayerListener implements IPlayer.Listener {

  @Override
  public void onPrepared() {
    //Do nothing
  }

  @Override
  public void onBufferingUpdate(int percent) {
    //Do nothing
  }

  @Override
  public void onSeekComplete() {
    //Do nothing
  }

  @Override
  public int onCompletion() {
    return C.CMD_RETURN_NORMAL;
  }

  @Override
  public boolean onInfo(int what, int extra) {
    return false;
  }

  @Override
  public boolean onError(int what, int extra, Exception e) {
    return false;
  }

  @Override
  public void onVideoSizeChanged(int width, int height,
      int unappliedRotationDegrees, float pixelWidthHeightRatio) {
    //Do nothing
  }

  // ============================@Extend
  @Override
  public void onStateChanged(int playbackState) {
    //Do nothing
  }

  public boolean onPlayProgress(long position, long duration) {
    return false;
  }

  @Override
  public void onPositionUnitProgress(long position, int posUnitIndex, int posUnitState) {
    //Do nothing
  }

  @Override
  public void onABProgress(long position, long duration, int abState) {
    //Do nothing
  }

  @Override
  public void onNotificationRequired(int order) {
    //Do nothing
  }

  @Override
  public boolean onAudioFocusChange(int focusChange) {
    return false;
  }

  @Override
  public boolean onIntent(Intent intent) {
    return intent != null;
  }


  // ============================@Life
  @Override
  public void onAdded() {
    //Do nothing
  }

  @Override
  public void onRemoved() {
    //Do nothing
  }
}