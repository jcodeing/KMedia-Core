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

import android.content.Context;
import android.net.Uri;
import android.view.Surface;
import android.view.SurfaceHolder;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Map;

/**
 * All of the MediaPlayer interface. <p>If you want to add new media player engine, can implement
 * this interface or extends {@link AMediaPlayer}.<p/>
 */
public interface IMediaPlayer extends IPlayerBase {

  // ============================@Source
  void setDataSource(String path)
      throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

  void setDataSource(Context context, Uri uri)
      throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

  void setDataSource(Context context, Uri uri, Map<String, String> headers)
      throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

  void setDataSource(FileDescriptor fd)
      throws IOException, IllegalArgumentException, IllegalStateException;

  Uri getDataSource();

  void prepareAsync() throws IllegalStateException;

  // ============================@Control
  // [Sync in android.media.MediaPlayer]
  boolean start() throws IllegalStateException;

  boolean pause() throws IllegalStateException;

  boolean seekTo(long ms) throws IllegalStateException;

  void stop() throws IllegalStateException;

  // ============================@Set/Get/Is
  // [Sync in android.media.MediaPlayer]
  void setAudioStreamType(int streamtype);

  void setVolume(float leftVolume, float rightVolume);

  void setDisplay(SurfaceHolder sh);

  void setSurface(Surface surface);

  void setScreenOnWhilePlaying(boolean screenOn);

  void setLooping(boolean looping);

  boolean isLooping();

  int getVideoWidth();

  int getVideoHeight();

  int getAudioSessionId();

  // ============================@Listener
  void setListener(Listener listener);

  void setOnPreparedListener(OnPreparedListener listener);

  void setOnCompletionListener(OnCompletionListener listener);

  void setOnBufferingUpdateListener(
      OnBufferingUpdateListener listener);

  void setOnSeekCompleteListener(
      OnSeekCompleteListener listener);

  void setOnVideoSizeChangedListener(
      OnVideoSizeChangedListener listener);

  void setOnErrorListener(OnErrorListener listener);

  void setOnInfoListener(OnInfoListener listener);

  /**
   * @see android.media.MediaPlayer.OnPreparedListener
   */
  interface OnPreparedListener {

    void onPrepared(IMediaPlayer mp);
  }

  /**
   * @see android.media.MediaPlayer.OnCompletionListener
   */
  interface OnCompletionListener {

    void onCompletion(IMediaPlayer mp);
  }

  /**
   * @see android.media.MediaPlayer.OnBufferingUpdateListener
   */
  interface OnBufferingUpdateListener {

    void onBufferingUpdate(IMediaPlayer mp, int percent);
  }

  /**
   * @see android.media.MediaPlayer.OnSeekCompleteListener
   */
  interface OnSeekCompleteListener {

    void onSeekComplete(IMediaPlayer mp);
  }

  interface OnVideoSizeChangedListener {

    void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int unappliedRotationDegrees,
        float pixelWidthHeightRatio);
  }

  /**
   * @see android.media.MediaPlayer.OnErrorListener
   */
  interface OnErrorListener {

    /**
     * @return True if the method handled the error, false if it didn't. Returning false, or not
     * having an OnErrorListener at all, will cause the OnCompletionListener to be called.
     */
    boolean onError(IMediaPlayer mp, int what, int extra, Exception e);
  }

  /**
   * @see android.media.MediaPlayer.OnInfoListener
   */
  interface OnInfoListener {

    /**
     * @return True if the method handled the info, false if it didn't. Returning false, or not
     * having an OnInfoListener at all, will cause the info to be discarded.
     */
    boolean onInfo(IMediaPlayer mp, int what, int extra);
  }

  // ============================@Constants
  //Do not change these values [from android.media.MediaPlayer]

  //ERROR
  int MEDIA_ERROR_UNKNOWN = 1;
  int MEDIA_ERROR_SERVER_DIED = 100;
  int MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200;
  int MEDIA_ERROR_IO = -1004;
  int MEDIA_ERROR_MALFORMED = -1007;
  int MEDIA_ERROR_UNSUPPORTED = -1010;
  int MEDIA_ERROR_TIMED_OUT = -110;

  //INFO
  int MEDIA_INFO_UNKNOWN = 1;
  int MEDIA_INFO_STARTED_AS_NEXT = 2;
  int MEDIA_INFO_VIDEO_RENDERING_START = 3;
  int MEDIA_INFO_VIDEO_TRACK_LAGGING = 700;
  int MEDIA_INFO_BUFFERING_START = 701;
  int MEDIA_INFO_BUFFERING_END = 702;
  int MEDIA_INFO_BAD_INTERLEAVING = 800;
  int MEDIA_INFO_NOT_SEEKABLE = 801;
  int MEDIA_INFO_METADATA_UPDATE = 802;
  int MEDIA_INFO_EXTERNAL_METADATA_UPDATE = 803;
  int MEDIA_INFO_TIMED_TEXT_ERROR = 900;
  int MEDIA_INFO_UNSUPPORTED_SUBTITLE = 901;
  int MEDIA_INFO_SUBTITLE_TIMED_OUT = 902;
}