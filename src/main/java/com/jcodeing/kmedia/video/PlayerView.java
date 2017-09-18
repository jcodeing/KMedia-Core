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
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import com.jcodeing.kmedia.IPlayer;
import com.jcodeing.kmedia.PlayerListener;
import com.jcodeing.kmedia.R;
import com.jcodeing.kmedia.video.AspectRatioView.ResizeMode;

/**
 * Player View. below simple use for layout.
 * <pre>
 * Attrs
 * ==========================================
 * app:surface_type="surface_view"|"texture_view"|...
 * app:resize_mode="fill"|"fit"|"fixed_width/height"
 * app:use_control_group="true"
 * app:use_gesture_detector="false"
 * app:player_layout_id="..."
 * app:...
 *
 * Simple template
 * ==========================================
 * Default
 * ------------------------------------------
 * &#60com.jcodeing.kmedia.video.PlayerView
 *   android:id="@id/k_player_view"
 *   android:layout_width="match_parent"
 *   android:layout_height="200dp"&#47>
 *
 * Custom ControlLayer
 * ------------------------------------------
 * &#60com.jcodeing.kmedia.video.PlayerView
 *   android:id="@id/k_player_view"
 *   android:layout_width="match_parent"
 *   android:layout_height="200dp">
 *   &#60com.jcodeing.kmedia.video.ControlLayerView
 *     android:id="@id/k_ctrl_layer_port"
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent"&#47>
 *  &#60com.jcodeing.kmedia.video.PlayerView&#47>
 *
 * Custom Control Group
 * ------------------------------------------
 * &#60com.jcodeing.kmedia.video.PlayerView
 *   android:id="@id/k_player_view"
 *   android:layout_width="match_parent"
 *   android:layout_height="200dp">
 *   &#60com.jcodeing.kmedia.video.ControlGroupView
 *     android:id="@id/k_ctrl_group"
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent">
 *     &#60com.jcodeing.kmedia.video.ControlLayerView
 *       android:id="@id/k_ctrl_layer_port"
 *       android:layout_width="match_parent"
 *       android:layout_height="match_parent"&#47>
 *     &#60com.jcodeing.kmedia.video.ControlLayerView
 *       android:id="@id/k_ctrl_layer_land"
 *       android:layout_width="match_parent"
 *       android:layout_height="match_parent"&#47>
 *   &#60com.jcodeing.kmedia.video.ControlGroupView&#47>
 * &#60com.jcodeing.kmedia.video.PlayerView&#47>
 *
 * Readme
 * ==========================================
 * View frame explain:
 * |-------------------------------------|
 * |                                     |
 * |                                     |
 * |      Surface/Texture                |
 * |        AspectRatioView              |
 * |          ControlGroupView           |
 * |            ControlLayerView         |
 * |              Custom(Shutter..)      |
 * |                                     |
 * |                                     |
 * |-------------------------------------|
 * </pre>
 *
 * @see R.layout#k_player_view
 * @see ControlGroupView
 * @see ControlLayerView
 * @see AspectRatioView
 */
public final class PlayerView extends APlayerView<PlayerView> {

  public PlayerView(Context context) {
    this(context, null);
  }

  public PlayerView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public PlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    int playerLayoutId = R.layout.k_player_view;
    int surfaceType = SURFACE_TYPE_SURFACE_VIEW;
    int resizeMode = AspectRatioView.RESIZE_MODE_FILL;
    if (attrs != null) {
      TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
          R.styleable.APlayerView, 0, 0);
      try {
        playerLayoutId = a.getResourceId(R.styleable.APlayerView_player_layout_id,
            playerLayoutId);
        surfaceType = a.getInt(R.styleable.APlayerView_surface_type, surfaceType);
        resizeMode = a.getInt(R.styleable.APlayerView_resize_mode, resizeMode);

      } finally {
        a.recycle();
      }
    }

    LayoutInflater.from(context).inflate(playerLayoutId, this);

    componentListener = new ComponentListener();
    setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);

    // =========@AspectRatioView@=========
    aspectRatioView = (AspectRatioView) findViewById(R.id.k_content_frame);
    //noinspection WrongConstant
    setResizeMode(resizeMode);

    // Create a surface view and insert it into the aspect ratio view, if there is one.
    if (aspectRatioView != null && surfaceType != SURFACE_TYPE_NONE) {
      ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
          ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
      if (surfaceType == SURFACE_TYPE_TEXTURE_VIEW
          && VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH) {
        surfaceView = new TextureView(context);
      } else {
        surfaceView = new SurfaceView(context);
      }
      surfaceView.setLayoutParams(params);
      aspectRatioView.addView(surfaceView, 0);
    } else {
      surfaceView = null;
    }
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    shutterView = findViewById(R.id.k_shutter);
  }

  // ============================@AspectRatioView@============================
  private final AspectRatioView aspectRatioView;

  /**
   * Sets the resize mode.
   *
   * @param resizeMode The resize mode.
   */
  public void setResizeMode(@ResizeMode int resizeMode) {
    if (aspectRatioView != null) {
      aspectRatioView.setResizeMode(resizeMode);
    }
  }

  // ============================@Surface@============================
  public static final int SURFACE_TYPE_NONE = 0;
  public static final int SURFACE_TYPE_SURFACE_VIEW = 1;
  public static final int SURFACE_TYPE_TEXTURE_VIEW = 2;
  private View surfaceView;

  /**
   * Gets the view onto which video is rendered. This is either a {@link SurfaceView} (default) or a
   * {@link TextureView} if the {@code use_texture_view} view attribute has been set to true.
   *
   * @return Either a {@link SurfaceView} or a {@link TextureView}.
   */
  public View getSurfaceView() {
    return surfaceView;
  }

  /**
   * Set a new surface view
   */
  public void setSurfaceView(View surfaceView) {
    if (aspectRatioView != null) {
      if (this.surfaceView != null) {
        aspectRatioView.removeView(this.surfaceView);
      }
      if (surfaceView != null) {
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        surfaceView.setLayoutParams(params);
        aspectRatioView.addView(surfaceView, 0);
      }
    }
    this.surfaceView = surfaceView;
  }

  // ============================@Shutter@============================
  private View shutterView;

  public View getShutterView() {
    return shutterView;
  }

  // ============================@Player@============================
  @Override
  public PlayerView setPlayer(IPlayer player) {
    if (this.player == player) {
      return this;
    }
    if (this.player != null) {
      this.player.removeListener(componentListener);
      this.player.clearVideo();
    }
    this.player = player;

    if (useControlGroup) {
      controlGroupView.setPlayer(player);
    }

    if (player != null) {
      if (surfaceView instanceof TextureView) {
        player.setVideo((TextureView) surfaceView);
      } else if (surfaceView instanceof SurfaceView) {
        player.setVideo((SurfaceView) surfaceView);
      }
      player.addListener(componentListener);
    }
    return this;
  }

  // ============================@ComponentListener@============================
  private final ComponentListener componentListener;

  private final class ComponentListener extends PlayerListener {

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees,
        float pixelWidthHeightRatio) {
      super.onVideoSizeChanged(width, height, unappliedRotationDegrees, pixelWidthHeightRatio);
      if (aspectRatioView != null) {
        float aspectRatio = height == 0 ? 1 : (width * pixelWidthHeightRatio) / height;
        aspectRatioView.setSize(width, height, aspectRatio);
      }
    }

    @Override
    public void onPrepared() {
      super.onPrepared();
      if (shutterView != null) {
        shutterView.setVisibility(INVISIBLE);
      }
    }

    @Override
    public void onStateChanged(int playbackState) {
      switch (playbackState) {
        case IPlayer.STATE_GOT_SOURCE:
          if (shutterView != null) {
            shutterView.setVisibility(VISIBLE);
          }
          break;
      }
    }
  }
}