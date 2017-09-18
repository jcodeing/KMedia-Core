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

import android.view.SurfaceView;
import android.view.TextureView;
import com.jcodeing.kmedia.assist.C;

/**
 * {@link IMediaPlayer} And {@link IPlayer} Common Interface
 */
public interface IPlayerBase {

  // ============================@Control
  boolean start();

  boolean pause();

  boolean seekTo(long ms);

  void stop();

  void reset();

  void release();

  // ============================@Video
  void setVideo(SurfaceView surfaceView);

  void setVideo(TextureView textureView);

  void clearVideo();

  // ============================@Set/Get/Is

  /**
   * Sets the volume, with 0 being silence and 1 being unity gain.
   */
  void setVolume(float volume);

  /**
   * Returns the volume, with 0 being silence and 1 being unity gain.
   */
  float getVolume();

  long getCurrentPosition();

  long getDuration();

  boolean isPlaying();

  // =========@Base Extend
  float getPlaybackSpeed();

  /**
   * @return Whether support
   */
  boolean setPlaybackSpeed(float speed);

  /**
   * @return One of the {@code STATE} constants defined <ul> <li>{@link #STATE_IDLE} <li>{@link
   * #STATE_GOT_SOURCE} <li>{@link #STATE_BUFFERING} <li>{@link #STATE_READY} <li>{@link
   * #STATE_ENDED} </ul>
   */
  int getPlaybackState();

  /**
   * Playback state <ul> <li>!= {@link #STATE_IDLE} <li>!= {@link #STATE_GOT_SOURCE} <li>!= {@link
   * #STATE_BUFFERING} <ul/>
   *
   * @return Whether current state can playback[go player.start()]
   */
  boolean isPlayable();

  // ============================@Listener
  interface Listener {

    void onPrepared();

    /**
     * @return CMD (need handle return command) {@link C#CMD_RETURN_FORCED}/{@link
     * C#CMD_RETURN_NORMAL}
     */
    int onCompletion();

    void onBufferingUpdate(int percent);

    void onSeekComplete();

    void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
        float pixelWidthHeightRatio);

    boolean onError(int what, int extra, Exception e);

    boolean onInfo(int what, int extra);

    // =========@Base Extend

    /**
     * Called when the value returned from {@link #getPlaybackState()} changes.
     */
    void onStateChanged(int playbackState);
  }

  // ============================@Constants
  // Do not change these values
  /**
   * The player does not have a source to play, so it is neither buffering nor ready to play.
   */
  int STATE_IDLE = 1;
  /**
   * The player not able to immediately play from the current position. This state typically occurs
   * when more data needs to be loaded to be ready to play, or more data needs to be buffered for
   * playback to resume.
   */
  int STATE_BUFFERING = 2;
  /**
   * The player is able to immediately play from the current position.
   */
  int STATE_READY = 3;
  /**
   * The player has finished playing the media.
   */
  int STATE_ENDED = 4;

  // =========@Extend
  // Constants Value 916+
  // [IPlayer First letter sequence]+
  /**
   * The player got a source to play. <p>setDataSource(...) after the state of the<p/>
   */
  int STATE_GOT_SOURCE = 9161;
}