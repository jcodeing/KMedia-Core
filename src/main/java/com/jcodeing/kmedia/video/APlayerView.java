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

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import com.jcodeing.kmedia.IPlayer;
import com.jcodeing.kmedia.R;
import com.jcodeing.kmedia.assist.AutoPlayPauseHelper;
import com.jcodeing.kmedia.assist.AutoPlayPauseHelper.PlayerAskFor;
import com.jcodeing.kmedia.assist.OrientationHelper;
import com.jcodeing.kmedia.utils.Metrics;

public abstract class APlayerView<PV extends APlayerView> extends FrameLayout {

  public APlayerView(Context context) {
    this(context, null);
  }

  public APlayerView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public APlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    if (attrs != null) {
      TypedArray a = context.obtainStyledAttributes(
          attrs, R.styleable.APlayerView, defStyleAttr, 0);
      try {
        initAttrs(a);
      } finally {
        a.recycle();
      }
    }
  }

  protected void initAttrs(TypedArray a) {
    heightOrigin = a.getLayoutDimension(R.styleable.APlayerView_android_layout_height, 0);
    useControlGroup = a.getBoolean(R.styleable.APlayerView_use_control_group, true);
    useGestureDetector = a.getBoolean(R.styleable.APlayerView_use_gesture_detector, false);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    // =========@ControlGroup@=========
    if (useControlGroup && controlGroupView == null) {
      View v = findViewById(R.id.k_ctrl_group);
      if (v != null) {
        //use from xml
        controlGroupView = (AControlGroupView) v;
      } else {
        //use from default
        controlGroupView = new ControlGroupView(getContext());
        controlGroupView.setId(R.id.k_ctrl_group);
        addView(controlGroupView);
        // =========@ControlLayer
        // =====@Port
        View portControlLayer = findViewById(R.id.k_ctrl_layer_port);
        if (portControlLayer != null) {
          //use from xml
          removeView(portControlLayer);
          controlGroupView.addView(portControlLayer);
        } else {
          //use from default
          ControlLayerView controlLayerView = new ControlLayerView(getContext());
          controlLayerView.setId(R.id.k_ctrl_layer_port);
          controlGroupView.addView(controlLayerView);
          //Manual call finish inflate(new View(.) The system will not auto callback)
          controlLayerView.onFinishInflate();
        }
        // =====@Land
        View landControlLayer = findViewById(R.id.k_ctrl_layer_land);
        if (landControlLayer != null) {
          //use from xml
          removeView(landControlLayer);
          controlGroupView.addView(landControlLayer);
        }
        controlGroupView.onFinishInflate();
      }
      if (useGestureDetector) {
        controlGroupView.setUseGestureDetector(true, null);
      }
    }
  }

  // ============================@Player@============================
  protected IPlayer player;

  /**
   * @return the player currently set on this view, or null if no player is set.
   */
  public IPlayer player() {
    return player;
  }

  /**
   * @param player Set the {@link IPlayer} to use.
   */
  public abstract PV setPlayer(IPlayer player);


  // ============================@ControlGroup@============================
  protected AControlGroupView controlGroupView;
  protected boolean useControlGroup;//initAttrs
  protected boolean useGestureDetector;//initAttrs

  /**
   * @return the controlGroup currently set on this view, or null if no controlGroup is set.
   */
  public AControlGroupView getControlGroup() {
    return controlGroupView;
  }

  public void setControlGroup(AControlGroupView controlGroupView) {
    setControlGroup(controlGroupView, true);
  }

  public void setControlGroup(AControlGroupView controlGroupView, boolean useControlGroup) {
    if (controlGroupView == null || this.controlGroupView == controlGroupView) {
      return;
    }
    setUseControlGroup(false);
    // =========@Set The Control Group@=========Start
    if (this.controlGroupView != null) {
      // =========@Replace
      controlGroupView.setLayoutParams(this.controlGroupView.getLayoutParams());
      ViewGroup parent = ((ViewGroup) this.controlGroupView.getParent());
      int controllerIndex = parent.indexOfChild(this.controlGroupView);
      parent.removeView(this.controlGroupView);
      parent.addView(controlGroupView, controllerIndex);
    } else {
      // =========@Add
      ViewGroup.LayoutParams layoutParams = controlGroupView.getLayoutParams();
      if (layoutParams == null) {
        layoutParams = new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
      }
      addView(controlGroupView, layoutParams);
    }
    this.controlGroupView = controlGroupView;
    // =========@Set The Control Group@=========End
    setUseControlGroup(useControlGroup);
  }

  /**
   * @return whether the controlGroup are enabled.
   */
  public boolean isUseControlGroup() {
    return useControlGroup;
  }

  /**
   * Sets whether control group are enabled. If set to {@code false} the control group are never
   * visible and are disconnected from the player.
   *
   * @param useControlGroup Whether control group should be enabled.
   */
  public boolean setUseControlGroup(boolean useControlGroup) {
    if (this.useControlGroup == useControlGroup || controlGroupView == null) {
      return false;
    }
    this.useControlGroup = useControlGroup;
    if (useControlGroup) {
      controlGroupView.setPlayer(player);
    } else {
      controlGroupView.setPlayer(null);
    }
    return true;
  }

  // ============================@Orientation Helper@============================
  protected OrientationHelper orientationHelper;
  protected Activity activity;
  private int flagsOrigin;

  /**
   * @return the orientationHelper currently set on this view, or null if no orientationHelper is
   * set.
   */
  public OrientationHelper getOrientationHelper() {
    return orientationHelper;
  }

  /**
   * After setOrientationHelper by getOrientationHelper().go...() change Orientation <p /> Notice:
   * call onConfigurationChanged in your Activity
   * <pre>
   * WARNING:
   *
   * 1. Add configChanges in AndroidManifest.xml
   *  &#60activity
   *    android:configChanges="orientation|keyboardHidden|screenLayout|screenSize"
   *  &#60/activity>
   *
   * 2. Override method onConfigurationChanged in Activity
   *  &#64Override
   *  public void onConfigurationChanged(Configuration newConfig) {
   *    super.onConfigurationChanged(newConfig);
   *    //Dispose View
   *    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
   *      xxx.setVisibility(View.GONE);
   *    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
   *      xxx.setVisibility(View.VISIBLE);
   *    }
   *  }
   * </pre>
   *
   * @param order <ul> <li>1:  enable listen from Sensor orientation change <li>2: disable listen
   * from Sensor orientation change <li>0: release </ul>
   */
  public void setOrientationHelper(Activity activity, int order) {
    // =========@release
    if (order == 0) {
      if (orientationHelper != null) {
        orientationHelper.release();
        orientationHelper = null;
      }
      this.activity = null;
      return;
    }
    // =========@init activity
    if (activity != null) {
      this.activity = activity;
      flagsOrigin = activity.getWindow().getAttributes().flags;
    }
    // =========@init orientationHelper
    if (orientationHelper == null && activity != null) {
      orientationHelper = new OrientationHelper(activity);
    }
    // =========@orientationHelper other order
    if (orientationHelper != null) {
      if (order == 1) {
        orientationHelper.enableFromSensorOrientationChange(true);
      } else if (order == 2) {
        orientationHelper.enableFromSensorOrientationChange(false);
      }
    }
  }

  protected int heightOrigin;//initAttrs

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    if (activity == null || controlGroupView == null) {
      return;
    }

    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {//goLandscape
      //[1] hide all screen decorations (such as the status bar)
      int flagsFullscreen = flagsOrigin | WindowManager.LayoutParams.FLAG_FULLSCREEN;
      if (flagsFullscreen != flagsOrigin) {//Determine whether they have been hidden
        WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
        attrs.flags = flagsFullscreen;
        activity.getWindow().setAttributes(attrs);
      }
      //adjustment player size
      getLayoutParams().width = Metrics.widthPx(activity);
      getLayoutParams().height = Metrics.heightPx(activity);
      //Dispose Player View
      controlGroupView.switchControlLayer(R.id.k_ctrl_layer_land);
    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {//goPortrait
      //clear operation[1]
      int flagsFullscreen = flagsOrigin | WindowManager.LayoutParams.FLAG_FULLSCREEN;
      if (flagsFullscreen != flagsOrigin) {
        final WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
        attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
        activity.getWindow().setAttributes(attrs);
      }
      //adjustment player size
      getLayoutParams().width = Metrics.widthPx(activity);
      getLayoutParams().height = heightOrigin;
      //Dispose Player View
      controlGroupView.switchControlLayer(R.id.k_ctrl_layer_port);
    }
  }

  // ============================@External Call@============================
  /**
   * Notice:  External call area method, need to call in your The Activity
   */

  /**
   * call me in your The Activity Or your Want to finish.
   *
   * @see Activity#finish()
   */
  public void finish() {
    setPlayer(null);
    setOrientationHelper(null, 0);//release
  }

  // ============================@Lifecycle
  private AutoPlayPauseHelper autoPlayPauseHelper;

  public AutoPlayPauseHelper getAutoPlayPauseHelper() {
    if (autoPlayPauseHelper == null) {
      autoPlayPauseHelper = new AutoPlayPauseHelper(new PlayerAskFor() {
        @Override
        public IPlayer player() {
          return player;
        }
      });
    }
    return autoPlayPauseHelper;
  }

  /**
   * call me in your The Activity <ul> <li>auto handle play <ul/>
   *
   * @see Activity#onResume()
   */
  public void onResume() {
    getAutoPlayPauseHelper().onResume();
  }

  /**
   * call me in your The Activity <ul> <li>auto handle pause <ul/>
   *
   * @see Activity#onPause()
   */
  public void onPause() {
    getAutoPlayPauseHelper().onPause();
  }

  /**
   * call me in your The Activity
   *
   * @see Activity#onDestroy()
   */
  public void onDestroy() {
    autoPlayPauseHelper = null;
  }

  // ============================@dispatchKeyEvent

  /**
   * call me in your The Activity <p /> if you want to process media key events. see{@link
   * com.jcodeing.kmedia.assist.MediaButtonReceiverHelper#onMediaButtonEvent(KeyEvent)}
   *
   * @see Activity#dispatchKeyEvent(KeyEvent)
   */
  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    if (controlGroupView != null && controlGroupView.dispatchKeyEvent(event)) {
      return true;//was handled
    }//dispatch control group view
    if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {//handle keycode back
      if (controlGroupView != null
          && controlGroupView.getCurrentControlLayerId() == R.id.k_ctrl_layer_land
          && controlGroupView.getControlLayerView(R.id.k_ctrl_layer_port) != null) {
        if (orientationHelper != null) {
          orientationHelper.goPortrait();
        }
        return true;
      }
    }
    return super.dispatchKeyEvent(event);
  }
}