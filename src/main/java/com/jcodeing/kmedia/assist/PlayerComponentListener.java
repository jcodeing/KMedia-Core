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

import com.jcodeing.kmedia.PlayerListener;
import com.jcodeing.kmedia.definition.IMediaItem;
import com.jcodeing.kmedia.definition.IMediaQueue;
import java.util.List;

/**
 * Simple Component listener
 */
public class PlayerComponentListener extends PlayerListener implements IMediaQueue.Listener {

  @Override
  public void onQueueUpdated(List<? extends IMediaItem> newQueue) {
    //Do nothing
  }

  @Override
  public void onItemRemoved(int index) {
    //Do nothing
  }

  @Override
  public void onCurrentQueueIndexUpdated(int index) {
    //Do nothing
  }

  @Override
  public boolean onSkipQueueIndex(int index) {
    return false;
  }
}