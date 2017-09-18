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

/**
 * Defines assist constants used by the media framework. <p>usual int constant use nine digits
 * defines (Integer.MAX_VALUE 2147483647)<p/>
 */
public final class C {

  private C() {
  }

  // ============================@PARAM@============================
  /**
   * Use: setting method of int param keeping the original values <p /> Structured approach:
   * C([3])+PARAM([1611]8113)+ORIGINAL([1518]97914112) <p /> WARNING: Please avoid repeat with
   * parameters, if repeat more likely see {@link PARAM}
   */
  public static final int PARAM_ORIGINAL = 316111518;
  /**
   * Use: setting method of int param reset values <p /> Structured approach:
   * C([3])+PARAM([1611]8113)+RESET([1851]9520) <p /> WARNING: Please avoid repeat with parameters,
   * if repeat more likely see {@link PARAM}
   */
  public static final int PARAM_RESET = 316111851;
  /**
   * WARNING: Please avoid repeat with parameters, if repeat more likely see {@link PARAM}
   *
   * @see #PARAM_RESET (PARAM_UNSET = PARAM_RESET)
   */
  public static final int PARAM_UNSET = PARAM_RESET;

  // ============================@Extend
  public enum PARAM {
    /**
     * Use: setting method of custom param keeping the original values
     */
    ORIGINAL,
    /**
     * Use: setting method of custom param reset values
     */
    RESET,
    /**
     * @see #RESET
     */
    UNSET,
    /**
     * Use: setting method of custom param force set values
     */
    FORCE
  }

  // ============================@CMD@============================
  // ============================@RETURN
  /**
   * a subclass to override a method, call superclass this method. need handled return command.
   * Premise in the superclass Java doc has specific requirements. (superclass Java doc: @return CMD
   * (need handle return command))
   * <pre>
   *  e.g.
   * {@literal @}Override
   *  public int onCompletion() {
   *    //handle return CMD
   *    if (super.onCompletion() == C.CMD_RETURN_FORCED) {
   *      return C.CMD_RETURN_FORCED;
   *    }
   *  }
   * </pre>
   * <p>Structured approach: C([3])+CMD([313]4)+RETURN([185]20211814)+FORCED([61]518354)<p/>
   */
  public static final int CMD_RETURN_FORCED = 331318561;
  /**
   * <p>Structured approach: C([3])+CMD([313]4)+RETURN([185]20211814)+NORMAL([14]151813112)<p/>
   */
  public static final int CMD_RETURN_NORMAL = 331318514;

  // ============================@UNSET@============================
  /**
   * Represents an unset or unknown position.
   */
  public static final int POSITION_UNSET = -1;
  /**
   * Represents an unset or unknown index.
   */
  public static final int INDEX_UNSET = -1;
  /**
   * Special constant representing an unset or unknown time or duration. Suitable for use in any
   * time base.
   */
  public static final long TIME_UNSET = Long.MIN_VALUE + 1;

  // ============================@STATE@============================
  // ============================@PROGRESS
  /**
   * ...[(S)-E]... <p>Position unit start<p/>
   */
  public static final int STATE_PROGRESS_POS_UNIT_START = 1;
  /**
   * ...[S(-)E]... <p>Position unit mid<p/>
   */
  public static final int STATE_PROGRESS_POS_UNIT_MID = 2;
  /**
   * ...[S-(E)]... <p>Position unit end<p/>
   */
  public static final int STATE_PROGRESS_POS_UNIT_END = 3;
  /**
   * ...[S-(E)]... <p>Position unit finish(loop...)<p/>
   */
  public static final int STATE_PROGRESS_POS_UNIT_FINISH = 4;

  /**
   * ...[(A)-B]... <p>A-B start<p/>
   */
  public static final int STATE_PROGRESS_AB_START = STATE_PROGRESS_POS_UNIT_START;
  /**
   * ...[A(-)B]... <p>A-B mid<p/>
   */
  public static final int STATE_PROGRESS_AB_MID = STATE_PROGRESS_POS_UNIT_MID;
  /**
   * ...[A-(B)]... <p>A-B end<p/>
   */
  public static final int STATE_PROGRESS_AB_END = STATE_PROGRESS_POS_UNIT_END;
  /**
   * ...[A-(B)]... <p>A-B finish(loop...)<p/>
   */
  public static final int STATE_PROGRESS_AB_FINISH = STATE_PROGRESS_POS_UNIT_FINISH;
}