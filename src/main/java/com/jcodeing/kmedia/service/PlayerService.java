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
package com.jcodeing.kmedia.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import com.jcodeing.kmedia.IPlayer;
import com.jcodeing.kmedia.PlayerListener;
import com.jcodeing.kmedia.worker.ANotifier;

public class PlayerService extends Service {

  // ============================@Binder@============================
  protected IBinder mBinder;
  protected boolean mFirstBinding;

  public class PlayerBinder extends Binder {

    public boolean isBoundPlayer() {
      return player() != null;
    }

    public void bindPlayer(IPlayer player) {
      setPlayer(player);
    }

    public boolean isFirstBinding() {
      return mFirstBinding;
    }

    public void firstBindingFinish() {
      mFirstBinding = true;
    }

    public PlayerService getService() {
      return PlayerService.this;
    }
  }

  @Override
  public IBinder onBind(Intent intent) {
    if (mBinder == null) {
      mBinder = new PlayerBinder();
    }
    return mBinder;
  }

  // ============================@Player@============================
  protected IPlayer mPlayer;

  public IPlayer player() {
    return mPlayer;
  }

  protected void setPlayer(IPlayer player) {
    mPlayer = player;
    player.addListener(componentListener);
  }

  // ============================@Notifier@============================
  private ANotifier notifier;

  public void setNotifier(ANotifier notifier) {
    this.notifier = notifier;
    notifier.init(this);
    //Listener
    if (componentListener == null) {
      componentListener = new ComponentListener();
    }
    if (mPlayer != null) {
      mPlayer.addListener(componentListener);
    }
  }

  // ============================@ComponentListener@============================
  private ComponentListener componentListener;

  protected class ComponentListener extends PlayerListener {

    @Override
    public void onNotificationRequired(int order) {
      super.onNotificationRequired(order);
      if (notifier != null) {
        if (order == 0) {//stop
          notifier.stopNotification();
        } else if (order == 1) {//start
          notifier.startNotification();
        } else if (order == 2) {//update
          //If has started go to update
          if (notifier.startNotification() == 2) {
            notifier.updateNotification();
          }
        }
      }
    }

    @Override
    public void onRemoved() {
      super.onRemoved();
      if (notifier != null) {//resident
        player().addListener(this);
      }
    }
  }
}