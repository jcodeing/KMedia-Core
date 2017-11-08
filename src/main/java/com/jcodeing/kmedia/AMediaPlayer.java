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

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Map;

public abstract class AMediaPlayer implements IMediaPlayer {

  // ============================@Source@============================
  protected Uri dataSource;

  @Override
  public void setDataSource(String path)
      throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
    setDataSource(null, Uri.parse(path));
  }

  @Override
  public void setDataSource(Context context, Uri uri)
      throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
    setDataSource(context, uri, null);
  }

  @Override
  public void setDataSource(Context context, Uri uri, Map<String, String> headers)
      throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
    dataSource = uri;
  }

  @Override
  public void setDataSource(FileDescriptor fd)
      throws IOException, IllegalArgumentException, IllegalStateException {
    throw new UnsupportedOperationException("no support");
  }

  @Override
  public Uri getDataSource() {
    return dataSource;
  }

  // ============================@Control@============================
  @Override
  public void reset() {
    dataSource = null;
  }

  // ============================@Set/Get/Is@============================
  @Override
  public void setVolume(float volume) {
    setVolume(volume, volume);
  }

  private int playbackState = STATE_IDLE;

  @Override
  public int getPlaybackState() {
    return playbackState;
  }

  protected void setPlaybackState(int playbackState) {
    this.playbackState = playbackState;
    notifyOnStateChanged();
  }

  @Override
  public boolean isPlayable() {
    return getDataSource() != null;
  }

  // ============================@Listener@============================
  // ============================@MediaPlayer
  private OnPreparedListener mOnPreparedListener;
  private OnCompletionListener mOnCompletionListener;
  private OnBufferingUpdateListener mOnBufferingUpdateListener;
  private OnSeekCompleteListener mOnSeekCompleteListener;
  private OnVideoSizeChangedListener mOnVideoSizeChangedListener;
  private OnErrorListener mOnErrorListener;
  private OnInfoListener mOnInfoListener;

  public final void setOnPreparedListener(OnPreparedListener listener) {
    mOnPreparedListener = listener;
  }

  public final void setOnCompletionListener(OnCompletionListener listener) {
    mOnCompletionListener = listener;
  }

  public final void setOnBufferingUpdateListener(
      OnBufferingUpdateListener listener) {
    mOnBufferingUpdateListener = listener;
  }

  public final void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
    mOnSeekCompleteListener = listener;
  }

  public final void setOnVideoSizeChangedListener(
      OnVideoSizeChangedListener listener) {
    mOnVideoSizeChangedListener = listener;
  }

  public final void setOnErrorListener(OnErrorListener listener) {
    mOnErrorListener = listener;
  }

  public final void setOnInfoListener(OnInfoListener listener) {
    mOnInfoListener = listener;
  }

  // ============================@Player
  protected Listener listener;

  @Override
  public void setListener(Listener listener) {
    this.listener = listener;
  }

  // ============================@Notify
  // =========@Base
  protected final void notifyOnPrepared() {
    if (mOnPreparedListener != null) {
      mOnPreparedListener.onPrepared(this);
    }
    if (listener != null) {
      listener.onPrepared();
    }
  }

  protected final void notifyOnCompletion() {
    if (mOnCompletionListener != null) {
      mOnCompletionListener.onCompletion(this);
    }
    if (listener != null) {
      listener.onCompletion();
    }
  }

  protected final void notifyOnBufferingUpdate(int percent) {
    if (mOnBufferingUpdateListener != null) {
      mOnBufferingUpdateListener.onBufferingUpdate(this, percent);
    }
    if (listener != null) {
      listener.onBufferingUpdate(percent);
    }
  }

  protected final void notifyOnSeekComplete() {
    if (mOnSeekCompleteListener != null) {
      mOnSeekCompleteListener.onSeekComplete(this);
    }
    if (listener != null) {
      listener.onSeekComplete();
    }
  }

  protected final void notifyOnVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
      float pixelWidthHeightRatio) {
    if (mOnVideoSizeChangedListener != null) {
      mOnVideoSizeChangedListener.onVideoSizeChanged(this, width, height,
          unappliedRotationDegrees, pixelWidthHeightRatio);
    }
    if (listener != null) {
      listener.onVideoSizeChanged(width, height,
          unappliedRotationDegrees, pixelWidthHeightRatio);
    }
  }

  protected final boolean notifyOnError(int what, int extra, Exception e) {
    boolean error_was_handled =
        mOnErrorListener != null && mOnErrorListener.onError(this, what, extra, e);
    boolean listener_was_handled =
        listener != null && listener.onError(what, extra, e);
    return error_was_handled || listener_was_handled;
  }

  protected final boolean notifyOnInfo(int what, int extra) {
    boolean info_was_handled =
        mOnInfoListener != null && mOnInfoListener.onInfo(this, what, extra);
    boolean listener_was_handled =
        listener != null && listener.onInfo(what, extra);
    return info_was_handled || listener_was_handled;
  }

  // =========@Extend
  protected final void notifyOnStateChanged() {
    if (listener != null) {
      listener.onStateChanged(playbackState);
    }
  }

  public void releaseListeners() {
    mOnPreparedListener = null;
    mOnBufferingUpdateListener = null;
    mOnCompletionListener = null;
    mOnSeekCompleteListener = null;
    mOnVideoSizeChangedListener = null;
    mOnErrorListener = null;
    mOnInfoListener = null;
    listener = null;
  }
}