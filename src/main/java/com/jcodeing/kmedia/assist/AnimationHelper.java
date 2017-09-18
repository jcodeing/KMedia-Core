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
package com.jcodeing.kmedia.assist;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

/**
 * Simple Animation Helper. <p /> Mainly for view to provide show/hide basic animation.
 */
public final class AnimationHelper {

  private AnimationHelper() {
    super();
  }

  // ============================@Top@============================
  public static void showTop(final View view,
      final AnimationActionListener mAnimationActionListener) {
    if (view.getAnimation() == null) {
      TranslateAnimation tA = new TranslateAnimation(
          Animation.RELATIVE_TO_SELF, 0f,
          Animation.RELATIVE_TO_SELF, 0f,
          Animation.RELATIVE_TO_SELF, -1f,
          Animation.RELATIVE_TO_SELF, 0f);
      tA.setDuration(300);
      tA.setRepeatCount(0);
      tA.setFillAfter(false);
      tA.setAnimationListener(new Animation.AnimationListener() {
        public void onAnimationEnd(Animation animation) {
          if (mAnimationActionListener != null) {
            mAnimationActionListener.onAnimationEnd();
          }

          view.clearAnimation();
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationStart(Animation animation) {
        }
      });
      view.startAnimation(tA);
    }
  }

  public static void hideTop(final View view,
      final AnimationActionListener mAnimationActionListener) {
    if (view.getAnimation() == null) {
      TranslateAnimation tA = new TranslateAnimation(
          Animation.RELATIVE_TO_SELF, 0f,
          Animation.RELATIVE_TO_SELF, 0f,
          Animation.RELATIVE_TO_SELF, 0f,
          Animation.RELATIVE_TO_SELF, -1f);
      tA.setDuration(300);
      tA.setRepeatCount(0);
      tA.setFillAfter(false);
      tA.setAnimationListener(new Animation.AnimationListener() {
        public void onAnimationEnd(Animation animation) {
          if (mAnimationActionListener != null) {
            mAnimationActionListener.onAnimationEnd();
          }

          view.clearAnimation();
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationStart(Animation animation) {
        }
      });
      view.startAnimation(tA);
    }
  }

  // ============================@Bottom@============================
  public static void showBottom(final View view,
      final AnimationActionListener mAnimationActionListener) {
    if (view.getAnimation() == null) {
      TranslateAnimation tA = new TranslateAnimation(
          Animation.RELATIVE_TO_SELF, 0f,
          Animation.RELATIVE_TO_SELF, 0f,
          Animation.RELATIVE_TO_SELF, 1f,
          Animation.RELATIVE_TO_SELF, 0f);
      tA.setDuration(300);
      tA.setRepeatCount(0);
      tA.setFillAfter(false);
      tA.setAnimationListener(new Animation.AnimationListener() {
        public void onAnimationEnd(Animation animation) {
          if (mAnimationActionListener != null) {
            mAnimationActionListener.onAnimationEnd();
          }

          view.clearAnimation();
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationStart(Animation animation) {
        }
      });
      view.startAnimation(tA);
    }
  }

  public static void hideBottom(final View view,
      final AnimationActionListener mAnimationActionListener) {
    if (view.getAnimation() == null) {
      TranslateAnimation tA = new TranslateAnimation(
          Animation.RELATIVE_TO_SELF, 0f,
          Animation.RELATIVE_TO_SELF, 0f,
          Animation.RELATIVE_TO_SELF, 0f,
          Animation.RELATIVE_TO_SELF, 1f);
      tA.setDuration(300);
      tA.setRepeatCount(0);
      tA.setFillAfter(false);
      tA.setAnimationListener(new Animation.AnimationListener() {
        public void onAnimationEnd(Animation animation) {
          if (mAnimationActionListener != null) {
            mAnimationActionListener.onAnimationEnd();
          }

          view.clearAnimation();
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationStart(Animation animation) {
        }
      });
      view.startAnimation(tA);
    }
  }

  // ============================@Left@============================
  public static void showLeft(final View view,
      final AnimationActionListener mAnimationActionListener) {
    if (view.getAnimation() == null) {
      TranslateAnimation tA = new TranslateAnimation(
          Animation.RELATIVE_TO_SELF, -1f,
          Animation.RELATIVE_TO_SELF, 0f,
          Animation.RELATIVE_TO_SELF, 0f,
          Animation.RELATIVE_TO_SELF, 0f);
      tA.setDuration(300);
      tA.setRepeatCount(0);
      tA.setFillAfter(false);
      tA.setAnimationListener(new Animation.AnimationListener() {
        public void onAnimationEnd(Animation animation) {
          if (mAnimationActionListener != null) {
            mAnimationActionListener.onAnimationEnd();
          }

          view.clearAnimation();
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationStart(Animation animation) {
        }
      });
      view.startAnimation(tA);
    }
  }

  public static void hideLeft(final View view,
      final AnimationActionListener mAnimationActionListener) {
    if (view.getAnimation() == null) {
      TranslateAnimation tA = new TranslateAnimation(
          Animation.RELATIVE_TO_SELF, 0f,
          Animation.RELATIVE_TO_SELF, -1f,
          Animation.RELATIVE_TO_SELF, 0f,
          Animation.RELATIVE_TO_SELF, 0f);
      tA.setDuration(300);
      tA.setRepeatCount(0);
      tA.setFillAfter(false);
      tA.setAnimationListener(new Animation.AnimationListener() {
        public void onAnimationEnd(Animation animation) {
          if (mAnimationActionListener != null) {
            mAnimationActionListener.onAnimationEnd();
          }
          view.clearAnimation();
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationStart(Animation animation) {
        }
      });
      view.startAnimation(tA);
    }
  }

  // ============================@Right@============================
  public static void showRight(final View view,
      final AnimationActionListener mAnimationActionListener) {
    if (view.getAnimation() == null) {
      TranslateAnimation tA = new TranslateAnimation(
          Animation.RELATIVE_TO_SELF, 1f,
          Animation.RELATIVE_TO_SELF, 0f,
          Animation.RELATIVE_TO_SELF, 0f,
          Animation.RELATIVE_TO_SELF, 0f);
      tA.setDuration(300);
      tA.setRepeatCount(0);
      tA.setFillAfter(false);
      tA.setAnimationListener(new Animation.AnimationListener() {
        public void onAnimationEnd(Animation animation) {
          if (mAnimationActionListener != null) {
            mAnimationActionListener.onAnimationEnd();
          }

          view.clearAnimation();
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationStart(Animation animation) {
        }
      });
      view.startAnimation(tA);
    }
  }

  public static void hideRight(final View view,
      final AnimationActionListener mAnimationActionListener) {
    if (view.getAnimation() == null) {
      TranslateAnimation tA = new TranslateAnimation(
          Animation.RELATIVE_TO_SELF, 0f,
          Animation.RELATIVE_TO_SELF, 1f,
          Animation.RELATIVE_TO_SELF, 0f,
          Animation.RELATIVE_TO_SELF, 0f);
      tA.setDuration(300);
      tA.setRepeatCount(0);
      tA.setFillAfter(false);
      tA.setAnimationListener(new Animation.AnimationListener() {
        public void onAnimationEnd(Animation animation) {
          if (mAnimationActionListener != null) {
            mAnimationActionListener.onAnimationEnd();
          }

          view.clearAnimation();
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationStart(Animation animation) {
        }
      });
      view.startAnimation(tA);
    }
  }

  // ============================@Middle@============================
  public static void showMiddle(final View view,
      final AnimationActionListener mAnimationActionListener) {
    if (view.getAnimation() == null) {
      ScaleAnimation sA = new ScaleAnimation(
          0f, 1f,
          0f, 1f,
          Animation.RELATIVE_TO_SELF, 0.5f,
          Animation.RELATIVE_TO_SELF, 0.5f);
      sA.setDuration(300);
      sA.setRepeatCount(0);
      sA.setFillAfter(false);
      sA.setAnimationListener(new Animation.AnimationListener() {
        public void onAnimationEnd(Animation animation) {
          if (mAnimationActionListener != null) {
            mAnimationActionListener.onAnimationEnd();
          }

          view.clearAnimation();
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationStart(Animation animation) {
        }
      });
      view.startAnimation(sA);
    }
  }

  public static void hideMiddle(final View view,
      final AnimationActionListener mAnimationActionListener) {
    if (view.getAnimation() == null) {
      ScaleAnimation sA = new ScaleAnimation(
          1f, 0f,
          1f, 0f,
          Animation.RELATIVE_TO_SELF, 0.5f,
          Animation.RELATIVE_TO_SELF, 0.5f);
      sA.setDuration(300);
      sA.setRepeatCount(0);
      sA.setFillAfter(false);
      sA.setAnimationListener(new Animation.AnimationListener() {
        public void onAnimationEnd(Animation animation) {
          if (mAnimationActionListener != null) {
            mAnimationActionListener.onAnimationEnd();
          }

          view.clearAnimation();
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationStart(Animation animation) {
        }
      });
      view.startAnimation(sA);
    }
  }

  // ============================@Listener@============================
  public interface AnimationActionListener {

    void onAnimationEnd();
  }
}