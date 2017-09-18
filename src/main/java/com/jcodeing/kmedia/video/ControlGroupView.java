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
package com.jcodeing.kmedia.video;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import com.jcodeing.kmedia.IPlayer;
import com.jcodeing.kmedia.IPlayerBase;

/**
 * Player View. below simple use for layout.
 * <pre>
 * Attrs
 * ==========================================
 * app:show_timeout="5000"(5s)
 * app:rewind_increment="5000"(5s)
 * app:fast_forward_increment="15000"(15s)
 * app:use_gesture_detector="false"
 * app:default_control_layer_id="..."
 * app:...
 * </pre>
 *
 * @see PlayerView
 * @see ControlLayerView
 */
public class ControlGroupView extends AControlGroupView {

  public ControlGroupView(Context context) {
    this(context, null);
  }

  public ControlGroupView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ControlGroupView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  public ViewGroup.LayoutParams getLayoutParams() {
    ViewGroup.LayoutParams layoutParams = super.getLayoutParams();
    if (layoutParams == null) {
      layoutParams = new ViewGroup.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }
    return layoutParams;
  }

  @Override
  protected ComponentListener initGetComponentListener() {
    return new MyComponentListener();
  }

  @Override
  public void setPlayer(IPlayer player) {
    if (this.player == player) {
      return;
    }
    if (this.player != null) {
      this.player.removeListener(componentListener);
    }
    this.player = player;

    if (player != null) {
      player.addListener(componentListener);
      updateAll();
    } else {
      hide(false);
    }
  }

  /**
   * Sets the rewind increment in milliseconds.
   *
   * @param rewindMs The rewind increment in milliseconds. A non-positive value will cause the
   * rewind button to be disabled.
   */
  @Override
  public void setRewindIncrementMs(int rewindMs) {
    super.setRewindIncrementMs(rewindMs);
    updateNavigation();
  }

  /**
   * Sets the fast forward increment in milliseconds.
   *
   * @param fastForwardMs The fast forward increment in milliseconds. A non-positive value will
   * cause the fast forward button to be disabled.
   */
  @Override
  public void setFastForwardIncrementMs(int fastForwardMs) {
    super.setFastForwardIncrementMs(fastForwardMs);
    updateNavigation();
  }


  /**
   * Shows the playback controls. If {@link #getShowTimeoutMs()} is positive then the controls will
   * be automatically hidden after this duration of time has elapsed without user input.
   */
  public boolean show() {
    if (super.show()) {
      updateAll();
      requestPlayPauseFocus();
      return true;
    }
    return false;
  }

  @Override
  protected void updateAll() {
    super.updateAll();
    updatePlayPauseButton();
    updateNavigation();
  }

  private void updatePlayPauseButton() {
    if (!isAttachedToWindow || !isPlayable()) {
      return;
    }
    boolean playing = player.isPlaying();
    if (isVisibleByPlayController()) {
      boolean requestPlayPauseFocus = false;
      if (playView != null) {
        requestPlayPauseFocus = playing && playView.isFocused();
        playView.setVisibility(playing ? GONE : VISIBLE);
      }
      if (pauseView != null) {
        requestPlayPauseFocus |= //Both sides to false is false
            !playing && pauseView.isFocused();
        pauseView.setVisibility(!playing ? GONE : VISIBLE);
      }
      if (requestPlayPauseFocus) {
        requestPlayPauseFocus();
      }
    }
  }

  private void updateNavigation() {
    if (!isVisibleByPlayController() || !isAttachedToWindow) {
      return;
    }
    setButtonEnabled(fastForwardMs > 0 && isPlayable(), fastForwardView);
    setButtonEnabled(rewindMs > 0 && isPlayable(), rewindView);
    if (progressBar != null) {
      progressBar.setEnabled(isPlayable());
    }
    if (progressAny != null) {
      progressAny.setEnabled(isPlayable());
    }
  }

  private void requestPlayPauseFocus() {
    boolean playing = player != null && player.isPlaying();
    if (!playing && playView != null) {
      playView.requestFocus();
    } else if (playing && pauseView != null) {
      pauseView.requestFocus();
    }
  }

  private void setButtonEnabled(boolean enabled, View view) {
    if (view == null) {
      return;
    }
    view.setEnabled(enabled);
    if (Build.VERSION.SDK_INT >= 11) {
      setViewAlphaV11(view, enabled ? 1f : 0.3f);
      view.setVisibility(VISIBLE);
    } else {
      view.setVisibility(enabled ? VISIBLE : INVISIBLE);
    }
  }

  @TargetApi(11)
  private void setViewAlphaV11(View view, float alpha) {
    view.setAlpha(alpha);
  }

  @Override
  public void onAttachedToWindow() {
    super.onAttachedToWindow();
    updateAll();
  }

  @Override
  public void onDetachedFromWindow() {
    super.onDetachedFromWindow();
  }


  // ============================@ComponentListener@============================
  private final class MyComponentListener extends ComponentListener {

    @Override
    public boolean onPlayProgress(long position, long duration) {
      if (isVisibleByPlayController() && isAttachedToWindow) {
        updateProgressView(position, duration);
      }
      return true;
    }

    @Override
    public void onBufferingUpdate(int percent) {
      super.onBufferingUpdate(percent);
      if (progressBar != null) {
        progressBar.setSecondaryProgress(percent);
      }
      if (progressAny != null) {
        progressAny.setSecondaryProgress(percent);
      }
    }

    @Override
    public void onStateChanged(int playbackState) {
      if (playbackState == IPlayerBase.STATE_READY) {
        if (!player.isPlaying()) {
          show(true, false);//not playing, show with not hide after timeout
        } else if (isVisibleByInteractionArea()) {
          hideAfterTimeout(1000);//[1s] playing, quick go to hide
        }
      }
      updatePlayPauseButton();
      updateProgressView(-1, -1);
      showBufferingView(playbackState == IPlayer.STATE_BUFFERING);
    }

    @Override
    public void onPrepared() {
      super.onPrepared();
      updateNavigation();
    }
  }
}