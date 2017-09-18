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
package com.jcodeing.kmedia.window;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.jcodeing.kmedia.IPlayer;
import com.jcodeing.kmedia.R;
import com.jcodeing.kmedia.utils.Metrics;
import com.jcodeing.kmedia.video.PlayerView;

/**
 * Default video floating window view <p /> Custom floating window view, can extends {@link
 * VideoFloatingWindowView} , override {@link #getDefaultLayoutId()} custom layout. <p /> WARNING:
 * custom layout <ul> <li>R.id.k_player_view <li>R.id.k_floating_view_drag_location (optional)
 * <li>R.id.k_floating_view_drag_size (optional) <ul/>
 *
 * @see R.layout#k_v_floating_win_view
 */
public class VideoFloatingWindowView extends FloatingWindowView {

  public VideoFloatingWindowView(Context context) {
    super(context);
  }

  public VideoFloatingWindowView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public VideoFloatingWindowView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  // ============================@View@============================

  /**
   * Sub class can override, this method. return custom default control layer layout id.
   */
  protected int getDefaultLayoutId() {
    return R.layout.k_v_floating_win_view;
  }

  private PlayerView playerView;
  private View ctrlGroup;
  private View dragLocationView;
  private View dragSizeView;

  @Override
  protected void initView() {
    inflate(getDefaultLayoutId());
    playerView = (PlayerView) findViewById(R.id.k_player_view);
    ctrlGroup = findViewById(R.id.k_ctrl_group);
    dragLocationView = findViewById(R.id.k_floating_view_drag_location);
    dragSizeView = findViewById(R.id.k_floating_view_drag_size);
  }

  @Override
  protected View initConfigGetDragLocationView() {
    return dragLocationView;
  }

  @Override
  protected View initConfigGetDragSizeView() {
    return dragSizeView;
  }

  @Override
  protected OnTouchListener initConfigGetDragSizeTouchListener() {
    return new LocalDragSizeViewTouch(getContext());
  }

  class LocalDragSizeViewTouch extends DragSizeViewTouch {

    boolean isHandleCtrlGroupVisible;

    LocalDragSizeViewTouch(Context context) {
      super(context);
    }

    @Override
    protected void setViewSize(int width, int height, boolean widthIsAscending,
        boolean heightIsAscending) {
      if (!isHandleCtrlGroupVisible &&
          ctrlGroup != null && ctrlGroup.getVisibility() == VISIBLE) {
        isHandleCtrlGroupVisible = true;
        ctrlGroup.setVisibility(GONE);
      }
      super.setViewSize(width, height, widthIsAscending, heightIsAscending);
    }

    @Override
    protected boolean onTouchUp(View v, MotionEvent event) {
      if (isHandleCtrlGroupVisible &&
          ctrlGroup != null && ctrlGroup.getVisibility() != VISIBLE) {
        isHandleCtrlGroupVisible = false;
        ctrlGroup.setVisibility(VISIBLE);
      }
      return super.onTouchUp(v, event);
    }
  }

  /**
   * @param player if == null -> finish
   */
  public void setPlayer(IPlayer player) {
    if (player != null) {
      player.clearVideo();
      playerView.setPlayer(player);
    } else {
      playerView.finish();
    }
  }

  // ============================@Other@============================
  @Override
  protected void onSet(FloatingWindow floatingWindow) {
    super.onSet(floatingWindow);
    floatingWindow
        .setMinWidthHeight(Metrics.dp2px(getContext(), 191f), Metrics.dp2px(getContext(), 127f));
    floatingWindow
        .setMaxWidthHeight(displayWidth, displayHeight);
  }

  @Override
  protected void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    if (floatingWindow != null) {//changed based on orientation
      floatingWindow.setMaxWidthHeight(displayWidth, displayHeight);
    }
  }
}