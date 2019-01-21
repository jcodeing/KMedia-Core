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
package com.jcodeing.kmedia.definition;

import androidx.annotation.IntDef;
import com.jcodeing.kmedia.IPlayer;
import com.jcodeing.kmedia.Player;
import com.jcodeing.kmedia.assist.C;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

public interface IMediaQueue {

  /**
   * Don't have to manually call, automatically initialized in the {@link
   * Player#setMediaQueue(IMediaQueue)}
   */
  void init(IPlayer player);

  /**
   * destroy this queue
   */
  void destroy();

  // ============================@Queue

  /**
   * update this queue, with external use the same object, not independent maintenance
   */
  void update(List<? extends IMediaItem> newQueue);

  /**
   * Returns <tt>true</tt> if this queue contains no {@link IMediaItem}s.
   *
   * @return <tt>true</tt> if this queue contains no {@link IMediaItem}s
   */
  boolean isEmpty();

  /**
   * Returns the number of {@link IMediaItem}s in this queue.  If this queue contains more than
   * <tt>Integer.MAX_VALUE</tt> {@link IMediaItem}s, returns <tt>Integer.MAX_VALUE</tt>.
   *
   * @return the number of {@link IMediaItem}s in this queue
   */
  int size();

  /**
   * Returns the index of the first occurrence of the specified mediaItem in this queue, or -1 if
   * this queue does not contain the mediaItem.
   */
  int indexOf(IMediaItem item);

  /**
   * Removes the mediaItem at the specified position in this queue.
   *
   * @param index the index of the mediaItem to be removed
   * @return the mediaItem previously at the specified position
   */
  IMediaItem remove(int index);

  /**
   * Removes all of the {@link IMediaItem}s from this queue (optional operation). The queue will be
   * empty after this call returns.
   */
  void clear();

  // ============================@MediaItem
  IMediaItem getMediaItem(int index);

  IMediaItem getMediaItem(String mediaId);

  IMediaItem getCurrentMediaItem();

  // ============================@Index
  int getCurrentIndex();

  /**
   * set the current queue index
   *
   * @param index queue index
   * @return is set success
   */
  boolean setCurrentIndex(int index);

  /**
   * set the current queue index from the media Id
   *
   * @param mediaId {@link IMediaItem#getMediaId()}
   * @return is set success
   */
  boolean setCurrentIndex(String mediaId);

  /**
   * seek queue index by media id
   *
   * @param mediaId {@link IMediaItem#getMediaId()}
   * @return mediaId->queueIndex or -1(didn't find)
   */
  int seekIndexByMediaId(String mediaId);

  /**
   * @return a random queue index
   */
  int getRandomIndex();

  // ============================@Skip@============================

  /**
   * Skip to the specify queue index media item.
   *
   * @param index queue index
   * @return is skip success
   */
  boolean skipToIndex(int index);

  /**
   * skipToIndex(currentIndex + increment)
   *
   * @see #skipToIndex(int)
   */
  boolean skipToIndexByIncrement(int increment);

  /**
   * Skip to the next media item. <p>skipToIndexByIncrement(1)<p/>
   */
  boolean skipToNext();

  /**
   * Skip to the previous media item. <p>skipToIndexByIncrement(-1)<p/>
   */
  boolean skipToPrevious();

  boolean skipToRandom();

  void setAutoSkipMode(@AutoSkipMode int autoSkipMode);

  /**
   * <p>*param loopMode can use {@link C#PARAM_ORIGINAL} keeping the original values <p>*param
   * loopMode can use {@link C#PARAM_UNSET}/{@link C#PARAM_RESET} unset/reset values<p/><p/>
   */
  void setItemLoop(int loopMode);

  int getAutoSkipMode();

  /**
   * @return is skip success or have processing
   */
  boolean skipToAutoAssigned();

  // ============================@Listener
  interface Listener {

    void onQueueUpdated(List<? extends IMediaItem> newQueue);

    void onItemRemoved(int index);

    void onCurrentQueueIndexUpdated(int index);

    boolean onSkipQueueIndex(int index);
  }

  void addListener(Listener listener);

  void removeListener(Listener listener);

  // ============================@Constants
  //Do not change these values

  int AUTO_SKIP_MODE_LIST_LOOP = 1;
  int AUTO_SKIP_MODE_RANDOM = 2;
  int AUTO_SKIP_MODE_SINGLE = 3;
  int AUTO_SKIP_MODE_SINGLE_LOOP = 31;
  int AUTO_SKIP_MODE_SINGLE_ONCE = 32;

  /**
   * auto skip modes for {@link #setAutoSkipMode(int)}
   */
  @Retention(RetentionPolicy.SOURCE)
  @IntDef({AUTO_SKIP_MODE_LIST_LOOP, AUTO_SKIP_MODE_RANDOM,
      AUTO_SKIP_MODE_SINGLE, AUTO_SKIP_MODE_SINGLE_LOOP, AUTO_SKIP_MODE_SINGLE_ONCE})
  public @interface AutoSkipMode {

  }
}