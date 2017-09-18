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
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import com.jcodeing.kmedia.R;
import com.jcodeing.kmedia.assist.AnimationHelper;

/**
 * Control layer view . below simple use for layout.
 * <pre>
 * Attrs
 * ==========================================
 * app:interaction_area_always_visible="false"
 * app:use_part_top="false"
 * app:use_part_bottom="true"
 * app:use_part_left="false"
 * app:use_part_right="false"
 * app:use_part_middle="true"
 * app:use_part_view_animation="true"
 * app:part_top_min_height="topMinHeight"
 * app:part_bottom_min_height="bottomMinHeight"
 * -------------------------
 * app:use_part_buffer="true"
 * app:use_part_tips="true"
 * -------------------------
 * app:control_layer_layout_id="..."
 * app:...
 *
 * Simple template
 * ==========================================
 * &#60com.jcodeing.kmedia.video.ControlLayerView
 *   android:id="@id/k_ctrl_layer_port"
 *   android:layout_width="match_parent"
 *   android:layout_height="match_parent"
 *   app:use_part_bottom="false"
 *   app:use_part_top="true">
 *    &#60XX-View
 *      android:id="@id/k_ctrl_layer_part_top"
 *      android:layout_width="..."
 *      android:layout_height="..."
 *      android:layout_gravity="..."&#47>
 * &#60/com.jcodeing.kmedia.video.ControlLayerView>
 *
 * Readme
 * ==========================================
 * Can use below id To quickly build interaction area:
 *  R.id.k_ctrl_layer_part_top
 *  R.id.k_ctrl_layer_part_bottom
 *  R.id.k_ctrl_layer_part_left
 *  R.id.k_ctrl_layer_part_right
 *  R.id.k_ctrl_layer_part_middle
 *
 * View frame explain:
 * |------------------------|
 * |          top           |
 * |                        |
 * |                        |
 * | left    middle   right |
 * |                        |
 * |                        |
 * |         bottom         |
 * |------------------------|
 * ~~~~~~~~~~~~~~~~~~~Area~~~~~~~~~~~~~~~~~~~
 * 1.Interaction (top, bottom, left, right, middle)
 *     Handle user interaction.
 *     in the case of user inactivity, automatically hidden.
 *     Internal:
 *     Default layout(R.layout.k_ctrl_layer_view)
 *     use part_top/bottom/left/right/middle_container anchor interaction area
 *     developer -> app:use_part_top="true" Or  custom view use @id/k_ctrl_layer_part_top
 *     ->: internal will default Or custom top view add to part_top_container
 * 2.Other (buffer, tips, ...)
 *     Handle user assist.
 * ~~~~~~~~~~~~~~~~~~~Part~~~~~~~~~~~~~~~~~~~
 * 1.From default control layer layout(R.layout.k_ctrl_layer_view)
 *     part_top_container, ..., part_buffer, part_tips_tv
 * 2.From default top/bottom/middle layout(R.layout.k_ctrl_layer_part_top/bottom/middle)
 * 3.From custom (anyone child view)
 * </pre>
 * Custom control layer view, can extends {@link ControlLayerView} , override {@link
 * #getDefaultLayoutId()} custom layout. <ul> <li>R.id.k_ctrl_layer_part_buffer(optional)
 * <li>R.id.k_ctrl_layer_part_tips_tv(optional) <ul/>
 *
 * @see R.layout#k_ctrl_layer_view
 * @see ControlGroupView
 * @see PlayerView
 */
public class ControlLayerView extends AControlLayerView {

  public ControlLayerView(Context context) {
    this(context, null);
  }

  public ControlLayerView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ControlLayerView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  // =========@Interaction Area@=========
  protected boolean interactionAreaAlwaysVisible;
  // =========@Use
  protected boolean usePartTop;
  protected boolean usePartBottom;
  protected boolean usePartLeft;
  protected boolean usePartRight;
  protected boolean usePartMiddle;
  protected boolean usePartViewAnimation;
  // =========@Attr
  private float partTopMinHeight;
  private float partBottomMinHeight;
  // =========@Other@=========
  protected boolean usePartBuffer;
  protected boolean usePartTips;


  @Override
  protected boolean initAttrs(TypedArray a) {
    // =========@Form Default
    controlLayerLayoutId = getDefaultLayoutId();
    // =====@Interaction Area
    interactionAreaAlwaysVisible = false;
    usePartTop = false;
    usePartBottom = true;
    usePartLeft = false;
    usePartRight = false;
    usePartMiddle = true;
    usePartViewAnimation = true;
    // =====@Other
    usePartBuffer = true;
    usePartTips = true;

    // =========@Form User
    if (super.initAttrs(a)) {
      controlLayerLayoutId = a.getResourceId(
          R.styleable.AControlLayerView_control_layer_layout_id, controlLayerLayoutId);
      // =====@Interaction Area
      interactionAreaAlwaysVisible = a.getBoolean(
          R.styleable.AControlLayerView_interaction_area_always_visible,
          interactionAreaAlwaysVisible);
      usePartTop = a.getBoolean(
          R.styleable.AControlLayerView_use_part_top, usePartTop);
      usePartBottom = a.getBoolean(
          R.styleable.AControlLayerView_use_part_bottom, usePartBottom);
      usePartLeft = a.getBoolean(
          R.styleable.AControlLayerView_use_part_left, usePartLeft);
      usePartRight = a.getBoolean(
          R.styleable.AControlLayerView_use_part_right, usePartRight);
      usePartMiddle = a.getBoolean(
          R.styleable.AControlLayerView_use_part_middle, usePartMiddle);
      usePartViewAnimation = a.getBoolean(
          R.styleable.AControlLayerView_use_part_view_animation, usePartViewAnimation);
      partTopMinHeight = a.getDimension(
          R.styleable.AControlLayerView_part_top_min_height, 0);
      partBottomMinHeight = a.getDimension(
          R.styleable.AControlLayerView_part_bottom_min_height, 0);
      // =====@Other
      usePartBuffer = a.getBoolean(
          R.styleable.AControlLayerView_use_part_buffer, usePartBuffer);
      usePartTips = a.getBoolean(
          R.styleable.AControlLayerView_use_part_tips, usePartTips);
      return true;
    } else {
      return false;
    }
  }

  protected int controlLayerLayoutId;

  /**
   * Sub class can override, this method. return custom default control layer layout id.
   */
  protected int getDefaultLayoutId() {
    return R.layout.k_ctrl_layer_view;
  }

  @Override
  protected void initView() {
    inflate(controlLayerLayoutId);
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
  protected void onFinishInflate() {
    super.onFinishInflate();
    // =========@Top@=========
    if (controlLayerLayoutId == getDefaultLayoutId()) {
      //default control layer layout(handle by attrs)
      if (usePartTop && topView == null) {//↓ R.layout.k_ctrl_layer_view
        topView = findViewById(R.id.part_top_container);
        if (topView != null && topView instanceof ViewGroup) {
          //==Use container
          ViewGroup topContainer = (ViewGroup) this.topView;
          View top = initPartFromFinishInflate(R.id.k_ctrl_layer_part_top);
          if (top == null) {
            LayoutInflater.from(getContext())//use default top
                .inflate(R.layout.k_ctrl_layer_part_top, topContainer);
            initPartFromFinishInflate(R.id.k_ctrl_layer_part_top);
          } else {//adjust
            removeView(top);//custom top add topContainer
            topContainer.addView(top);
          }
        } else if (topView == null) {//sub class override default layout id
          //==Non use container
          topView = initPartFromFinishInflate(R.id.k_ctrl_layer_part_top);
        }
      }
    } else {
      if (topView == null) {
        topView = initPartFromFinishInflate(R.id.k_ctrl_layer_part_top);
      }//custom control layer layout(don't handle default layout attrs)
    }
    // =========@Bottom@=========
    if (controlLayerLayoutId == getDefaultLayoutId()) {
      //default control layer layout(handle by attrs)
      if (usePartBottom && bottomView == null) {//↓ R.layout.k_ctrl_layer_view
        bottomView = findViewById(R.id.part_bottom_container);
        if (bottomView != null && bottomView instanceof ViewGroup) {
          //==Use container
          ViewGroup bottomContainer = (ViewGroup) this.bottomView;
          View bottom = initPartFromFinishInflate(R.id.k_ctrl_layer_part_bottom);
          if (bottom == null) {
            LayoutInflater.from(getContext())//use default bottom
                .inflate(R.layout.k_ctrl_layer_part_bottom, bottomContainer);
            initPartFromFinishInflate(R.id.k_ctrl_layer_part_bottom);
          } else {//adjust
            removeView(bottom);//custom bottom add bottomContainer
            bottomContainer.addView(bottom);
          }
        } else if (bottomView == null) {//sub class override default layout id
          //==Non use container
          bottomView = initPartFromFinishInflate(R.id.k_ctrl_layer_part_bottom);
        }
      }
    } else {
      if (bottomView == null) {
        bottomView = initPartFromFinishInflate(R.id.k_ctrl_layer_part_bottom);
      }//custom control layer layout(don't handle default layout attrs)
    }
    // =========@Left@=========
    if (controlLayerLayoutId == getDefaultLayoutId()) {
      //default control layer layout(handle by attrs)
      if (usePartLeft && leftView == null) {//↓ R.layout.k_ctrl_layer_view
        leftView = findViewById(R.id.part_left_container);
        if (leftView != null && leftView instanceof ViewGroup) {
          //==Use container
          ViewGroup leftContainer = (ViewGroup) this.leftView;
          View left = findViewById(R.id.k_ctrl_layer_part_left);
          if (left != null) {//adjust
            removeView(left);//custom left add leftContainer
            leftContainer.addView(left);
          }
        } else if (leftView == null) {//sub class override default layout id
          //==Non use container
          leftView = findViewById(R.id.k_ctrl_layer_part_left);
        }
      }
    } else {
      if (leftView == null) {
        leftView = findViewById(R.id.k_ctrl_layer_part_left);
      }//custom control layer layout(don't handle default layout attrs)
    }
    // =========@Right@=========
    if (controlLayerLayoutId == getDefaultLayoutId()) {
      //default control layer layout(handle by attrs)
      if (usePartRight && rightView == null) {//↓ R.layout.k_ctrl_layer_view
        rightView = findViewById(R.id.part_right_container);
        if (rightView != null && rightView instanceof ViewGroup) {
          //==Use container
          ViewGroup rightContainer = (ViewGroup) this.rightView;
          View right = findViewById(R.id.k_ctrl_layer_part_right);
          if (right != null) {//adjust
            removeView(right);//custom right add rightContainer
            rightContainer.addView(right);
          }
        } else if (rightView == null) {//sub class override default layout id
          //==Non use container
          rightView = findViewById(R.id.k_ctrl_layer_part_right);
        }
      }
    } else {
      if (rightView == null) {
        rightView = findViewById(R.id.k_ctrl_layer_part_right);
      }//custom control layer layout(don't handle default layout attrs)
    }
    // =========@Middle@=========
    if (controlLayerLayoutId == getDefaultLayoutId()) {
      //default control layer layout(handle by attrs)
      if (usePartMiddle && middleView == null) {//↓ R.layout.k_ctrl_layer_view
        middleView = findViewById(R.id.part_middle_container);
        if (middleView != null && middleView instanceof ViewGroup) {
          //==Use container
          ViewGroup middleContainer = (ViewGroup) this.middleView;
          View middle = findViewById(R.id.k_ctrl_layer_part_middle);
          if (middle == null) {
            LayoutInflater.from(getContext())//use default middle
                .inflate(R.layout.k_ctrl_layer_part_middle, middleContainer);
          } else {//adjust
            removeView(middle);//custom middle add middleContainer
            middleContainer.addView(middle);
          }
        } else if (middleView == null) {//sub class override default layout id
          //==Non use container
          middleView = findViewById(R.id.k_ctrl_layer_part_middle);
        }
      }
    } else {
      if (middleView == null) {
        middleView = findViewById(R.id.k_ctrl_layer_part_middle);
      }//custom control layer layout(don't handle default layout attrs)
    }
    // =========@Other@=========
    bufferView = findViewById(R.id.k_ctrl_layer_part_buffer);
    if (bufferView == null && usePartBuffer) {
      bufferView = findViewById(R.id.part_buffer);//use default
    } else {
      removeView(findViewById(R.id.part_buffer));//remove default
    }
    tipsView = (TextView) findViewById(R.id.k_ctrl_layer_part_tips_tv);
    if (tipsView == null && usePartTips) {
      tipsView = (TextView) findViewById(R.id.part_tips_tv);//use default
    } else {
      removeView(findViewById(R.id.part_tips_tv));//remove default
    }
  }

  public View initPartFromFinishInflate(@IdRes int id) {
    View v = findViewById(id);
    if (v == null) {
      return null;
    }
    if (id == R.id.k_ctrl_layer_part_top) {
      if (partTopMinHeight > 0) {
        v.setMinimumHeight((int) partTopMinHeight);
      }
    } else if (id == R.id.k_ctrl_layer_part_bottom) {
      if (partBottomMinHeight > 0) {
        v.setMinimumHeight((int) partBottomMinHeight);
      }
    }
    return v;
  }

  // ============================@Part/ChildView Operation@============================

  /**
   * @param id part/childView Id
   */
  public View findPart(@IdRes int id) {
    return findViewById(id);
  }

  public View findPart(@IdRes int id, int width, int height) {
    View part = findPart(id);
    ViewGroup.LayoutParams lp =
        part.getLayoutParams();
    lp.width = width;
    lp.height = height;
    part.setLayoutParams(lp);
    return part;
  }

  /**
   * @param id part/childView Id
   */
  public View initPart(@IdRes int id) {
    View view = findPart(id);
    if (view != null && view.getVisibility() != VISIBLE) {
      view.setVisibility(VISIBLE);
    }
    return view;
  }

  /**
   * Init part image button
   *
   * @param id part/childView Id
   * @param resId ImageResource
   */
  public ImageButton initPartIb(@IdRes int id, @DrawableRes int resId,
      CharSequence contentDescription) {
    ImageButton ib = (ImageButton) initPart(id);
    ib.setImageResource(resId);
    ib.setContentDescription(contentDescription);
    return ib;
  }

  /**
   * @param oldPartId part/childView Id
   */
  public void replacePart(@IdRes int oldPartId, View newPartView) {
    View originView = findViewById(oldPartId);
    if (originView != null) {
      newPartView.setLayoutParams(originView.getLayoutParams());
      ViewGroup parent = ((ViewGroup) originView.getParent());
      int controllerIndex = parent.indexOfChild(originView);
      parent.removeView(originView);
      parent.addView(newPartView, controllerIndex);
    }
  }

  /**
   * Remove part in control layer <p /> WARNING: if remove part is smart view, need best invoke
   * {@link #updateSmartView()} go to update smart view.
   *
   * @param id part/childView Id
   */
  public void removePart(@IdRes int id) {
    if (id == R.id.part_buffer || id == R.id.k_ctrl_layer_part_buffer) {
      bufferView = null;
    } else if (id == R.id.part_tips_tv || id == R.id.k_ctrl_layer_part_tips_tv) {
      tipsView = null;
    } else if (id == R.id.part_top_container || id == R.id.k_ctrl_layer_part_top) {
      topView = null;
    } else if (id == R.id.part_bottom_container || id == R.id.k_ctrl_layer_part_bottom) {
      bottomView = null;
    } else if (id == R.id.part_left_container || id == R.id.k_ctrl_layer_part_left) {
      leftView = null;
    } else if (id == R.id.part_right_container || id == R.id.k_ctrl_layer_part_right) {
      rightView = null;
    } else if (id == R.id.part_middle_container || id == R.id.k_ctrl_layer_part_middle) {
      middleView = null;
    }
    View v = findViewById(id);
    if (v != null) {
      ViewGroup parent = (ViewGroup) v.getParent();
      if (parent != null) {
        parent.removeView(v);
      }
    }
  }

  // ============================@T/B/L/R/M Visibility@============================
  private View topView;
  private View bottomView;
  private View leftView;
  private View rightView;
  private View middleView;

  @Override
  public void setVisibilityByInteractionArea(int visibility, boolean animation) {
    if (visibility == VISIBLE) {
      // =========@Show@=========
      //Top
      if (topView != null &&
          (topView.getVisibility() == GONE || topView.getVisibility() == INVISIBLE)) {
        topView.setVisibility(VISIBLE);
        if (usePartViewAnimation && animation) {
          AnimationHelper.showTop(topView, null);
        }
      }
      //Bottom
      if (bottomView != null &&
          (bottomView.getVisibility() == GONE
              || bottomView.getVisibility() == INVISIBLE)) {
        bottomView.setVisibility(VISIBLE);
        if (usePartViewAnimation && animation) {
          AnimationHelper.showBottom(bottomView, null);
        }
      }
      //Left
      if (leftView != null &&
          (leftView.getVisibility() == GONE || leftView.getVisibility() == INVISIBLE)) {
        leftView.setVisibility(VISIBLE);
        if (usePartViewAnimation && animation) {
          AnimationHelper.showLeft(leftView, null);
        }
      }
      //Right
      if (rightView != null &&
          (rightView.getVisibility() == GONE || rightView.getVisibility() == INVISIBLE)) {
        rightView.setVisibility(VISIBLE);
        if (usePartViewAnimation && animation) {
          AnimationHelper.showRight(rightView, null);
        }
      }
      //Middle
      if (middleView != null &&
          (middleView.getVisibility() == GONE
              || middleView.getVisibility() == INVISIBLE)) {
        middleView.setVisibility(VISIBLE);
        if (usePartViewAnimation && animation) {
          AnimationHelper.showMiddle(middleView, null);
        }
      }
    } else if (!interactionAreaAlwaysVisible && (visibility == GONE || visibility == INVISIBLE)) {
      // =========@Hide@=========
      //Top
      if (topView != null &&
          topView.getVisibility() == VISIBLE) {
        if (usePartViewAnimation && animation) {
          AnimationHelper
              .hideTop(topView, new AnimationHelper.AnimationActionListener() {
                public void onAnimationEnd() {
                  topView.setVisibility(GONE);
                }
              });
        } else {
          topView.setVisibility(GONE);
        }
      }
      //Bottom
      if (bottomView != null &&
          bottomView.getVisibility() == VISIBLE) {
        if (usePartViewAnimation && animation) {
          AnimationHelper
              .hideBottom(bottomView, new AnimationHelper.AnimationActionListener() {
                public void onAnimationEnd() {
                  bottomView.setVisibility(GONE);
                }
              });
        } else {
          bottomView.setVisibility(GONE);
        }
      }
      //Left
      if (leftView != null &&
          leftView.getVisibility() == VISIBLE) {
        if (usePartViewAnimation && animation) {
          AnimationHelper
              .hideLeft(leftView, new AnimationHelper.AnimationActionListener() {
                public void onAnimationEnd() {
                  leftView.setVisibility(GONE);
                }
              });
        } else {
          leftView.setVisibility(GONE);
        }
      }
      //Right
      if (rightView != null &&
          rightView.getVisibility() == VISIBLE) {
        if (usePartViewAnimation && animation) {
          AnimationHelper
              .hideRight(rightView, new AnimationHelper.AnimationActionListener() {
                public void onAnimationEnd() {
                  rightView.setVisibility(GONE);
                }
              });
        } else {
          rightView.setVisibility(GONE);
        }
      }
      //Middle
      if (middleView != null &&
          middleView.getVisibility() == VISIBLE) {
        if (usePartViewAnimation && animation) {
          AnimationHelper
              .hideMiddle(middleView, new AnimationHelper.AnimationActionListener() {
                public void onAnimationEnd() {
                  middleView.setVisibility(GONE);
                }
              });
        } else {
          middleView.setVisibility(GONE);
        }
      }
    }
  }

  @Override
  public boolean isVisibleByInteractionArea() {
    return (topView != null && topView.getVisibility() == VISIBLE) ||
        (bottomView != null && bottomView.getVisibility() == VISIBLE) ||
        (leftView != null && leftView.getVisibility() == VISIBLE) ||
        (rightView != null && rightView.getVisibility() == VISIBLE) ||
        (middleView != null && middleView.getVisibility() == VISIBLE);
  }

  @Override
  public boolean isVisibleByPlayController() {
    return isVisibleByInteractionArea();
  }

  // ============================@X View@============================
  // ============================@Buffer
  protected View bufferView;

  @Override
  public boolean showBufferingView(boolean show) {
    if (!super.showBufferingView(show) && bufferView != null) {
      bufferView.setVisibility(show ? VISIBLE : GONE);
    }
    return true;
  }

  // ============================@Tips
  private TextView tipsView;

  @Override
  public boolean showTipsView(boolean show, CharSequence text, @DrawableRes int icon) {
    if (!super.showTipsView(show, text, icon)) {
      if (tipsView == null) {
        return false;
      }
      if (show) {
        tipsView.setVisibility(VISIBLE);
        if (!TextUtils.isEmpty(text)) {
          tipsView.setText(text);
        }
        if (icon != -1) {
          tipsView.setCompoundDrawablesWithIntrinsicBounds(
              0, icon, 0, 0);
        }
      } else if (tipsView.getVisibility() == VISIBLE) {
        tipsView.setVisibility(GONE);
      }
    }
    return true;
  }
}