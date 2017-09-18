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

import android.annotation.TargetApi;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.annotation.RequiresApi;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import com.jcodeing.kmedia.utils.L;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Map;

public class AndroidMediaPlayer extends AMediaPlayer implements
    MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener,
    MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnSeekCompleteListener,
    MediaPlayer.OnVideoSizeChangedListener, MediaPlayer.OnErrorListener,
    MediaPlayer.OnInfoListener, SurfaceHolder.Callback {

  private static final String TAG = L.makeTag("AndroidMediaPlayer");

  private final MediaPlayer internalPlayer;

  public AndroidMediaPlayer() {
    internalPlayer = new MediaPlayer();
    internalPlayer.setOnPreparedListener(this);
    internalPlayer.setOnBufferingUpdateListener(this);
    internalPlayer.setOnCompletionListener(this);
    internalPlayer.setOnSeekCompleteListener(this);
    internalPlayer.setOnVideoSizeChangedListener(this);
    internalPlayer.setOnErrorListener(this);
    internalPlayer.setOnInfoListener(this);
  }

  public MediaPlayer internalPlayer() {
    return internalPlayer;
  }

  // ============================@Source@============================
  @Override
  public void setDataSource(String path)
      throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
    dataSource = Uri.parse(path);
    internalPlayer.setDataSource(path);
    setPlaybackState(STATE_GOT_SOURCE);
  }

  @Override
  public void setDataSource(Context context, Uri uri)
      throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
    internalPlayer.setDataSource(context, dataSource = uri);
    setPlaybackState(STATE_GOT_SOURCE);
  }

  /**
   * WARNING: VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH(14)
   */
  @TargetApi(VERSION_CODES.ICE_CREAM_SANDWICH)
  @Override
  public void setDataSource(Context context, Uri uri, Map<String, String> headers)
      throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
    if (VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH) {
      internalPlayer.setDataSource(context, dataSource = uri, headers);
    } else {
      setDataSource(context, uri);
    }
    setPlaybackState(STATE_GOT_SOURCE);
  }

  @Override
  public void setDataSource(FileDescriptor fd)
      throws IOException, IllegalArgumentException, IllegalStateException {
    internalPlayer.setDataSource(fd);
    setPlaybackState(STATE_GOT_SOURCE);
  }

  private boolean isPrepared;

  @Override
  public void prepareAsync() throws IllegalStateException {
    isPrepared = false;
    internalPlayer.prepareAsync();
    setPlaybackState(STATE_BUFFERING);
  }

  // ============================@Control@============================
  @Override
  public boolean start() throws IllegalStateException {
    internalPlayer.start();
    notifyOnStateChanged();
    L.dd(TAG, "start()");
    return true;
  }

  @Override
  public boolean pause() throws IllegalStateException {
    internalPlayer.pause();
    notifyOnStateChanged();
    L.dd(TAG, "pause()");
    return true;
  }

  @Override
  public boolean seekTo(long ms) throws IllegalStateException {
    internalPlayer.seekTo((int) ms);
    setPlaybackState(STATE_BUFFERING);
    L.dd(TAG, "seekTo(" + ms + ")");
    return true;
  }

  @Override
  public void stop() throws IllegalStateException {
    internalPlayer.stop();
  }

  @Override
  public void reset() {
    try {
      internalPlayer.reset();
    } catch (IllegalStateException e) {
      L.printStackTrace(e);
    }
  }

  @Override
  public void release() {
    internalPlayer.release();
    releaseListeners();
  }

  // ============================@Video@============================
  @Override
  public void setVideo(SurfaceView surfaceView) {
    surfaceView.getHolder().addCallback(this);
  }

  /**
   * WARNING: VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH(14)
   */
  @RequiresApi(api = VERSION_CODES.ICE_CREAM_SANDWICH)
  @Override
  public void setVideo(TextureView textureView) {
    setSurface(new Surface(textureView.getSurfaceTexture()));
  }

  @Override
  public void clearVideo() {
    if (VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH) {
      setSurface(null);
    } else {
      setDisplay(null);
    }
  }

  // ============================@SurfaceCallback
  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    setDisplay(holder);
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    //Do nothing
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
    //Do nothing
  }

  // ============================@Set/Get/Is@============================
  @Override
  public void setAudioStreamType(int streamtype) {
    internalPlayer.setAudioStreamType(streamtype);
  }

  @Override
  public void setVolume(float leftVolume, float rightVolume) {
    internalPlayer.setVolume(audioVolume = leftVolume, rightVolume);
  }

  private float audioVolume = 1;

  @Override
  public float getVolume() {
    return audioVolume;
  }

  @Override
  public void setDisplay(SurfaceHolder sh) {
    try {
      internalPlayer.setDisplay(sh);
    } catch (IllegalStateException e) {
      L.printStackTrace(e);
    }
  }

  /**
   * WARNING: VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH(14)
   */
  @RequiresApi(api = VERSION_CODES.ICE_CREAM_SANDWICH)
  @Override
  public void setSurface(Surface surface) {
    internalPlayer.setSurface(surface);
  }

  @Override
  public void setScreenOnWhilePlaying(boolean screenOn) {
    internalPlayer.setScreenOnWhilePlaying(screenOn);
  }

  @Override
  public void setLooping(boolean looping) {
    internalPlayer.setLooping(looping);
  }

  @Override
  public boolean isLooping() {
    return internalPlayer.isLooping();
  }

  @Override
  public boolean setPlaybackSpeed(float speed) {
    return false;//no support
  }

  @Override
  public float getPlaybackSpeed() {
    return 0;
  }

  @Override
  public long getCurrentPosition() {
    try {
      return internalPlayer.getCurrentPosition();
    } catch (IllegalStateException e) {
      L.printStackTrace(e);
      return 0;
    }
  }

  @Override
  public long getDuration() {
    try {
      if (isPrepared) {
        return internalPlayer.getDuration();
      }//Prepare before, inaccurate duration.
    } catch (IllegalStateException e) {
      L.printStackTrace(e);
    }
    return 0;
  }

  @Override
  public int getVideoWidth() {
    return internalPlayer.getVideoWidth();
  }

  @Override
  public int getVideoHeight() {
    return internalPlayer.getVideoHeight();
  }

  @Override
  public int getAudioSessionId() {
    return internalPlayer.getAudioSessionId();
  }

  @Override
  public boolean isPlayable() {
    int state = getPlaybackState();
    switch (state) {
      case STATE_IDLE:
      case STATE_GOT_SOURCE:
      case STATE_BUFFERING:
        return false;
      default:
        return true;
    }
  }

  @Override
  public boolean isPlaying() {
    try {
      return internalPlayer.isPlaying();
    } catch (IllegalStateException e) {
      L.printStackTrace(e);
      return false;
    }
  }

  // ============================@Listener@============================
  @Override
  public boolean onInfo(MediaPlayer mp, int what, int extra) {
    return notifyOnInfo(what, extra);
  }

  @Override
  public boolean onError(MediaPlayer mp, int what, int extra) {
    return notifyOnError(what, extra, null);
  }

  @Override
  public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
    notifyOnVideoSizeChanged(width, height, 0, 1);
  }

  @Override
  public void onSeekComplete(MediaPlayer mp) {
    setPlaybackState(STATE_READY);
    notifyOnSeekComplete();
  }

  @Override
  public void onBufferingUpdate(MediaPlayer mp, int percent) {
    notifyOnBufferingUpdate(percent);
  }

  @Override
  public void onCompletion(MediaPlayer mp) {
    if (isPrepared) {
      setPlaybackState(STATE_ENDED);
      notifyOnCompletion();
    }//Preventive injection, to avoid abnormal callback
  }//Such as, prepare before got inaccurate duration.

  @Override
  public void onPrepared(MediaPlayer mp) {
    isPrepared = true;
    setPlaybackState(STATE_READY);
    notifyOnPrepared();
  }
}