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
import android.view.View;
import android.view.View.OnClickListener;
import androidx.annotation.NonNull;
import com.jcodeing.kmedia.IPlayer;
import com.jcodeing.kmedia.R;

public class VideoFloatingWindowController extends FloatingWindowController implements
    OnClickListener {

  public VideoFloatingWindowController(@NonNull Context context) {
    super(context);
  }

  private IPlayer player;

  public IPlayer getPlayer() {
    return player;
  }

  /**
   * Show a video floating window view
   *
   * @param player {@link IPlayer} for internal video floating window view
   */
  public boolean show(IPlayer player) {
    this.player = player;
    return show();
  }

  @Override
  public boolean show() {
    if (super.show() && floatingView != null) {
      floatingView.setPlayer(player);
      return true;
    }
    return false;
  }

  /**
   * Hide a video floating window view
   */
  @Override
  public boolean hide() {
    super.hide();
    floatingView.setPlayer(null);
    if (listener != null) {
      listener.onHide(this);
    }//handle onHide()
    return true;
  }

  // ============================@Floating View@============================
  private VideoFloatingWindowView floatingView;

  @Override
  public FloatingWindowView getFloatingWindowView() {
    if (floatingView == null) {
      setFloatingView(new VideoFloatingWindowView(context));
    }
    return floatingView;
  }

  /**
   * Set custom floating window view
   */
  public void setFloatingView(@NonNull VideoFloatingWindowView floatingView) {
    this.floatingView = floatingView;
    View close = floatingView.findViewById(R.id.k_floating_view_close);
    if (close != null) {
      close.setOnClickListener(this);
    }
  }

  // ============================@Other@============================
  @Override
  public void onClick(View v) {
    if (v.getId() == R.id.k_floating_view_close) {
      hide();
    }
  }

  @Override
  protected void onHide() {
    //Do nothing .. override super .. sub class handle
  }
}