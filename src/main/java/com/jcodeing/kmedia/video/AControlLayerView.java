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
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.util.AttributeSet;
import android.view.View;
import com.jcodeing.kmedia.R;
import com.jcodeing.kmedia.view.LocalFrameLayout;

public abstract class AControlLayerView extends LocalFrameLayout {

  public AControlLayerView(Context context) {
    this(context, null);
  }

  public AControlLayerView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public AControlLayerView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  /**
   * Initialize (invoke from constructor) <p /> internal invoke: <ul> <li>{@link
   * #initAttrs(TypedArray)} <li>{@link #initView()} <li>{@link #initConfig()} <ul/>
   */
  @Override
  protected void initialize(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    if (attrs != null) {
      TypedArray a = getContext().obtainStyledAttributes(
          attrs, R.styleable.AControlLayerView, defStyleAttr, 0);
      try {
        initAttrs(a);
      } finally {
        a.recycle();
      }
    } else {
      initAttrs(null);
    }
    super.initialize(attrs, defStyleAttr, defStyleRes);
  }

  /**
   * Initialize attrs <p /> invoke from {@link #initialize(AttributeSet, int, int)} first(custom)
   * before {@link #initView()}
   *
   * @return a != null
   */
  protected boolean initAttrs(TypedArray a) {
    return a != null;
  }

  // ============================@Lifecycle@============================
  protected void onResume() {
    //Do something
  }

  protected void onPause() {
    //Do something
  }

  // ============================@View@============================

  /**
   * @return whether the interaction Area is currently visible.
   */
  public abstract boolean isVisibleByInteractionArea();

  /**
   * @return whether the play controller is currently visible.
   */
  public abstract boolean isVisibleByPlayController();

  /**
   * Set the visibility state of this view. [With Animation]
   *
   * @param visibility One of {@link #VISIBLE}, {@link #INVISIBLE}, or {@link #GONE}.
   * @param animation whether with animation
   */
  public abstract void setVisibilityByInteractionArea(int visibility, boolean animation);

  // ============================@Show X View

  /**
   * subclass handled: if (!super.showBufferingView(show)){ handled... } return true;
   *
   * @param show show/hideBufferingView
   * @return Return <code>true</code> to prevent this event from being propagated further, or
   * <code>false</code> to indicate that you have not handled this event and it should continue to
   * be propagated.
   */
  public boolean showBufferingView(boolean show) {
    return showXViewListener != null && showXViewListener.onShowBufferingView(show);
  }

  public boolean showTipsView(boolean show, CharSequence text, @DrawableRes int icon) {
    return showXViewListener != null && showXViewListener.onShowTipsView(show, text, icon);
  }

  // ============================@X View Listener
  public void setShowXViewListener(ShowXViewListener showXViewListener) {
    this.showXViewListener = showXViewListener;
  }

  protected ShowXViewListener showXViewListener;

  public interface ShowXViewListener {

    boolean onShowBufferingView(boolean show);

    boolean onShowTipsView(boolean show, CharSequence text, @DrawableRes int icon);
  }

  public static class SimpleShowXViewListener implements ShowXViewListener {

    @Override
    public boolean onShowBufferingView(boolean show) {
      return false;
    }

    @Override
    public boolean onShowTipsView(boolean show, CharSequence text, @DrawableRes int icon) {
      return false;
    }
  }

  // ============================@FindSmartView

  /**
   * ControlGroupView call this , get View(R.id.k_play ...)
   */
  public View findSmartView(@IdRes int id) {
    if (findSmartViewListener != null) {
      return findSmartViewListener.onFindSmartView(id);
    }
    return findViewById(id);
  }

  protected FindSmartViewListener findSmartViewListener;

  public void updateSmartView() {
    if (getControlGroup() != null && equals(controlGroup.getCurrentControlLayerView())) {
      controlGroup.initSmartViewByControlLayer(this);
    }
  }

  /**
   * Is not limited to the current ControlLayerView get View(R.id.k_play ...)
   */
  public void setFindSmartViewListener(FindSmartViewListener findSmartViewListener) {
    this.findSmartViewListener = findSmartViewListener;
    //If is current control layer view then immediately to init update up to date
    updateSmartView();
  }

  public interface FindSmartViewListener {

    View onFindSmartView(@IdRes int id);
  }

  // ============================@Assist@============================
  protected AControlGroupView controlGroup;

  protected AControlGroupView getControlGroup() {
    if (controlGroup == null && getParent() instanceof AControlGroupView) {
      controlGroup = (AControlGroupView) getParent();
    }
    return controlGroup;
  }
}