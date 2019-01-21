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

import android.Manifest.permission;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.SurfaceView;
import android.view.TextureView;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import com.jcodeing.kmedia.assist.AudioMgrHelper;
import com.jcodeing.kmedia.assist.C;
import com.jcodeing.kmedia.assist.PositionsHelper;
import com.jcodeing.kmedia.definition.IMediaItem;
import com.jcodeing.kmedia.definition.IMediaQueue;
import com.jcodeing.kmedia.definition.IPositionUnitList;
import com.jcodeing.kmedia.definition.MediaQueue;
import com.jcodeing.kmedia.utils.Assert;
import com.jcodeing.kmedia.utils.L;
import com.jcodeing.kmedia.utils.TimeProgress;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Abstract Player
 *
 * @param <P> subClass
 * @see Player
 */
public abstract class APlayer<P extends APlayer> implements IPlayer<P>, IPlayerBase.Listener {

  /**
   * Sub Class Override
   * <pre>
   * public class Player extends APlayer&#60Player> {
   *
   *    &#64Override
   *    protected Player returnThis() {
   *      return this;
   *    }
   * }
   * </pre>
   */
  protected abstract P returnThis();

  /**
   * Not param constructor. <p />Internal will nonsupport, base on context of function Module. <p
   * />If you later want to support, base on context of function Module. see below (optional
   * operation)
   *
   * @see #APlayer(Context)
   * @see #supportBaseOnContextOfFunctionModule(Context)
   */
  public APlayer() {
  }//Not param constructor

  /**
   * Default support, base on context of function Module.
   *
   * @see #supportBaseOnContextOfFunctionModule(Context)
   * @see #APlayer()
   */
  public APlayer(@NonNull Context context) {
    supportBaseOnContextOfFunctionModule(context);
  }

  // ============================@IPlayerExtend@============================
  protected IMediaPlayer internalPlayer;

  @Override
  public IMediaPlayer internalPlayer() {
    return internalPlayer;
  }

  /**
   * @return internalPlayer != null
   */
  protected boolean initConfig(IMediaPlayer mediaPlayer) {
    if (internalPlayer == null) {
      return false;
    }

    //Config
    internalPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    internalPlayer.setVolume(1, 1);

    //Listener
    internalPlayer.setListener(this);
    return true;
  }

  @Override
  public P init(IMediaPlayer mediaPlayer) {
    if (internalPlayer == mediaPlayer) {
      return returnThis();
    }
    try {
      if (internalPlayer != null) {
        internalPlayer.setListener(null);
        release();//release old player
      }
    } catch (Exception e) {
      L.printStackTrace(e);
    }
    internalPlayer = mediaPlayer;
    initConfig(mediaPlayer);
    return returnThis();
  }


  protected boolean isPrepared(Uri uri) {
    return uri != null && uri.equals(internalPlayer.getDataSource()) && isPlayable();
  }

  protected boolean prepare(Uri uri, boolean shouldAutoPlayWhenPrepared) {
    this.shouldAutoPlayWhenPrepared = shouldAutoPlayWhenPrepared;
    try {
      if (uri == null || TextUtils.isEmpty(uri.toString()) || internalPlayer == null) {
        return false;
      }
      // =========@has prepared@=========
      if (isPrepared(uri)) {
        onPrepared();
        return true;
      }
      // =========@reset@=========
      reset();
      // =========@source prepare@=========
      internalPlayer.setDataSource(uri.toString());
      internalPlayer.prepareAsync();

      // =========@Default@=========
      currentMediaId = uri.toString();

      // =========@Other@=========
      // to hold a Wifi lock,
      // which prevents the player from
      // going to sleep while the media is playing.
      if (wifiLock != null && !wifiLock.isHeld()) {
        wifiLock.acquire();
      }
      return true;
    } catch (Exception e) {
      //IO,Illegal...
      L.printStackTrace(e);
    }
    return false;
  }

  protected boolean prepare(IMediaItem mediaItem, boolean shouldAutoPlayWhenPrepared) {
    //The late extension, processing mediaItem.getExtras()
    if (mediaItem != null && prepare(mediaItem.getMediaUri(), shouldAutoPlayWhenPrepared)) {
      currentMediaId = mediaItem.getMediaId();
      return true;
    }
    return false;
  }

  protected boolean prepareMediaId(String mediaId, boolean shouldAutoPlayWhenPrepared) {
    return getMediaQueue().setCurrentIndex(mediaId) &&
        prepare(getMediaQueue().getCurrentMediaItem(), shouldAutoPlayWhenPrepared);
  }


  @Override
  public boolean prepare(Uri uri) {
    return prepare(uri, false);
  }

  @Override
  public boolean prepare(IMediaItem mediaItem) {
    return prepare(mediaItem, false);
  }

  @Override
  public boolean prepareMediaId(String mediaId) {
    return prepareMediaId(mediaId, false);
  }

  @Override
  public boolean play() {
    //AB Enabled
    if (abEnabled = abStartPosition >= 0) {
      isInAB = false;
      abLoopedCount = 0;
      shouldAutoPlayWhenSeekComplete = true;
      return seekTo(abStartPosition);
    }
    return start();
  }

  @Override
  public boolean play(Uri uri) {
    //AB Enabled
    if (abEnabled = abStartPosition >= 0) {
      isInAB = false;
      abLoopedCount = 0;
      seekToPending(abStartPosition);
      return prepare(uri, true);
    }
    return prepare(uri, true);
  }

  @Override
  public boolean play(IMediaItem mediaItem) {
    //AB Enabled
    if (abEnabled = abStartPosition >= 0) {
      isInAB = false;
      abLoopedCount = 0;
      seekToPending(abStartPosition);
      return prepare(mediaItem, true);
    }
    return prepare(mediaItem, true);
  }

  @Override
  public boolean playMediaId(String mediaId) {
    //AB Enabled
    if (abEnabled = abStartPosition >= 0) {
      isInAB = false;
      abLoopedCount = 0;
      seekToPending(abStartPosition);
      return prepareMediaId(mediaId, true);
    }
    return prepareMediaId(mediaId, true);
  }

  @Override
  public void shutdown() {
    release();
    listeners.clear();
    mediaQueue = null;
    posUnitList = null;
    audioMgrHelper = null;
  }

  // ============================@Control
  protected long pendingSeekToMs = C.POSITION_UNSET;

  @Override
  public void seekToPending(long ms) {
    pendingSeekToMs = ms;
  }

  protected boolean haveSeekToPending() {
    return pendingSeekToMs != C.POSITION_UNSET;
  }

  @Override
  public long seekToProgress(int progress, int progressMax) {
    long position = TimeProgress.positionValue(progress, getDuration(), progressMax);
    if (seekTo(position)) {
      return position;
    }
    return -1;
  }

  // ============================@IPlayer@============================
  // ============================@Control
  @Override
  public boolean start() {
    shouldAutoPlayWhenSeekComplete = true;
    try {
      if (isPlayable()) {
        if (beforeStart()) {
          return false;
        }
        internalPlayer.start();
        //go update...
        updatePlayProgress(1);
        //start notification
        onNotificationRequired(1);
        return true;
      }
    } catch (Exception e) {
      //IllegalState
      L.printStackTrace(e);
    }
    return false;
  }

  protected boolean beforeStart() {
    tryToGetAudioFocus();
    if (configureAudioFocus()) {
      return true;//forced pause
    }
    // =========@reset@=========
    if (getPlaybackState() == STATE_ENDED) {
      // =========@start from onCompletion
      setCurrentPositionUnitIndex(C.INDEX_UNSET);
      posUnitLoopedCount = 0;
    }
    // =========@pending@=========
    if (haveSeekToPending()) {
      long ms = pendingSeekToMs;
      pendingSeekToMs = C.POSITION_UNSET;
      seekTo(ms);
      return true;
    }
    return false;
  }

  @Override
  public boolean pause() {
    shouldAutoPlayWhenSeekComplete = false;
    try {
      //interrupt update...
      updatePlayProgress(0);
      if (isPlayable()) {
        internalPlayer.pause();
        return true;
      }
    } catch (Exception e) {
      //IllegalState
      L.printStackTrace(e);
    }
    return false;
  }

  @Override
  public boolean seekTo(long ms, int processingLevel) {
    try {
      if (isPlayable()) {
        long duration = getDuration();
        long position = Math.min(ms < 0 ? 0 : ms, duration);
        internalPlayer.seekTo(position);
        // =========@processing@=========
        if (processingLevel >= 1) {
          onPlayProgress(position, duration);
        }
        if (processingLevel >= 2) {
          calibrateCurrentPositionUnitIndex(position);
        }
        return true;
      }
    } catch (Exception e) {
      //IllegalState
      L.printStackTrace(e);
    }
    return false;
  }

  @Override
  public boolean seekTo(long ms) {
    return seekTo(ms, 2);
  }

  @Override
  public boolean fastForwardRewind(long ms) {
    return ms != 0 && seekTo(getCurrentPosition() + ms);
  }

  @Override
  public void stop() {
    try {
      //interrupt update...
      updatePlayProgress(0);
      if (internalPlayer != null) {
        internalPlayer.stop();
      }
      giveUpAudioFocus();
      unregisterComponentReceiver();
      //stop notification
      onNotificationRequired(0);
    } catch (Exception e) {
      //IllegalState
      L.printStackTrace(e);
    }
  }

  @Override
  public void reset() {
    //interrupt update...
    updatePlayProgress(0);
    // =========@Reset@=========
    setCurrentPositionUnitIndex(C.INDEX_UNSET);
    posUnitLoopedCount = 0;
    //media uri change must be reset
    setPositionUnitLoopIndexList(null);
    currentMediaId = null;
    // =========@Player
    if (internalPlayer != null) {
      internalPlayer.reset();
    }
  }

  @Override
  public void release() {
    stop();
    // =========@Release@=========
    if (internalPlayer != null) {
      internalPlayer.release();
      internalPlayer = null;
    }
    if (wifiLock != null && wifiLock.isHeld()) {
      wifiLock.release();
    }
    // =========@Clear
    clearAB();
  }

  // ============================@Video
  @Override
  public void setVideo(SurfaceView surfaceView) {
    if (internalPlayer != null) {
      internalPlayer.setVideo(surfaceView);
    }
  }

  @Override
  public void setVideo(TextureView textureView) {
    if (internalPlayer != null) {
      internalPlayer.setVideo(textureView);
    }
  }

  @Override
  public void clearVideo() {
    if (internalPlayer != null) {
      internalPlayer.clearVideo();
    }
  }

  // ============================@Set/Get/Is
  @Override
  public void setVolume(float volume) {
    if (internalPlayer != null) {
      internalPlayer.setVolume(volume);
    }
  }

  @Override
  public float getVolume() {
    if (internalPlayer != null) {
      return internalPlayer.getVolume();
    }
    return 1;
  }

  @Override
  public long getCurrentPosition() {
    if (internalPlayer != null) {
      return internalPlayer.getCurrentPosition();
    }
    return 0;
  }

  @Override
  public long getDuration() {
    if (internalPlayer != null) {
      return internalPlayer.getDuration();
    }
    return 0;
  }

  @Override
  public boolean isPlaying() {
    return internalPlayer != null && internalPlayer.isPlaying();
  }

  @Override
  public boolean setPlaybackSpeed(float speed) {
    return internalPlayer != null && internalPlayer.setPlaybackSpeed(speed);
  }

  @Override
  public float getPlaybackSpeed() {
    if (internalPlayer != null) {
      return internalPlayer.getPlaybackSpeed();
    }
    return 0;
  }

  @Override
  public int getPlaybackState() {
    if (internalPlayer != null) {
      return internalPlayer.getPlaybackState();
    }
    return STATE_IDLE;
  }

  @Override
  public boolean isPlayable() {
    return internalPlayer != null && internalPlayer.isPlayable();
  }


  // ============================@Listener
  protected final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<>();

  @Override
  public void addListener(Listener listener) {
    if (listener != null) {
      listeners.add(listener);
      listener.onAdded();
    }
  }

  @Override
  public void removeListener(Listener listener) {
    if (listener != null) {
      listeners.remove(listener);
      listener.onRemoved();
    } else {//removeAll
      int removeTotal = listeners.size();
      for (int i = 0; i < removeTotal; i++) {
        listener = listeners.remove(i);
        listener.onRemoved();
        i = i - 1;
        removeTotal = removeTotal - 1;
      }
    }
  }

  // =========@Base@=========
  protected boolean shouldAutoPlayWhenPrepared = true;

  @Override
  public void onPrepared() {
    if (shouldAutoPlayWhenPrepared) {
      start();
    }
    for (Listener listener : listeners) {
      listener.onPrepared();
    }
  }

  @Override
  public void onBufferingUpdate(int percent) {
    for (Listener listener : listeners) {
      listener.onBufferingUpdate(percent);
    }
  }

  protected boolean shouldAutoPlayWhenSeekComplete = true;

  @Override
  public P shouldAutoPlayWhenSeekComplete(boolean shouldAutoPlayWhenSeekComplete) {
    this.shouldAutoPlayWhenSeekComplete = shouldAutoPlayWhenSeekComplete;
    return returnThis();
  }

  @Override
  public void onSeekComplete() {
    if (shouldAutoPlayWhenSeekComplete) {
      start();
    }
    for (Listener listener : listeners) {
      listener.onSeekComplete();
    }
  }

  @Override
  public int onCompletion() {
    //completion update... [insure (duration <= position) deal]
    updatePlayProgress(11);
    // =========@LoopProcessing
    int loopReturnParam;
    // ======@PositionUnit
    if ((loopReturnParam = positionUnitLoopProcessing(2)) == 1) {
      return C.CMD_RETURN_FORCED;//[position unit] loop is processing
    } else if (loopReturnParam == 2) {
      onPositionUnitProgress(C.POSITION_UNSET, C.POSITION_UNSET,
          C.INDEX_UNSET, C.STATE_PROGRESS_POS_UNIT_FINISH);//loop finish
    }
    // ======@AB
    if ((loopReturnParam = abLoopProcessing(2)) == 1) {
      return C.CMD_RETURN_FORCED;//[A-B] loop is processing
    } else if (loopReturnParam == 2) {
      onABProgress(C.POSITION_UNSET, C.POSITION_UNSET,
          C.STATE_PROGRESS_AB_FINISH);//loop finish
    }
    //interrupt update...
    updatePlayProgress(0);
    if (!getMediaQueue().skipToAutoAssigned()) {
      for (Listener listener : listeners) {
        listener.onCompletion();
      }
    }
    return C.CMD_RETURN_NORMAL;
  }

  @Override
  public boolean onInfo(int what, int extra) {
    boolean info_was_handled = false;
    for (Listener listener : listeners) {
      if (listener.onInfo(what, extra)) {
        info_was_handled = true;
      }
    }
    return info_was_handled;
  }

  @Override
  public boolean onError(int what, int extra, Exception e) {
    boolean error_was_handled = false;
    for (Listener listener : listeners) {
      if (listener.onError(what, extra, e)) {
        error_was_handled = true;
      }
    }
    return error_was_handled;
  }

  @Override
  public void onVideoSizeChanged(int width, int height,
      int unappliedRotationDegrees, float pixelWidthHeightRatio) {
    for (Listener listener : listeners) {
      listener
          .onVideoSizeChanged(width, height, unappliedRotationDegrees, pixelWidthHeightRatio);
    }
  }

  // =========@Base Extend
  @Override
  public void onStateChanged(int playbackState) {
    if (playbackState == IPlayer.STATE_READY) {
      //update notification(with started)
      onNotificationRequired(2);
    }
    for (Listener listener : listeners) {
      listener.onStateChanged(playbackState);
    }
  }

  // =========@Extend@=========
  // Must be kept in sync with IPlayer.Listener
  protected boolean onIntent(Intent intent) {
    if (intent != null) {
      for (Listener listener : listeners) {
        listener.onIntent(intent);
      }
      return true;
    }
    return false;
  }

  protected boolean onPlayProgress(long position, long duration) {
    boolean play_progress_was_handled = false;
    for (Listener listener : listeners) {
      if (listener.onPlayProgress(position, duration)) {
        //As long as there is one was handled
        play_progress_was_handled = true;
      }
    }
    return play_progress_was_handled;
  }

  protected void onPositionUnitProgress(long position, long duration,
      int posUnitIndex, int posUnitState) {
    for (Listener listener : listeners) {
      listener.onPositionUnitProgress(position, duration, posUnitIndex, posUnitState);
    }
    if (posUnitState == C.STATE_PROGRESS_POS_UNIT_END) {
      if (positionUnitLoopProcessing(1) == 2) {
        // =========@finish@=========
        for (Listener listener : listeners) {
          listener.onPositionUnitProgress(position, duration,
              posUnitIndex, C.STATE_PROGRESS_POS_UNIT_FINISH);
        }
      }
    }
  }

  protected void onABProgress(long position, long duration, int abState) {
    for (Listener listener : listeners) {
      listener.onABProgress(position, duration, abState);
    }
    if (abState == C.STATE_PROGRESS_AB_END) {
      if (abLoopProcessing(1) == 2) {
        // =========@finish@=========
        for (Listener listener : listeners) {
          listener.onABProgress(position, duration,
              C.STATE_PROGRESS_AB_FINISH);
        }
      }
    }
  }

  protected void onNotificationRequired(int order) {
    for (Listener listener : listeners) {
      listener.onNotificationRequired(order);
    }
  }

  protected boolean onAudioFocusChange(int focusChange) {
    boolean was_handled = false;
    for (Listener listener : listeners) {
      if (listener.onAudioFocusChange(focusChange)) {
        was_handled = true;
      }
    }
    return was_handled;
  }

  // ============================@PlayProgress@============================
  private final Runnable updatePlayProgressAction = new Runnable() {
    @Override
    public void run() {
      updatePlayProgress(11);
    }
  };

  protected long updatePlayProgressDelayMs = -1;

  @Override
  public P setUpdatePlayProgressDelayMs(long updatePlayProgressDelayMs) {
    this.updatePlayProgressDelayMs = updatePlayProgressDelayMs;
    return returnThis();
  }

  /**
   * @return true: interrupt update Play Progress
   */
  protected boolean updatePlayProgressInterruptConditions() {
    return !isPlayable() || !internalPlayer.isPlaying();
  }

  private boolean updatePlayProgressBaseInterruptConditions = false;

  /**
   * @param updatePlayProgressCommand <ul> <li>0: interrupt update <li>1: go update <li>n: go
   * update(no Change base interrupt conditions) </ul>
   */
  protected void updatePlayProgress(int updatePlayProgressCommand) {
    if (updatePlayProgressCommand == 0) {
      updatePlayProgressBaseInterruptConditions = true;
      handler.removeCallbacks(updatePlayProgressAction);
      return;
    } else if (updatePlayProgressCommand == 1) {
      updatePlayProgressBaseInterruptConditions = false;
    }

    if (updatePlayProgressBaseInterruptConditions || updatePlayProgressInterruptConditions()) {
      //Conditions are not satisfied
      return;
    }

    long position = getCurrentPosition();
    long duration = getDuration();
    if (position >= 0) {
      if (!positionUnitProgress(position, duration) &//use "&"
          !abProgress(position, duration) &//use "&"
          !onPlayProgress(position, duration)) {//called two method
        //No one was handled
        return;
      }
    }

    if (updatePlayProgressBaseInterruptConditions || updatePlayProgressInterruptConditions()) {
      //Conditions are not satisfied
      return;
    }

    handler.removeCallbacks(updatePlayProgressAction);
    // Schedule an update
    long delayMs;
    if (updatePlayProgressDelayMs < 0) {
      delayMs = 1000 - (position % 1000);
      if (delayMs < 200) {
        delayMs += 1000;
      }
    } else {
      delayMs = updatePlayProgressDelayMs;
    }
    handler.postDelayed(updatePlayProgressAction, delayMs);
  }

  // ============================@PositionUnit@============================
  // 0[start~PosUnit~end] - 1[s~PosUnit~e] - 2[s~PosUnit~e] - . .. ... ....
  /**
   * current PositionUnit index
   */
  protected int currentPosUnitIndex = C.INDEX_UNSET;
  protected IPositionUnitList posUnitList;
  /**
   * Whether the current position in the range of position unit <p>...[start~PosUnit~end]...</p>
   */
  protected boolean isInPosUnit;

  @Override
  public P setPositionUnitList(IPositionUnitList posUnitList) {
    this.posUnitList = posUnitList;
    return returnThis();
  }

  protected boolean posUnitListAvailable() {
    return posUnitList != null && !TextUtils.isEmpty(posUnitList.getMediaId()) &&
        posUnitList.getMediaId().equals(currentMediaId) &&
        posUnitList.positionUnitSize() > 0;
  }

  @Override
  public int getCurrentPositionUnitIndex() {
    return currentPosUnitIndex;
  }

  @Override
  public void setCurrentPositionUnitIndex(int posUnitIndex) {
    if (posUnitListAvailable() && Assert.checkIndex(posUnitIndex, posUnitList.positionUnitSize())) {
      isInPosUnit = false;
      currentPosUnitIndex = posUnitIndex - 1;
      long startPos = posUnitList.getStartPosition(posUnitIndex);
      positionUnitProgress(startPos, C.POSITION_UNSET);
    } else {//Re/unset
      currentPosUnitIndex = C.INDEX_UNSET;
      if (posUnitList != null) {
        onPositionUnitProgress(C.POSITION_UNSET, C.POSITION_UNSET,
            currentPosUnitIndex, C.STATE_PROGRESS_POS_UNIT_START);
      }
    }
  }

  /**
   * @param position current play position
   * @return whether someone was handled
   */
  protected boolean positionUnitProgress(long position, long duration) {
    if (posUnitListAvailable()) {
      // =========@End@=========
      if (isInPosUnit && currentPosUnitIndex >= 0 &&
          currentPosUnitIndex < posUnitList.positionUnitSize() &&
          (posUnitList.getEndPosition(currentPosUnitIndex) <= position ||
              (duration <= position && duration > 0 && //last end position
                  posUnitList.getEndPosition(currentPosUnitIndex) >= duration))) {
        isInPosUnit = false;//[s~PosUnit~e]...
        onPositionUnitProgress(position
                - posUnitList.getStartPosition(currentPosUnitIndex),
            posUnitList.getEndPosition(currentPosUnitIndex)
                - posUnitList.getStartPosition(currentPosUnitIndex),
            currentPosUnitIndex, C.STATE_PROGRESS_POS_UNIT_END);
        L.v(TAG, "Pos(" + position + ") PosUnitIndex(" + currentPosUnitIndex + ") End ...");
        return true;//(if condition 1End == 2Start, still return Avoid operating together)
      }

      int posUnitIndexNext = currentPosUnitIndex + 1;
      if (posUnitIndexNext < 0) {
        posUnitIndexNext = 0;
      }
      // =========@Start@=========
      if (posUnitIndexNext < posUnitList.positionUnitSize() &&
          posUnitList.getStartPosition(posUnitIndexNext) <= position) {
        currentPosUnitIndex = posUnitIndexNext;
        isInPosUnit = true;//[s~PosUnit~e]
        onPositionUnitProgress(position
                - posUnitList.getStartPosition(currentPosUnitIndex),
            posUnitList.getEndPosition(currentPosUnitIndex)
                - posUnitList.getStartPosition(currentPosUnitIndex),
            currentPosUnitIndex, C.STATE_PROGRESS_POS_UNIT_START);
        L.v(TAG, "Pos(" + position + ") PosUnitIndex(" + currentPosUnitIndex + ") Start ...");
      }

      // =========@Mid@=========
      if (isInPosUnit && currentPosUnitIndex >= 0 &&
          currentPosUnitIndex < posUnitList.positionUnitSize()) {
        onPositionUnitProgress(position
                - posUnitList.getStartPosition(currentPosUnitIndex),
            posUnitList.getEndPosition(currentPosUnitIndex)
                - posUnitList.getStartPosition(currentPosUnitIndex),
            currentPosUnitIndex, C.STATE_PROGRESS_POS_UNIT_MID);
      }
      return true;
    }
    return false;
  }

  @Override
  public long seekToPositionUnitIndex(int posUnitIndex) {
    if (isPlayable() && posUnitListAvailable()) {
      posUnitIndex = Assert.reviseIndex(posUnitIndex, posUnitList.positionUnitSize());
      setCurrentPositionUnitIndex(posUnitIndex);
      long position = posUnitList.getStartPosition(posUnitIndex);
      if (seekTo(position, 1)) {//not calibrate
        return position;
      }
    }
    return -1;
  }

  @Override
  public int calibrateCurrentPositionUnitIndex(long position) {
    if (internalPlayer != null && posUnitListAvailable()) {
      if (position < 0 || position > getDuration()) {
        position = getCurrentPosition();
      }
      int posUnitIndex = PositionsHelper.searchStartIndex(posUnitList, position);
      setCurrentPositionUnitIndex(posUnitIndex);
      if (posUnitIndex < 0) {
        return -1;//not present
      } else {
        return posUnitIndex;
      }
    }
    return -1;//not present
  }

  // ============================@PositionUnitLoop@============================
  /**
   * [position unit] Loop whether enabled
   */
  protected boolean posUnitLoopEnabled;
  /**
   * [position unit] Loop Mode<ul> <li>-8: infinity loop <li>x(<=0): not loop <li>1++(>0): specified
   * number loop<ul/>
   */
  protected int posUnitLoopMode = 0;
  /**
   * [position unit] loop interval
   */
  protected int posUnitLoopInterval = 0;
  /**
   * [position unit] looped count tag
   */
  protected int posUnitLoopedCount = 0;
  /**
   * specified [position unit] loop List(index)
   */
  protected ArrayList<Integer> posUnitLoopIndexList;

  protected void setPositionUnitLoop(int loopMode, int loopInterval) {
    //Support use C.PARAM_RESET constant reset values.
    if (loopMode == C.PARAM_RESET) {
      loopMode = 0;
    }
    if (loopInterval == C.PARAM_RESET) {
      loopInterval = 0;
    }
    //Support use C.PARAM_ORIGINAL constant keeping the original values
    //...loop after the end of values not reset, so don't have to set up again
    if (loopMode != C.PARAM_ORIGINAL) {
      posUnitLoopMode = loopMode;
    }
    if (loopInterval != C.PARAM_ORIGINAL) {
      posUnitLoopInterval = loopInterval;
    }
    //reset tag
    posUnitLoopedCount = 0;
  }

  @Override
  public P setPositionUnitLoopIndexList(ArrayList<Integer> posUnitLoopIndexList) {
    this.posUnitLoopIndexList = posUnitLoopIndexList;
    return returnThis();
  }

  @Override
  public P setEnabledPositionUnitLoop(boolean enabled, int loopMode, int loopInterval) {
    posUnitLoopEnabled = enabled;
    setPositionUnitLoop(loopMode, loopInterval);
    return returnThis();
  }

  /**
   * @param processFrom <ul> <li>1: from onPositionUnitProgress(..end) <li>2: from onCompletion()
   * <ul/>
   * @return <ul> <li>0: disable <li>1: enable(processing[infinity/specified loop | loop index
   * list]) <li>2: enable(finish[specified loop | not loop]) <ul/>
   */
  protected int positionUnitLoopProcessing(int processFrom) {
    if (posUnitLoopEnabled && (posUnitLoopIndexList == null || posUnitLoopIndexList.size() == 0
        || posUnitLoopIndexList.contains(currentPosUnitIndex))) {
      // ============================@Processing@============================
      if (posUnitLoopMode == -8) {
        // =========@infinity loop[-8]@=========
        pause();
        methodAgent.sendEmptyMessageDelayed(31, posUnitLoopInterval * 1000);
        return 1;//enable(processing[infinity loop])
      } else if (posUnitLoopMode > 0) {
        // =========@specified loop[>0]@=========
        if (posUnitLoopedCount < posUnitLoopMode) {
          pause();
          methodAgent.sendEmptyMessageDelayed(311, posUnitLoopInterval * 1000);
          return 1;//enable(processing[specified loop])
        } else {
          // =========@specified loop finish@=========
          // reset tag(posUnitLoopedCount = 0)
          if (currentPosUnitIndex < posUnitList.positionUnitSize() - 1) {
            //In addition to the last [position unit], go reset.
            //Last [position unit] looped count keep to the process from onCompletion().
            posUnitLoopedCount = 0;
          }

          //[position unit] loop index list (A-B)
          if (posUnitLoopIndexList != null && posUnitLoopIndexList.size() > 0 &&
              currentPosUnitIndex >= posUnitLoopIndexList.get(posUnitLoopIndexList.size() - 1)) {
            if (posUnitLoopInterval > 0) {
              pause();
              Message message = methodAgent.obtainMessage(3111);
              message.arg1 = posUnitLoopIndexList.get(0);
              methodAgent.sendMessageDelayed(message, posUnitLoopInterval * 1000);
            } else {
              seekToPositionUnitIndex(posUnitLoopIndexList.get(0));//B end go play A
            }
            return 1;//enable(processing[loop index list])
          }
          return 2;//enable(finish[specified loop])
        }
      } else {
        // =========@not loop[<=0]@=========
        if (processFrom == 2) {//process from onCompletion().
          return 2;//enable(finish[not loop])
        }
        //[position unit] loop index list (A-B)
        if (posUnitLoopIndexList != null && posUnitLoopIndexList.size() > 0 &&
            currentPosUnitIndex >= posUnitLoopIndexList.get(posUnitLoopIndexList.size() - 1)) {
          if (posUnitLoopInterval > 0) {
            pause();
            Message message = methodAgent.obtainMessage(3111);
            message.arg1 = posUnitLoopIndexList.get(0);
            methodAgent.sendMessageDelayed(message, posUnitLoopInterval * 1000);
          } else {
            seekToPositionUnitIndex(posUnitLoopIndexList.get(0));//B end go play A
          }
          return 1;//enable(processing[loop index list])
        } else {
          if (posUnitLoopInterval > 0) {
            pause();
            methodAgent.sendEmptyMessageDelayed(1, posUnitLoopInterval * 1000);//start()
          }
          return 2;//enable(finish[not loop])
        }
      }
    } else {
      return 0;//disable
    }
  }

  // ============================@Handler@============================
  protected final Handler handler = new Handler(Looper.myLooper());
  /**
   * Simple Method Agent What Code<ul> <li>1: start(); <li>31: seekToPositionUnitIndex(currentPosUnitIndex)
   * <li>311: seekToPositionUnitIndex(currentPosUnitIndex) with posUnitLoopedCount++ <li>3111:
   * seekToPositionUnitIndex(msg.arg1) <li>32: seekTo(abStartPosition) <li>322:
   * seekTo(abStartPosition) with abLoopedCount++<ul/>
   */
  protected final Handler methodAgent = new Handler(new Callback() {
    @Override
    public boolean handleMessage(Message msg) {
      switch (msg.what) {
        case 1:
          start();
          break;
        // =========@PosUnit@=========
        case 31:
          shouldAutoPlayWhenSeekComplete = true;
          seekToPositionUnitIndex(currentPosUnitIndex);
          break;
        case 311:
          shouldAutoPlayWhenSeekComplete = true;
          if (seekToPositionUnitIndex(currentPosUnitIndex) >= 0) {
            posUnitLoopedCount++;
          }
          break;
        case 3111:
          shouldAutoPlayWhenSeekComplete = true;
          seekToPositionUnitIndex(msg.arg1);
          break;
        // =========@AB@=========
        case 32:
          shouldAutoPlayWhenSeekComplete = true;
          seekTo(abStartPosition);
          break;
        case 322:
          shouldAutoPlayWhenSeekComplete = true;
          if (seekTo(abStartPosition)) {
            abLoopedCount++;
          }
          break;
      }
      return true;
    }
  });

  // ============================@A-B@============================
  // [A-B] can be based on [position unit] to quickly implement
  // But here compatible with both can coexist choose alone implement

  /**
   * [A-B] whether enabled
   */
  protected boolean abEnabled;
  /**
   * [A-B] start position
   */
  protected long abStartPosition = C.POSITION_UNSET;
  /**
   * [A-B] end position
   */
  protected long abEndPosition = C.POSITION_UNSET;
  /**
   * [A-B] Loop Mode<ul> <li>-8: infinity loop <li>x(<=0): not loop <li>1++(>0): specified number
   * loop<ul/>
   */
  protected int abLoopMode = 0;
  /**
   * [A-B] loop interval
   */
  protected int abInterval = 0;
  /**
   * [A-B] looped count tag
   */
  protected int abLoopedCount = 0;

  protected boolean abAutoClear = true;

  @Override
  public P setAB(long startPos, long endPos) {
    //Reset Tag
    abLoopedCount = 0;
    abEnabled = false;
    abLoopMode = 0;
    abInterval = 0;

    //Set AB
    abStartPosition = startPos;
    abEndPosition = endPos;
    return returnThis();
  }

  @Override
  public P setABLoop(int loopMode, int loopInterval) {
    //Support use C.PARAM_RESET/UNSET constant reset values.
    if (loopMode == C.PARAM_RESET) {
      loopMode = 0;
    }
    if (loopInterval == C.PARAM_RESET) {
      loopInterval = 0;
    }
    //Support use C.PARAM_ORIGINAL constant keeping the original values
    //[A-B] after the end of values not reset(autoClearAB==false), so don't have to set up again
    if (loopMode != C.PARAM_ORIGINAL) {
      abLoopMode = loopMode;
    }
    if (loopInterval != C.PARAM_ORIGINAL) {
      abInterval = loopInterval;
    }
    return returnThis();
  }

  @Override
  public P setAB(long startPos, long endPos, int loopMode, int loopInterval) {
    setAB(startPos, endPos);
    setABLoop(loopMode, loopInterval);
    return returnThis();
  }

  @Override
  public P setClearAB(boolean autoClear) {
    abAutoClear = autoClear;
    return returnThis();
  }

  @Override
  public void clearAB() {
    setAB(C.POSITION_UNSET, C.POSITION_UNSET);
  }

  /**
   * Whether the current position in the range of [A-B] <p>...[start~AB~end]...</p>
   */
  protected boolean isInAB;

  /**
   * @param position current play position
   * @return whether someone was handled
   */
  protected boolean abProgress(long position, long duration) {
    if (abEnabled) {
      // =========@End@=========
      if (isInAB && (abEndPosition <= position ||
          (duration <= position && duration > 0 && //last end position
              abEndPosition >= duration))) {
        isInAB = false;//[s~AB~e]...
        onABProgress(position - abStartPosition, abEndPosition - abStartPosition,
            C.STATE_PROGRESS_AB_END);
        L.v(TAG, "Progress Pos(" + position + ") AB End ...");
        return true;
      }

      // =========@Start@=========
      if (!isInAB && abStartPosition <= position) {
        isInAB = true;//[s~AB~e]
        onABProgress(position - abStartPosition, abEndPosition - abStartPosition,
            C.STATE_PROGRESS_AB_START);
        L.v(TAG, "Progress Pos(" + position + ") AB Start ...");
      }

      // =========@Mid@=========
      if (isInAB) {
        onABProgress(position - abStartPosition, abEndPosition - abStartPosition,
            C.STATE_PROGRESS_AB_MID);
      }
      return true;
    }
    return false;
  }

  /**
   * @param processFrom <ul> <li>1: from onABProgress(..end) <li>2: from onCompletion() <ul/>
   * @return <ul> <li>0: disable <li>1: enable(processing[infinity/specified loop]) <li>2:
   * enable(finish[specified loop | not loop]) <ul/>
   */
  protected int abLoopProcessing(int processFrom) {
    if (abEnabled) {
      // ============================@Processing@============================
      if (abLoopMode == -8) {
        // =========@infinity loop[-8]@=========
        pause();
        methodAgent.sendEmptyMessageDelayed(32, abInterval * 1000);
        return 1;//enable(processing[infinity loop])
      } else if (abLoopMode > 0) {
        // =========@specified loop[>0]@=========
        if (abLoopedCount < abLoopMode) {
          pause();
          methodAgent.sendEmptyMessageDelayed(322, abInterval * 1000);
          return 1;//enable(processing[specified loop])
        } else {
          // =========@specified loop finish@=========
          // reset tag(abLoopedCount = 0) In every time setAB(..)
          abEnabled = false;
          if (abAutoClear) {
            clearAB();
          }
          if (processFrom != 2) {//!process from onCompletion().
            pause();
          }
          return 2;//enable(finish[specified loop])
        }
      } else {
        // =========@not loop[<=0]@=========
        abEnabled = false;
        if (abAutoClear) {
          clearAB();
        }
        if (processFrom != 2) {//!process from onCompletion().
          pause();
        }
        return 2;//enable(finish[not loop])
      }
    } else {
      return 0;//disable
    }
  }

  // ============================@MediaQueue@============================
  private IMediaQueue mediaQueue;
  protected String currentMediaId;

  @Override
  public void setMediaQueue(IMediaQueue mediaQueue) {
    this.mediaQueue = mediaQueue;
    mediaQueue.init(this);
  }

  @Override
  public IMediaQueue getMediaQueue() {
    if (mediaQueue == null) {
      setMediaQueue(new MediaQueue());
    }
    return mediaQueue;
  }

  @Override
  public String getCurrentMediaId() {
    return currentMediaId;
  }

  //<!--Using a WifiLock For KMedia Player-->
  //<uses-permission android:name="android.permission.WAKE_LOCK"/>
  protected WifiManager.WifiLock wifiLock;

  @RequiresPermission(permission.WAKE_LOCK)
  @Override
  public P setEnabledWifiLock(boolean enabled) {
    if (enabled) {
      if (wifiLock == null && context != null) {
        WifiManager wm = (WifiManager) context.getApplicationContext()
            .getSystemService(Context.WIFI_SERVICE);
        if (wm != null) {
          wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, "k_media_lock");
        }
      } else if (wifiLock != null && !wifiLock.isHeld()) {
        wifiLock.acquire();
      }
    } else {
      if (wifiLock != null && wifiLock.isHeld()) {
        wifiLock.release();
      }
      wifiLock = null;
    }
    return returnThis();
  }

  // ============================@AudioFocus@============================
  protected AudioMgrHelper audioMgrHelper;
  private int currentAudioFocusState = AudioManager.AUDIOFOCUS_LOSS;
  protected boolean enabledAudioFocusManage = false;

  @Override
  public P setEnabledAudioFocusManage(boolean enabled) {
    this.enabledAudioFocusManage = enabled;
    return returnThis();
  }

  @Override
  public AudioMgrHelper getAudioMgrHelper() {
    return audioMgrHelper;
  }

  private void tryToGetAudioFocus() {
    if (enabledAudioFocusManage && audioMgrHelper != null
        && currentAudioFocusState != AudioManager.AUDIOFOCUS_GAIN) {
      currentAudioFocusState = audioMgrHelper.requestAudioFocus(getComponentListener());
    }
  }

  private void giveUpAudioFocus() {
    if (enabledAudioFocusManage && audioMgrHelper != null) {
      currentAudioFocusState = audioMgrHelper.abandonAudioFocus(getComponentListener());
    }
  }

  /**
   * @return forced to pause
   */
  protected boolean configureAudioFocus() {
    if (!enabledAudioFocusManage) {
      return false;
    }
    if (currentAudioFocusState == AudioManager.AUDIOFOCUS_LOSS
        || currentAudioFocusState == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
      pause();
      return true;
    } else {//AUDIOFOCUS_GAIN, AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK
      registerComponentReceiver();

      if (currentAudioFocusState == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
        setVolume(0.2f);//DUCK
      } else {
        setVolume(1.0f);//NORMAL
      }
      return false;
    }
  }

  // ============================@Component Listener@============================
  protected ComponentListener componentListener;

  protected ComponentListener getComponentListener() {
    if (componentListener == null) {
      componentListener = new ComponentListener();
    }
    return componentListener;
  }

  protected class ComponentListener implements AudioManager.OnAudioFocusChangeListener {

    @Override
    public void onAudioFocusChange(int focusChange) {
      currentAudioFocusState = focusChange;
      L.d(TAG, "onAudioFocusChange. focusChange(" + focusChange + ")");
      if (!APlayer.this.onAudioFocusChange(focusChange)) {
        //If there is no one handled, with so me handle
        configureAudioFocus();
      }
    }
  }


  // ============================@Component Receiver/IntentFilter@============================
  private boolean componentReceiverRegistered;

  protected void registerComponentReceiver() {
    try {
      if (!componentReceiverRegistered && context != null) {
        context.registerReceiver(getComponentReceiver(), getComponentIntentFilter());
        componentReceiverRegistered = true;
      }
    } catch (Exception e) {
      L.printStackTrace(e);
    }
  }

  protected void unregisterComponentReceiver() {
    try {
      if (componentReceiverRegistered && context != null) {
        context.unregisterReceiver(getComponentReceiver());
        componentReceiverRegistered = false;
      }
    } catch (Exception e) {
      L.printStackTrace(e);
    }
  }

  // ============================@Get
  protected IntentFilter componentIntentFilter;

  protected IntentFilter getComponentIntentFilter() {
    if (componentIntentFilter == null) {
      componentIntentFilter = new IntentFilter();
      componentIntentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    }
    return componentIntentFilter;
  }

  protected ComponentReceiver componentReceiver;

  protected ComponentReceiver getComponentReceiver() {
    if (componentReceiver == null) {
      componentReceiver = new ComponentReceiver();
    }
    return componentReceiver;
  }

  protected class ComponentReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
      if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
        L.d(TAG, "Headphones disconnected.");
        if (isPlaying()) {
          pause();
        }
      }
    }
  }

  // ============================@Assist@============================
  protected String TAG = L.makeTag("APlayer");

  protected Context context;

  /**
   * Support, base on context of function Module. <ul> <li>{@link #registerComponentReceiver()}
   * <li>{@link #unregisterComponentReceiver()} <li>{@link #setEnabledAudioFocusManage(boolean)}
   * <li>{@link #setEnabledWifiLock(boolean enabled)} <ul/>
   *
   * @param context Use for internal register/unregisterReceiver, getSystemService, ...
   */
  public void supportBaseOnContextOfFunctionModule(@NonNull Context context) {
    this.context = context;//↓ Default enabled
    setEnabledAudioFocusManage(true).audioMgrHelper = AudioMgrHelper.i().init(context);
  }
}