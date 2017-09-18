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
package com.jcodeing.kmedia.view;

import android.view.View;

public interface ProgressAny {

  /**
   * @see View#isEnabled()
   */
  void setEnabled(boolean enabled);

  // ============================@Progress@============================

  /**
   * Get the progress any's current level of progress.
   */
  int getProgress();

  /**
   * Sets the current progress to the specified value.
   *
   * @param progress the new progress, between 0 and {@link #getMax()}
   */
  void setProgress(int progress);

  /**
   * Get the progress any's current level of secondary progress.
   */
  int getSecondaryProgress();

  /**
   * Set the current secondary progress to the specified value.
   *
   * @param secondaryProgress the new secondary progress, between 0 and {@link #getMax()}
   */

  void setSecondaryProgress(int secondaryProgress);

  /**
   * Return the upper limit of this progress any's range.
   */
  int getMax();

  /**
   * Set the range of the progress any to (0-max)
   *
   * @param max the upper range of this progress any
   */

  void setMax(int max);

  // ============================@Listener@============================
  void setOnChangeListener(OnChangeListener onChangeListener);

  interface OnChangeListener {

    void onProgressChanged(ProgressAny progressAny, int progress, boolean fromUser);

    void onStartTrackingTouch(ProgressAny progressAny);

    void onStopTrackingTouch(ProgressAny progressAny);
  }
}