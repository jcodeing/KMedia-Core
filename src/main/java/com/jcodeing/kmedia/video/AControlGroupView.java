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

import android.content.Context;
import android.content.res.TypedArray;
import android.os.SystemClock;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.jcodeing.kmedia.IPlayer;
import com.jcodeing.kmedia.PlayerListener;
import com.jcodeing.kmedia.R;
import com.jcodeing.kmedia.assist.C;
import com.jcodeing.kmedia.assist.GestureDetectorHelper;
import com.jcodeing.kmedia.assist.GestureListenerExtend;
import com.jcodeing.kmedia.utils.Assert;
import com.jcodeing.kmedia.utils.L;
import com.jcodeing.kmedia.utils.TimeProgress;
import com.jcodeing.kmedia.view.ProgressAny;

public abstract class AControlGroupView extends FrameLayout {

  public AControlGroupView(Context context) {
    this(context, null);
  }

  public AControlGroupView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public AControlGroupView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    showTimeoutMs = DEFAULT_SHOW_TIMEOUT_MS;
    rewindMs = DEFAULT_REWIND_MS;
    fastForwardMs = DEFAULT_FAST_FORWARD_MS;
    currentControlLayerId = R.id.k_ctrl_layer_port;
    boolean useGestureDetector = false;

    if (attrs != null) {
      TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
          R.styleable.AControlGroupView, 0, 0);
      try {
        showTimeoutMs =
            a.getInt(R.styleable.AControlGroupView_show_timeout, showTimeoutMs);
        rewindMs =
            a.getInt(R.styleable.AControlGroupView_rewind_increment, rewindMs);
        fastForwardMs =
            a.getInt(R.styleable.AControlGroupView_fast_forward_increment, fastForwardMs);
        currentControlLayerId =
            a.getResourceId(R.styleable.AControlGroupView_default_control_layer_id,
                currentControlLayerId);
        useGestureDetector =
            a.getBoolean(R.styleable.AControlGroupView_use_gesture_detector,
                false);
      } finally {
        a.recycle();
      }
    }

    if (useGestureDetector) {
      setUseGestureDetector(true, null);
    }//use default gesture detector

    init();
    setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
  }

  protected void init() {
    componentListener = initGetComponentListener();
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    initControlLayer();
  }

  protected boolean isAttachedToWindow;

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    isAttachedToWindow = true;
    // =========@Do something
    if (hideAtMs != C.TIME_UNSET) {
      long delayMs = hideAtMs - SystemClock.uptimeMillis();
      if (delayMs <= 0) {
        hide(false);
      } else {
        postDelayed(hideAction, delayMs);
      }
    }
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    isAttachedToWindow = false;
    // =========@Do something
    removeCallbacks(hideAction);
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    return isLocked || super.dispatchKeyEvent(event);
  }//isLocked intercept dispatch KeyEvent

  @Override
  public boolean dispatchTouchEvent(MotionEvent ev) {
    if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
      removeCallbacks(hideAction);
    } else if (ev.getActionMasked() == MotionEvent.ACTION_UP) {
      hideAfterTimeout();
    }
    return super.dispatchTouchEvent(ev);
  }

  public boolean onTouchEvent(MotionEvent ev) {
    if (!isLocked && gestureDetector != null) {
      gestureDetector.onTouchEvent(ev);
      if (localGestureListener != null) {
        localGestureListener.onTouchEvent(ev);
      }
      return true;
    }
    if (ev.getActionMasked() != MotionEvent.ACTION_DOWN) {
      return false;
    }
    if (isVisibleByInteractionArea()) {
      hide();
    } else {
      show();
    }
    return true;
  }

  // ============================@Gesture@============================
  protected GestureDetector gestureDetector;
  protected LocalGestureListener localGestureListener;

  /**
   * Set the enabled of this control group use gesture detector.
   *
   * @param enabled True if this gesture detector is enabled, false otherwise.
   * @param gestureDetector if null, use default gesture detector
   */
  public void setUseGestureDetector(boolean enabled, GestureDetector gestureDetector) {
    if (enabled) {
      if (this.gestureDetector == null) {
        if (gestureDetector == null) {
          this.gestureDetector = new GestureDetector(getContext(),
              localGestureListener == null ?
                  localGestureListener = new LocalGestureListener() : localGestureListener);
        } else {
          this.gestureDetector = gestureDetector;
        }
      } else if (gestureDetector != null) {//&& this.gestureDetector != null
        this.gestureDetector = gestureDetector;
      }
    } else {
      this.gestureDetector = null;
    }
  }

  /**
   * Set gesture proxy, base on use default gesture detector.
   *
   * @param gestureProxy {@link GestureDetectorHelper.IGestureListenerExtendProxy}
   * @see #setUseGestureDetector(boolean, GestureDetector)
   */
  public void setGestureProxy(GestureDetectorHelper.IGestureListenerExtendProxy gestureProxy) {
    if (localGestureListener == null) {//use default gesture detector
      setUseGestureDetector(true, gestureDetector = null);
    }
    localGestureListener.setGestureProxy(gestureProxy);
  }

  class LocalGestureListener extends GestureListenerExtend {

    boolean isAdjustsShowTipsView;
    long adjustsPendingPosition = C.POSITION_UNSET;//unset
    long adjustsDuration = C.POSITION_UNSET;//unset

    @Override
    public boolean onTouchEvent(MotionEvent e) {
      if (!super.onTouchEvent(e) && e.getAction() == MotionEvent.ACTION_UP
          && isAdjustsShowTipsView) {
        // =========@Adjusts End@=========
        // =========@Hide Tips View
        showTipsView(isAdjustsShowTipsView = false, null, -1);
        // =========@Handle Pending Position
        if (adjustsPendingPosition >= 0) {//!= unset
          seekTo(adjustsPendingPosition);
          adjustsPendingPosition = C.POSITION_UNSET;//reset
          adjustsDuration = C.POSITION_UNSET;//reset
          dragging = false;
        }
      }
      return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
      setGestureAreaWidth(getWidth());
      return super.onScroll(e1, e2, distanceX, distanceY);
    }

    @Override
    public boolean onScrollLongitudinalLeft(MotionEvent e1, MotionEvent e2, MotionEvent e3,
        float distanceX, float distanceY) {
      if (!super.onScrollLongitudinalLeft(e1, e2, e3, distanceX, distanceY)) {
        adjustsVolume((e3.getY() - e2.getY()) / getHeight(), isAdjustsShowTipsView = true);
      }// (last Y - current Y) / height -> get increment
      return true;
    }

    @Override
    public boolean onScrollLongitudinalRight(MotionEvent e1, MotionEvent e2, MotionEvent e3,
        float distanceX, float distanceY) {
      if (!super.onScrollLongitudinalRight(e1, e2, e3, distanceX, distanceY)) {
        adjustsVolume((e3.getY() - e2.getY()) / getHeight(), isAdjustsShowTipsView = true);
      }// (last Y - current Y) / height -> get increment
      return true;
    }

    @Override
    public boolean onScrollCrosswise(MotionEvent e1, MotionEvent e2, MotionEvent e3,
        float distanceX, float distanceY) {
      if (!super.onScrollCrosswise(e1, e2, e3, distanceX, distanceY)) {
        adjustsPosition((e2.getX() - e3.getX()) / getWidth() / 3, isAdjustsShowTipsView = true);
      }// (last X - current X) / width / 3  -> get increment [/3]: narrowing scroll adjusts position
      return true;
    }

    @Override
    public boolean onDoubleClick(MotionEvent e) {
      if (!super.onDoubleClick(e)) {
        play(2);
      }
      return true;
    }

    @Override
    public boolean onSingleClick(MotionEvent e) {
      if (!super.onSingleClick(e)) {
        if (isVisibleByInteractionArea()) {
          hide();
        } else {
          show();
        }
      }
      return true;
    }

    void adjustsVolume(float ascendingRatio, boolean isShowTips) {
      if (player != null) {
        float v = player.getVolume();
        v = ascendingRatio * 1 + v;//ascendingRatio * max + last
        if (v > 1) {
          v = 1;
        } else if (v < 0) {
          v = 0;
        }
        player.setVolume(v);

        if (!isShowTips) {
          return;
        }//Show Tips
        if (v == 0) {
          showTipsView(true, "0%", R.drawable.k_ic_volume_mute);
        } else if (ascendingRatio < 0) {
          showTipsView(true, Math.round(v / 1 * 100) + "%", R.drawable.k_ic_volume_down);
        } else if (ascendingRatio > 0) {
          showTipsView(true, Math.round(v / 1 * 100) + "%", R.drawable.k_ic_volume_up);
        }
      }
    }

    /**
     * Don't immediately seekTo, in gesture end ACTION_UP action handle pending.
     */
    void adjustsPosition(float ascendingRatio, boolean isShowTips) {
      if (isPlayable()) {
        dragging = true;

        if (adjustsPendingPosition < 0) {
          adjustsPendingPosition = player.getCurrentPosition();
        }
        if (adjustsDuration < 0) {
          adjustsDuration = (adjustsDuration = player.getDuration()) < 0 ? 0 : adjustsDuration;
        }
        long position = (long) (ascendingRatio * adjustsDuration)
            + adjustsPendingPosition; //ascendingRatio * max + last
        position = Assert.reviseInterval(position, 0, adjustsDuration, false, false);
        if (adjustsPendingPosition != position) {
          adjustsPendingPosition = position;
        } else {
          return;
        }

        updateProgressView(position, C.PARAM.FORCE, C.POSITION_UNSET, C.PARAM.UNSET);

        if (!isShowTips) {
          return;
        }//Show Tips
        if (ascendingRatio < 0) {
          showTipsView(true,
              TimeProgress.stringForTime(adjustsPendingPosition) + "/" + TimeProgress
                  .stringForTime(adjustsDuration),
              R.drawable.k_ic_rew);
        } else if (ascendingRatio > 0) {
          showTipsView(true,
              TimeProgress.stringForTime(adjustsPendingPosition) + "/" + TimeProgress
                  .stringForTime(adjustsDuration),
              R.drawable.k_ic_ffwd);
        }
      }
    }
  }

  // ============================@Player@============================
  protected IPlayer player;

  /**
   * @return the player currently being controlled by this view, or null if no player is set.
   */
  public IPlayer getPlayer() {
    return player;
  }

  /**
   * Sets the {@link IPlayer} to control.
   *
   * @param player the {@code IPlayer} to control.
   */
  public abstract void setPlayer(IPlayer player);


  // ============================@Initialization@============================
  protected void initControlLayer() {
    //controlLayers
    int count = getChildCount();
    if (count > controlLayers.size()) {
      for (int i = 0; i < count; i++) {
        View child = getChildAt(i);
        if (child instanceof AControlLayerView) {
          AControlLayerView controlLayerView = controlLayers.get(child.getId());
          if (controlLayerView == null) {
            controlLayerView = (AControlLayerView) child;
            controlLayers.put(child.getId(), controlLayerView);
          }
          controlLayerView.setVisibility(GONE);
        }
      }
    }
    // switch default ControlLayer
    if (switchControlLayer(currentControlLayerId) == 0) {
      //failure -> switch first ControlLayer
      int key = controlLayers.keyAt(0);
      if (controlLayers.indexOfKey(key) >= 0) {
        switchControlLayer(key);
      }
    }
  }

  // ============================@SmartView
  protected TextView positionTv;
  protected TextView durationTv;
  protected ProgressBar progressBar;
  protected ProgressAny progressAny;
  protected View playView;
  protected View pauseView;
  protected View previousView;
  protected View nextView;
  protected View rewindView;
  protected View fastForwardView;
  protected View switchControlLayerView;

  protected void initSmartViewByControlLayer(AControlLayerView view) {
    if (view == null) {
      return;
    }
    // =========@Progress@=========
    positionTv = (TextView) view.findSmartView(R.id.k_position_tv);
    durationTv = (TextView) view.findSmartView(R.id.k_duration_tv);
    progressBar = (ProgressBar) view.findSmartView(R.id.k_progress_bar);
    progressAny = (ProgressAny) view.findSmartView(R.id.k_progress_any);
    // =========@Set
    if (progressBar != null) {
      progressBar.setMax(PROGRESS_BAR_MAX);
      if (progressBar instanceof SeekBar) {
        ((SeekBar) progressBar).setOnSeekBarChangeListener(componentListener);
      }
    }
    if (progressAny != null) {
      progressAny.setMax(PROGRESS_BAR_MAX);
      progressAny.setOnChangeListener(componentListener);
    }
    // =========@Button@=========
    playView = view.findSmartView(R.id.k_play);
    pauseView = view.findSmartView(R.id.k_pause);
    previousView = view.findSmartView(R.id.k_prev);
    nextView = view.findSmartView(R.id.k_next);
    rewindView = view.findSmartView(R.id.k_rew);
    fastForwardView = view.findSmartView(R.id.k_ffwd);
    switchControlLayerView = view.findSmartView(R.id.k_switch_control_layer);
    // =========@Set
    if (playView != null) {
      playView.setOnClickListener(componentListener);
    }
    if (pauseView != null) {
      pauseView.setOnClickListener(componentListener);
    }
    if (previousView != null) {
      previousView.setOnClickListener(componentListener);
    }
    if (nextView != null) {
      nextView.setOnClickListener(componentListener);
    }
    if (switchControlLayerView != null) {
      switchControlLayerView.setOnClickListener(componentListener);
    }
    if (rewindView != null) {
      rewindView.setOnClickListener(componentListener);
    }
    if (fastForwardView != null) {
      fastForwardView.setOnClickListener(componentListener);
    }
  }

  public void clickSmartViewById(int id) {
    hideAfterTimeout();
    if (R.id.k_pause == id) {
      play(0);
    } else if (R.id.k_play == id) {
      play(1);
    } else if (R.id.k_next == id) {
      next();
    } else if (R.id.k_prev == id) {
      previous();
    } else if (R.id.k_ffwd == id) {
      fastForward();
    } else if (R.id.k_rew == id) {
      rewind();
    } else if (R.id.k_switch_control_layer == id) {
      try {
        if (currentControlLayerId == R.id.k_ctrl_layer_port) {
          getPlayerView().getOrientationHelper().goLandscape();
        } else {
          getPlayerView().getOrientationHelper().goPortrait();
        }
      } catch (Exception e) {
        //NullPointer...
        L.printStackTrace(e);
      }
    }
  }

  // ============================@ControlLayer Manage@============================
  protected int currentControlLayerId;
  protected final SparseArray<AControlLayerView> controlLayers = new SparseArray<>();

  /**
   * @param controlLayerId R.id.k_control_layer_...
   * @return switchState <ul> <li>0:failure <li>1:succeed <li>2:switched <ul/>
   */
  public int switchControlLayer(int controlLayerId) {
    AControlLayerView controlLayerView = getControlLayerView(controlLayerId);
    if (controlLayerView == null) {
      onSwitchControlLayer(controlLayerId, 0);
      return 0;//failure
    }
    if (currentControlLayerId == controlLayerId && controlLayerView.getVisibility() == VISIBLE) {
      onSwitchControlLayer(controlLayerId, 2);
      return 2;//switched
    }
    int returnParam = 0;//failure
    for (int i = 0; i < controlLayers.size(); i++) {
      int key = controlLayers.keyAt(i);
      controlLayerView = getControlLayerView(key);
      if (controlLayerView != null) {
        if (key == controlLayerId) {
          controlLayerView.setVisibility(VISIBLE);
          controlLayerView.onResume();
          initSmartViewByControlLayer(controlLayerView);
          currentControlLayerId = controlLayerId;
          updateAll();
          returnParam = 1;//succeed
        } else {
          controlLayerView.setVisibility(GONE);
          controlLayerView.onPause();
        }
      }
    }
    onSwitchControlLayer(controlLayerId, returnParam);
    return returnParam;
  }


  public AControlLayerView getControlLayerView(int controlLayerId) {
    AControlLayerView controlLayerView = controlLayers.get(controlLayerId);
    if (controlLayerView == null) {
      controlLayerView = (AControlLayerView) findViewById(controlLayerId);
      if (controlLayerView != null) {
        controlLayers.put(controlLayerId, controlLayerView);
      }
    }
    return controlLayerView;
  }

  public int getCurrentControlLayerId() {
    return currentControlLayerId;
  }

  public AControlLayerView getCurrentControlLayerView() {
    return getControlLayerView(currentControlLayerId);
  }

  // ============================@View@============================

  /**
   * Returns whether the interaction Area is currently visible. [call
   * currentControlLayerView.isVisibleByInteractionArea()]
   *
   * @return currentControlLayerView==null return getVisibility() == VISIBLE
   */
  public boolean isVisibleByInteractionArea() {
    AControlLayerView currentControlLayerView = getCurrentControlLayerView();
    if (currentControlLayerView != null) {
      return currentControlLayerView.isVisibleByInteractionArea();
    }
    return getVisibility() == VISIBLE;
  }

  /**
   * Returns whether the controller is currently visible. [call currentControlLayerView.isVisibleByPlayController()]
   *
   * @return currentControlLayerView==null return getVisibility() == VISIBLE
   */
  public boolean isVisibleByPlayController() {
    AControlLayerView currentControlLayerView = getCurrentControlLayerView();
    if (currentControlLayerView != null) {
      return currentControlLayerView.isVisibleByPlayController();
    }
    return getVisibility() == VISIBLE;
  }

  /**
   * super.setVisibility(visibility); before will handle hideTimeout
   */
  @Override
  public void setVisibility(int visibility) {
    if (visibility != VISIBLE) {
      removeCallbacks(hideAction);
    } else if (isVisibleByInteractionArea()) {
      hideAfterTimeout();//VISIBLE
    }
    super.setVisibility(visibility);
  }

  /**
   * call currentControlLayerView.setVisibility(visibility, animation)
   *
   * @param animation whether with animation
   */
  public void setVisibilityByInteractionArea(int visibility, boolean animation) {
    AControlLayerView currentControlLayerView = getCurrentControlLayerView();
    if (currentControlLayerView != null) {
      currentControlLayerView.setVisibilityByInteractionArea(visibility, animation);
    } else {
      super.setVisibility(visibility);
    }
  }

  // ============================@Lock
  protected boolean isLocked = false;

  /**
   * control layer by isLocked handle view visibility<ul> <li>false: handle all interaction view
   * show/hide <li>true: handle lock interaction view show/hide <ul/>
   */
  public boolean isLocked() {
    return isLocked;
  }

  /**
   * Set control group locked <p /> locked: <ul> <li>control layer locked <li>disable orientation
   * helper <li>intercept dispatch KeyEvent <li>disable gesture detector <ul/>
   *
   * @return isLocked
   */
  public boolean setLocked(boolean locked) {
    if (locked != isLocked) {
      // =========@InteractionArea
      // control layer by isLocked handle show/hide
      if (locked) {
        hide();
      }//locked before hide(isLocked==false)
      isLocked = locked;
      if (!locked) {
        show();
      }//unlock after show(isLocked==false)
      // =========@Other
      APlayerView playerView = getPlayerView();
      if (playerView != null) {
        if (locked) {//disable
          playerView.setOrientationHelper(null, 2);
        } else {//enable
          playerView.setOrientationHelper(null, 1);
        }
      }//handle orientation helper
    }
    return isLocked;
  }

  // ============================@Show X View
  public void showBufferingView(boolean show) {
    AControlLayerView currentControlLayerView = getCurrentControlLayerView();
    if (currentControlLayerView != null) {
      currentControlLayerView.showBufferingView(show);
    }
  }

  public void showTipsView(boolean show, CharSequence text, @DrawableRes int icon) {
    AControlLayerView currentControlLayerView = getCurrentControlLayerView();
    if (currentControlLayerView != null) {
      currentControlLayerView.showTipsView(show, text, icon);
    }
  }

  // ============================@Control Layer Interaction Area Show/Hide
  public static final int DEFAULT_SHOW_TIMEOUT_MS = 5000;
  // ===========================@@Show
  protected int showTimeoutMs;

  /**
   * Returns the controlGroupView timeout. The controlGroupView are automatically hidden after this
   * duration of time has elapsed without user input.
   *
   * @return The duration in milliseconds. A non-positive value indicates that the controlGroupView
   * will remain visible indefinitely.
   */
  public int getShowTimeoutMs() {
    return showTimeoutMs;
  }

  /**
   * Sets the controlGroupView timeout. The controlGroupView are automatically hidden after this
   * duration of time has elapsed without user input.
   *
   * @param showTimeoutMs The duration in milliseconds. A non-positive value will cause the
   * controlGroupView to remain visible indefinitely.
   */
  public void setShowTimeoutMs(int showTimeoutMs) {
    this.showTimeoutMs = showTimeoutMs;
  }

  /**
   * Shows the Interaction Area. [default with animation]
   *
   * @return Whether need to deal with [!isVisibleByInteractionArea()]
   */
  public boolean show() {
    return show(true);
  }

  /**
   * Shows the Interaction Area.
   *
   * @param animation whether with animation
   * @return Whether need to deal with [!isVisibleByInteractionArea()]
   */
  public boolean show(boolean animation) {
    return show(animation, true);
  }

  public boolean show(boolean animation, boolean hideAfterTimeout) {
    if (!isVisibleByInteractionArea()) {
      setVisibilityByInteractionArea(VISIBLE, animation);
      if (hideAfterTimeout) {
        hideAfterTimeout();
      } else {
        removeCallbacks(hideAction);
      }
      return true;
    }
    if (hideAfterTimeout) {
      hideAfterTimeout();// Call hideAfterTimeout even if already visible to reset the timeout.
    } else {
      removeCallbacks(hideAction);
    }
    return false;
  }

  // ===========================@@Hide
  protected long hideAtMs;
  public final Runnable hideAction = new Runnable() {
    @Override
    public void run() {
      hide();
    }
  };

  /**
   * Hides the Interaction Area. [default with animation]
   *
   * @return Whether need to deal with [isVisibleByInteractionArea()]
   */
  public boolean hide() {
    return hide(true);
  }

  /**
   * Hides the Interaction Area.
   *
   * @param animation whether with animation
   * @return Whether need to deal with [isVisibleByInteractionArea()]
   */
  public boolean hide(boolean animation) {
    if (isVisibleByInteractionArea()) {
      setVisibilityByInteractionArea(GONE, animation);
      removeCallbacks(hideAction);
      hideAtMs = C.TIME_UNSET;
      return true;
    }
    return false;
  }

  public void hideAfterTimeout(int showTimeoutMs) {
    removeCallbacks(hideAction);
    if (showTimeoutMs > 0) {
      hideAtMs = SystemClock.uptimeMillis() + showTimeoutMs;
      if (isAttachedToWindow) {
        postDelayed(hideAction, showTimeoutMs);
      }
    } else {
      hideAtMs = C.TIME_UNSET;
    }
  }

  public void hideAfterTimeout() {
    hideAfterTimeout(showTimeoutMs);
  }

  // ============================@Update@============================
  protected void updateAll() {
    updateProgressView(-1, -1);
    updateBufferingView();
  }

  // ============================@Progress
  protected static int PROGRESS_BAR_MAX = 100;
  protected boolean dragging;

  protected void updateProgressView(long position, long duration) {
    updateProgressView(position, null, duration, null);
  }

  /**
   * @param positionCP <ul> <li>{@link C.PARAM#UNSET} <li>{@link C.PARAM#FORCE} <ul/>
   * @param durationCP <ul> <li>{@link C.PARAM#UNSET} <ul/>
   */
  protected void updateProgressView(long position, C.PARAM positionCP,
      long duration, C.PARAM durationCP) {
    if (!isVisibleByPlayController() || !isAttachedToWindow) {
      return;
    }
    if (durationCP != C.PARAM.UNSET && durationTv != null) {
      if (duration < 0) {
        if (player != null) {
          duration = (duration = player.getDuration()) < 0 ? 0 : duration;
        } else {
          duration = 0;
        }
      }
      durationTv.setText(TimeProgress.stringForTime(duration));
    }

    if (positionCP != C.PARAM.UNSET && (!dragging || positionCP == C.PARAM.FORCE)) {
      if (position < 0) {
        if (player != null) {
          position = player.getCurrentPosition();
        } else {
          position = 0;
        }
      }
      if (positionTv != null) {
        positionTv.setText(TimeProgress.stringForTime(position));
      }
      int p = progressValue(position);
      if (progressBar != null) {
        progressBar.setProgress(p);
      }
      if (progressAny != null) {
        progressAny.setProgress(p);
      }
    }
  }

  protected int progressValue(long position) {
    long duration = player == null ? 0 : player.getDuration();
    return TimeProgress.progressValue(position, duration, PROGRESS_BAR_MAX);
  }

  protected long positionValue(int progress) {
    long duration = player == null ? 0 : player.getDuration();
    return TimeProgress.positionValue(progress, duration, PROGRESS_BAR_MAX);
  }

  // ============================@Buffer
  protected void updateBufferingView() {
    if (player != null) {
      showBufferingView(player.getPlaybackState() == IPlayer.STATE_BUFFERING);
    }
  }


  // ============================@Media Correlation@============================
  public boolean isPlayable() {
    return player != null && player.isPlayable();
  }

  /**
   * @param order 1:play 0:pause else:Auto
   */
  public void play(int order) {
    if (!isPlayable()) {
      return;
    }
    if (order == 1) {
      player.start();
    } else if (order == 0) {
      player.pause();
    } else if (!player.isPlaying()) {
      player.start();
    } else {
      player.pause();
    }
  }

  // ============================@Seek
  protected SeekDispatcher seekDispatcher;

  public void setSeekDispatcher(SeekDispatcher seekDispatcher) {
    this.seekDispatcher = seekDispatcher;
  }

  /**
   * Dispatches seek operations to the player.
   */
  public interface SeekDispatcher {

    /**
     * @param player The player to seek.
     * @param ms The seek position
     * @return True if the seek was dispatched. False otherwise.
     */
    boolean dispatchSeek(IPlayer player, long ms);
  }

  public void seekTo(long ms) {
    if (isPlayable() && (seekDispatcher == null || !seekDispatcher.dispatchSeek(player, ms))) {
      player.seekTo(ms);
    }
  }

  public void rewind() {
    if (rewindMs > 0 && isPlayable()) {
      player.fastForwardRewind(-rewindMs);
    }
  }

  public void fastForward() {
    if (fastForwardMs > 0 && isPlayable()) {
      player.fastForwardRewind(fastForwardMs);
    }
  }


  public void previous() {
    if (player != null) {
      player.getMediaQueue().skipToPrevious();
    }
  }

  public void next() {
    if (player != null) {
      player.getMediaQueue().skipToNext();
    }
  }

  public static final int DEFAULT_REWIND_MS = 5000;
  public static final int DEFAULT_FAST_FORWARD_MS = 15000;

  protected int rewindMs;
  protected int fastForwardMs;

  /**
   * Sets the rewind increment in milliseconds.
   *
   * @param rewindMs The rewind increment in milliseconds. A non-positive value will cause the
   * rewind button to be disabled.
   */
  public void setRewindIncrementMs(int rewindMs) {
    this.rewindMs = rewindMs;
  }

  /**
   * Sets the fast forward increment in milliseconds.
   *
   * @param fastForwardMs The fast forward increment in milliseconds. A non-positive value will
   * cause the fast forward button to be disabled.
   */
  public void setFastForwardIncrementMs(int fastForwardMs) {
    this.fastForwardMs = fastForwardMs;
  }

  // ============================@ComponentListener@============================
  protected ComponentListener componentListener;

  protected abstract ComponentListener initGetComponentListener();

  protected class ComponentListener extends PlayerListener implements OnClickListener,
      OnSeekBarChangeListener, ProgressAny.OnChangeListener {

    @Override
    public void onClick(View v) {
      clickSmartViewById(v.getId());
    }

    // ============================@SeekBar.OnSeekBarChangeListener
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
      removeCallbacks(hideAction);
      dragging = true;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      if (fromUser) {
        long position = positionValue(progress);
        if (positionTv != null) {
          positionTv.setText(TimeProgress.stringForTime(position));
        }
        if (player != null && !dragging) {
          seekTo(position);
        }
      }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
      dragging = false;
      if (player != null) {
        seekTo(positionValue(seekBar.getProgress()));
      }
      hideAfterTimeout();
    }

    // ============================@ProgressAny.Listener
    @Override
    public void onStartTrackingTouch(ProgressAny progressAny) {
      removeCallbacks(hideAction);
      dragging = true;
    }

    @Override
    public void onProgressChanged(ProgressAny progressAny, int progress, boolean fromUser) {
      if (fromUser) {
        long position = positionValue(progress);
        if (positionTv != null) {
          positionTv.setText(TimeProgress.stringForTime(position));
        }
        if (player != null && !dragging) {
          seekTo(position);
        }
      }
    }

    @Override
    public void onStopTrackingTouch(ProgressAny progressAny) {
      dragging = false;
      if (player != null) {
        seekTo(positionValue(progressAny.getProgress()));
      }
      hideAfterTimeout();
    }
  }

  // ============================@Listener@============================
  Listener listener;

  public void setListener(Listener listener) {
    this.listener = listener;
  }

  public interface Listener {

    /**
     * @param switchState <ul> <li>0:failure <li>1:succeed <li>2:switched <ul/>
     */
    void onSwitchControlLayer(int controlLayerId, int switchState);
  }

  protected void onSwitchControlLayer(int controlLayerId, int switchState) {
    if (listener != null) {
      listener.onSwitchControlLayer(controlLayerId, switchState);
    }
    if (switchState != 0) {
      show();
    }
  }

  // ============================@Assist@============================
  public APlayerView getPlayerView() {
    ViewParent parent = getParent();
    if (parent instanceof APlayerView) {
      return (APlayerView) parent;
    }
    return null;
  }
}