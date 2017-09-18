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

import android.app.Activity;
import com.jcodeing.kmedia.IPlayer;

/**
 * Auto Play Pause Helper. <p /> Assist control player on resume/pause to auto play/pause.
 */
public class AutoPlayPauseHelper {

  public AutoPlayPauseHelper() {
  }

  public AutoPlayPauseHelper(PlayerAskFor playerAskFor) {
    setPlayerAskFor(playerAskFor);
  }

  // ============================@PlayerAskFor@============================
  private PlayerAskFor playerAskFor;

  /**
   * Assist internal complete a full feats. <p /> mainly applied to auto play/pause player.
   */
  public void setPlayerAskFor(PlayerAskFor playerAskFor) {
    this.playerAskFor = playerAskFor;
  }

  public interface PlayerAskFor {

    IPlayer player();
  }

  // ============================@Play/Pause@============================
  private boolean autoPlayFromOnResume = false;
  private long resumePosition = C.POSITION_UNSET;

  public void setAutoPlayFromOnResume(boolean autoPlayFromOnResume) {
    this.autoPlayFromOnResume = autoPlayFromOnResume;
  }

  public boolean isAutoPlayFromOnResume() {
    return autoPlayFromOnResume;
  }

  public boolean haveResumePosition() {
    return resumePosition != C.POSITION_UNSET;
  }

  /**
   * Get lifecycle onPause() -> player.getCurrentPosition() <p> with consumed resumePosition <p/>
   */
  public long getResumePosition() {
    if (haveResumePosition()) {
      long ms = resumePosition;
      resumePosition = C.POSITION_UNSET;
      return ms;//consumed
    }
    return C.POSITION_UNSET;
  }

  /**
   * Call me in your The Activity
   *
   * @see Activity#onResume()
   */
  public void onResume() {
    // =========@AutoPlayFromOnResume@=========
    IPlayer player = playerAskFor != null ? playerAskFor.player() : null;
    if (autoPlayFromOnResume && player != null && player.isPlayable() && !player.isPlaying()) {
      // player.start();//[1]
      // ExoPlayer direct call start(), playerView will be a few seconds video motionless
      // So here call seekTo(resumePosition). A temporary solution.
      if (haveResumePosition()) {//[2]
        player.shouldAutoPlayWhenSeekComplete(true);
        player.seekTo(getResumePosition());
      } else {
        player.start();
      }
    }
  }

  /**
   * Call me in your The Activity
   *
   * @see Activity#onPause()
   */
  public void onPause() {
    IPlayer player = playerAskFor != null ? playerAskFor.player() : null;
    autoPlayFromOnResume = player != null && player.isPlaying();
    if (autoPlayFromOnResume) {
      player.pause();
    }
    resumePosition = Math.max(0, player != null ? player.getCurrentPosition() : -1);
  }
}