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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.view.SurfaceView;
import android.view.TextureView;
import com.jcodeing.kmedia.assist.AudioMgrHelper;
import com.jcodeing.kmedia.definition.IMediaItem;
import com.jcodeing.kmedia.definition.IMediaQueue;
import com.jcodeing.kmedia.definition.IPositionUnitList;
import com.jcodeing.kmedia.service.PlayerService;
import com.jcodeing.kmedia.service.PlayerService.PlayerBinder;
import com.jcodeing.kmedia.utils.L;
import java.util.ArrayList;

/**
 * Abstract Player Binding
 *
 * @param <P> subClass
 * @see PlayerBinding
 */
public abstract class APlayerBinding<P extends APlayerBinding> implements IPlayer<P> {

  /**
   * Sub Class Override
   * <pre>
   * public class PlayerBinding extends APlayerBinding&#60PlayerBinding> {
   *
   *    &#64Override
   *    protected PlayerBinding returnThis() {
   *      return this;
   *    }
   * }
   * </pre>
   */
  protected abstract P returnThis();

  // ============================@Service@============================
  private PlayerService mService;

  protected PlayerService service() {
    return mService;
  }

  // ============================@Bind
  private boolean mBound = false;

  protected boolean isBound() {
    return mBound;
  }

  private ServiceConnection mConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName className,
        IBinder service) {
      if (service instanceof PlayerBinder) {
        PlayerBinder binder = (PlayerBinder) service;
        mService = binder.getService();
        // =====@Bind Player
        if (!binder.isBoundPlayer()) {
          binder.bindPlayer(mBindPlayer.onBindPlayer());
        }
        // =====@First Binding
        if (!binder.isFirstBinding()) {
          if (mBindingListener != null) {
            mBindingListener.onFirstBinding(mService);
          }
          binder.firstBindingFinish();
        }
        mBound = true;
        // =====@Binding Finish
        if (mBindingListener != null) {
          mBindingListener.onBindingFinish();
        }
      }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
      mBound = false;
    }
  };

  private final BindPlayer mBindPlayer;

  public interface BindPlayer {

    /**
     * Player service bind player, call back.(if is bound, not call back)
     *
     * @return new Player(context).init(new ...MediaPlayer(context));
     */
    IPlayer onBindPlayer();
  }

  private BindingListener mBindingListener;

  public interface BindingListener {

    /**
     * First binding call back.(if first binding finish, not call back) <p /> Can do something
     * player service init operation.
     */
    void onFirstBinding(PlayerService service);

    /**
     * Binding finish. <p /> Can play.
     */
    void onBindingFinish();
  }

  // ============================@Life@============================
  protected final Context mContext;
  private final Intent mIntent;

  /**
   * @param playerServiceClass *.class[extends PlayerService]
   * @param bindPlayer new BindPlayer(){onBindPlayer()-> return Object[extends IPlayer]
   */
  public <PService extends PlayerService> APlayerBinding(Context context,
      Class<PService> playerServiceClass,
      BindPlayer bindPlayer) {
    mContext = context;
    mBindPlayer = bindPlayer;
    // =========@Init@=========
    mIntent = new Intent(mContext, playerServiceClass);
    // =========@Start
    mContext.startService(mIntent);
    // =========@Bind
    mContext.bindService(mIntent, mConnection, Context.BIND_AUTO_CREATE);
  }

  public <PService extends PlayerService> APlayerBinding(Context context,
      Class<PService> playerServiceClass,
      BindPlayer bindPlayer, BindingListener serviceConnected) {
    this(context, playerServiceClass, bindPlayer);
    mBindingListener = serviceConnected;
  }


  public void onDestroy() {
    try {
      mContext.stopService(mIntent);
      if (mBound) {
        mContext.unbindService(mConnection);
        mBound = false;
      }
    } catch (Exception e) {
      L.printStackTrace(e);
    }
  }

  public IPlayer player() {
    if (mBound) {
      return mService.player();
    }
    return null;
  }

  // ============================@IPlayer@============================
  // ============================@Control
  @Override
  public boolean start() {
    return mBound && mService.player().start();
  }

  @Override
  public boolean pause() {
    return mBound && mService.player().pause();
  }

  @Override
  public boolean seekTo(long ms, int processingLevel) {
    return mBound && mService.player().seekTo(ms, processingLevel);
  }

  @Override
  public boolean seekTo(long ms) {
    return mBound && mService.player().seekTo(ms);
  }

  @Override
  public boolean fastForwardRewind(long ms) {
    return mBound && mService.player().fastForwardRewind(ms);
  }

  @Override
  public void stop() {
    if (mBound) {
      mService.player().stop();
    }
  }

  @Override
  public void reset() {
    if (mBound) {
      mService.player().reset();
    }
  }

  @Override
  public void release() {
    if (mBound) {
      mService.player().release();
    }
  }

  // ============================@Video@============================
  @Override
  public void setVideo(SurfaceView surfaceView) {
    if (mBound) {
      mService.player().setVideo(surfaceView);
    }
  }

  @Override
  public void setVideo(TextureView textureView) {
    if (mBound) {
      mService.player().setVideo(textureView);
    }
  }

  @Override
  public void clearVideo() {
    if (mBound) {
      mService.player().clearVideo();
    }
  }

  // ============================@Set/Get/Is
  @Override
  public void setVolume(float volume) {
    if (mBound) {
      mService.player().setVolume(volume);
    }
  }

  @Override
  public float getVolume() {
    if (mBound) {
      return mService.player().getVolume();
    }
    return 1;
  }

  @Override
  public long getCurrentPosition() {
    if (mBound) {
      return mService.player().getCurrentPosition();
    }
    return 0;
  }

  @Override
  public long getDuration() {
    if (mBound) {
      return mService.player().getDuration();
    }
    return 0;
  }

  @Override
  public boolean isPlaying() {
    return mBound && mService.player().isPlaying();
  }

  @Override
  public float getPlaybackSpeed() {
    if (mBound) {
      return mService.player().getPlaybackSpeed();
    }
    return 0;
  }

  @Override
  public boolean setPlaybackSpeed(float speed) {
    return mBound && mService.player().setPlaybackSpeed(speed);
  }

  @Override
  public int getPlaybackState() {
    if (mBound) {
      return mService.player().getPlaybackState();
    }
    return STATE_IDLE;
  }

  @Override
  public boolean isPlayable() {
    return mBound && mService.player().isPlayable();
  }

  // ============================@Listener
  @Override
  public void addListener(Listener listener) {
    if (mBound) {
      mService.player().addListener(listener);
    }
  }

  @Override
  public void removeListener(Listener listener) {
    if (mBound) {
      mService.player().removeListener(listener);
    }
  }

  // ============================@IPlayerExtend@============================
  // ============================@Player
  @Override
  public IMediaPlayer internalPlayer() {
    if (mBound) {
      return mService.player().internalPlayer();
    }
    return null;
  }

  @Override
  public P init(IMediaPlayer mediaPlayer) {
    if (mBound) {
      mService.player().init(mediaPlayer);
    }
    return returnThis();
  }

  @Override
  public boolean prepare(Uri uri) {
    return mBound && mService.player().prepare(uri);
  }

  @Override
  public boolean prepare(IMediaItem mediaItem) {
    return mBound && mService.player().prepare(mediaItem);
  }

  @Override
  public boolean prepareMediaId(String mediaId) {
    return mBound && mService.player().prepareMediaId(mediaId);
  }

  @Override
  public boolean play() {
    return mBound && mService.player().play();
  }

  @Override
  public boolean play(Uri uri) {
    return mBound && mService.player().play(uri);
  }

  @Override
  public boolean play(IMediaItem mediaItem) {
    return mBound && mService.player().play(mediaItem);
  }

  @Override
  public boolean playMediaId(String mediaId) {
    return mBound && mService.player().playMediaId(mediaId);
  }

  @Override
  public void shutdown() {
    if (mBound) {
      mService.player().shutdown();
    }
  }

  // ============================@Control
  @Override
  public P shouldAutoPlayWhenSeekComplete(boolean shouldAutoPlayWhenSeekComplete) {
    if (mBound) {
      mService.player().shouldAutoPlayWhenSeekComplete(shouldAutoPlayWhenSeekComplete);
    }
    return returnThis();
  }

  @Override
  public void seekToPending(long ms) {
    if (mBound) {
      mService.player().seekToPending(ms);
    }
  }

  @Override
  public long seekToProgress(int progress, int progressMax) {
    if (mBound) {
      return mService.player().seekToProgress(progress, progressMax);
    }
    return -1;
  }

  // ============================@Set/Get/Is
  @Override
  public P setUpdatePlayProgressDelayMs(long updatePlayProgressDelayMs) {
    if (mBound) {
      mService.player().setUpdatePlayProgressDelayMs(updatePlayProgressDelayMs);
    }
    return returnThis();
  }

  @Override
  public String getCurrentMediaId() {
    if (mBound) {
      return mService.player().getCurrentMediaId();
    }
    return null;
  }

  @Override
  public P setEnabledWifiLock(boolean enabled) {
    if (mBound) {
      mService.player().setEnabledWifiLock(enabled);
    }
    return returnThis();
  }

  @Override
  public P setEnabledAudioFocusManage(boolean enabled) {
    if (mBound) {
      mService.player().setEnabledAudioFocusManage(enabled);
    }
    return returnThis();
  }

  @Override
  public AudioMgrHelper getAudioMgrHelper() {
    if (mBound) {
      return mService.player().getAudioMgrHelper();
    }
    return null;
  }

  // ============================@PositionUnit
  @Override
  public P setPositionUnitList(IPositionUnitList posUnitList) {
    if (mBound) {
      mService.player().setPositionUnitList(posUnitList);
    }
    return returnThis();
  }

  @Override
  public int getCurrentPositionUnitIndex() {
    if (mBound) {
      return mService.player().getCurrentPositionUnitIndex();
    }
    return -1;
  }

  @Override
  public void setCurrentPositionUnitIndex(int posUnitIndex) {
    if (mBound) {
      mService.player().setCurrentPositionUnitIndex(posUnitIndex);
    }
  }

  @Override
  public long seekToPositionUnitIndex(int posUnitIndex) {
    if (mBound) {
      return mService.player().seekToPositionUnitIndex(posUnitIndex);
    }
    return -1;
  }

  @Override
  public int calibrateCurrentPositionUnitIndex(long position) {
    if (mBound) {
      return mService.player().calibrateCurrentPositionUnitIndex(position);
    }
    return -1;
  }

  @Override
  public P setEnabledPositionUnitLoop(boolean enabled, int loopMode, int loopInterval) {
    if (mBound) {
      mService.player().setEnabledPositionUnitLoop(enabled, loopMode, loopInterval);
    }
    return returnThis();
  }

  @Override
  public P setPositionUnitLoopIndexList(ArrayList<Integer> posUnitLoopIndexList) {
    if (mBound) {
      mService.player().setPositionUnitLoopIndexList(posUnitLoopIndexList);
    }
    return returnThis();
  }

  // ============================@AB
  @Override
  public P setAB(long startPos, long endPos) {
    if (mBound) {
      mService.player().setAB(startPos, endPos);
    }
    return returnThis();
  }

  @Override
  public P setABLoop(int loopMode, int loopInterval) {
    if (mBound) {
      mService.player().setABLoop(loopMode, loopInterval);
    }
    return returnThis();
  }

  @Override
  public P setAB(long startPos, long endPos, int loopMode, int loopInterval) {
    if (mBound) {
      mService.player().setAB(startPos, endPos, loopMode, loopInterval);
    }
    return returnThis();
  }

  @Override
  public P setClearAB(boolean autoClear) {
    if (mBound) {
      mService.player().setClearAB(autoClear);
    }
    return returnThis();
  }

  @Override
  public void clearAB() {
    if (mBound) {
      mService.player().clearAB();
    }
  }

  // ============================@MediaQueue
  @Override
  public void setMediaQueue(IMediaQueue mediaQueue) {
    if (mBound) {
      mService.player().setMediaQueue(mediaQueue);
    }
  }

  @Override
  public IMediaQueue getMediaQueue() {
    if (mBound) {
      return mService.player().getMediaQueue();
    }
    return null;
  }
}