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

import com.jcodeing.kmedia.definition.IPositionUnit;
import com.jcodeing.kmedia.definition.IPositionUnitList;
import java.util.List;

public class PositionsHelper {

  /**
   * Return the start index (Within the current Position scope)
   *
   * @param curPos CurrentPosition
   * @return LeftStartIndex [LeftStartPos <= curPos < RightStartPos] or -1 [< 0] (not present)
   */
  public static int searchStartIndex(List<? extends IPositionUnit> positionUnits, long curPos) {
    if (positionUnits == null || positionUnits.size() <= 0 || curPos < 0) {
      return -1;//not present
    }

    // LeftStartPos <= curPos < RightStartPos
    // =========@binarySearch@=========
    int loIndex = 0;
    int hiIndex = positionUnits.size() - 1;

    while (loIndex <= hiIndex) {
      final int midIndex = (loIndex + hiIndex) >>> 1;
      final long midStartPos = positionUnits.get(midIndex).getStartPos();

      if (midStartPos < curPos) {
        loIndex = midIndex + 1;
        if (loIndex <= hiIndex && positionUnits.get(loIndex).getStartPos() > curPos) {
          return midIndex;
        }
      } else if (midStartPos > curPos) {
        hiIndex = midIndex - 1;
        if (loIndex <= hiIndex && positionUnits.get(hiIndex).getStartPos() <= curPos) {
          return hiIndex;
        }
      } else {
        return midIndex;
      }
    }

    return hiIndex;
  }

  /**
   * Return the start index (Within the current Position scope)
   *
   * @param curPos CurrentPosition
   * @return LeftStartIndex [LeftStartPos <= curPos < RightStartPos] or -1 [< 0] (not present)
   */
  public static int searchStartIndex(IPositionUnitList positionUnitList, long curPos) {
    if (positionUnitList == null || positionUnitList.positionUnitSize() <= 0 || curPos < 0) {
      return -1;//not present
    }

    // LeftStartPos <= curPos < RightStartPos
    // =========@binarySearch@=========
    int loIndex = 0;
    int hiIndex = positionUnitList.positionUnitSize() - 1;

    while (loIndex <= hiIndex) {
      final int midIndex = (loIndex + hiIndex) >>> 1;
      final long midStartPos = positionUnitList.getStartPosition(midIndex);

      if (midStartPos < curPos) {
        loIndex = midIndex + 1;
        if (loIndex <= hiIndex && positionUnitList.getStartPosition(loIndex) > curPos) {
          return midIndex;
        }
      } else if (midStartPos > curPos) {
        hiIndex = midIndex - 1;
        if (loIndex <= hiIndex && positionUnitList.getStartPosition(hiIndex) <= curPos) {
          return hiIndex;
        }
      } else {
        return midIndex;
      }
    }

    return hiIndex;
  }
}