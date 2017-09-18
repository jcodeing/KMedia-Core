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
package com.jcodeing.kmedia.utils;

import android.view.ViewGroup.LayoutParams;
import java.util.Collection;

/**
 * Provides methods for asserting the truth of expressions and properties.
 */
public final class Assert {

  private Assert() {
  }

  /**
   * @param index The index to check.
   * @param start The start of the allowed range (inclusive).
   * @param limit The end of the allowed range (exclusive).
   * @return index >= start && index < limit <ul> <li><tt>true</tt>: if {@code index} outside the
   * specified bounds. <li><tt>false</tt>: if {@code index} falls outside the specified bounds.
   * <ul/>
   */
  public static boolean checkIndex(int index, int start, int limit) {
    return index >= start && index < limit;
  }

  /**
   * @param index The index to check.
   * @param limit The end of the allowed range (exclusive).
   * @return index >= 0 && index < limit <ul> <li><tt>true</tt>: if {@code index} outside the
   * specified bounds. <li><tt>false</tt>: if {@code index} falls outside the specified bounds.
   * <ul/>
   */
  public static boolean checkIndex(int index, int limit) {
    return checkIndex(index, 0, limit);
  }

  /**
   * Throws {@link IllegalArgumentException} if {@code string}
   *
   * @param collection {@link Collection}
   * @return collection != null && !collection.isEmpty() <ul> <li> <tt>true</tt>: if is null or this
   * collection contains no elements. <li><tt>false</tt>: if non-null, non-empty. <ul/>
   */
  public static boolean checkNotEmpty(Collection collection) {
    return collection != null && !collection.isEmpty();
  }

  /**
   * @return is not special value for the height or width
   * @see LayoutParams#MATCH_PARENT
   * @see LayoutParams#WRAP_CONTENT
   */
  public static boolean checkNotSpecialWidthHeight(int value) {
    return value != LayoutParams.MATCH_PARENT &&
        value != LayoutParams.WRAP_CONTENT;
  }

  /**
   * <pre>
   * Open : { x | a < x < b }
   * Closed : { x | a <= x <= b }
   * Left-closed,right-open : { x | a <= x < b }
   * Left-open,right-closed : { x | a < x <= b }
   * </pre>
   * WARNING: open interval special situation(a>b), return origin x.
   *
   * @param left_open True open(a < x), false closed(a <= x)
   * @param right_open True open(x > b), false closed(x >= b)
   */
  public static int reviseInterval(int x, int a, int b, boolean left_open,
      boolean right_open) {
    if (!left_open && right_open) {//a <= x < b
      if (x >= b) {
        x = b - 1;//x < b
      }
      return x < a ? a : x;//a <= x
    } else if (!left_open /*&& !right_open*/) {//a <= x <= b
      if (x > b) {
        x = b;//x <= b
      }
      return x < a ? a : x;//a <= x
    } else if (/*left_open &&*/ !right_open) {//a < x <= b
      if (x <= a) {
        x = a + 1;//a < x
      }
      return x > b ? b : x;//x <= b
    } else /*if (left_open && right_open)*/ {//a < x < b
      int originX = x;
      if (x >= b) {
        x = b - 1;//x < b
      }
      if (x <= a) {
        x = a + 1;//a < x
      }
      return x < b ? x : originX;
    }
  }

  /**
   * <pre>
   * Open : { x | a < x < b }
   * Closed : { x | a <= x <= b }
   * Left-closed,right-open : { x | a <= x < b }
   * Left-open,right-closed : { x | a < x <= b }
   * </pre>
   * WARNING: open interval special situation(a>b), return origin x.
   *
   * @param left_open True open(a < x), false closed(a <= x)
   * @param right_open True open(x > b), false closed(x >= b)
   */
  public static long reviseInterval(long x, long a, long b, boolean left_open,
      boolean right_open) {
    if (!left_open && right_open) {//a <= x < b
      if (x >= b) {
        x = b - 1;//x < b
      }
      return x < a ? a : x;//a <= x
    } else if (!left_open /*&& !right_open*/) {//a <= x <= b
      if (x > b) {
        x = b;//x <= b
      }
      return x < a ? a : x;//a <= x
    } else if (/*left_open &&*/ !right_open) {//a < x <= b
      if (x <= a) {
        x = a + 1;//a < x
      }
      return x > b ? b : x;//x <= b
    } else /*if (left_open && right_open)*/ {//a < x < b
      long originX = x;
      if (x >= b) {
        x = b - 1;//x < b
      }
      if (x <= a) {
        x = a + 1;//a < x
      }
      return x < b ? x : originX;
    }
  }

  /**
   * <pre>
   * Open : { x | a < x < b }
   * Closed : { x | a <= x <= b }
   * Left-closed,right-open : { x | a <= x < b }
   * Left-open,right-closed : { x | a < x <= b }
   * </pre>
   * WARNING: open interval special situation(a>b), return origin x.
   *
   * @param left_open True open(a < x), false closed(a <= x)
   * @param right_open True open(x > b), false closed(x >= b)
   */
  public static float reviseInterval(float x, float a, float b, boolean left_open,
      boolean right_open) {
    if (!left_open && right_open) {//a <= x < b
      if (x >= b) {
        x = b - 1;//x < b
      }
      return x < a ? a : x;//a <= x
    } else if (!left_open /*&& !right_open*/) {//a <= x <= b
      if (x > b) {
        x = b;//x <= b
      }
      return x < a ? a : x;//a <= x
    } else if (/*left_open &&*/ !right_open) {//a < x <= b
      if (x <= a) {
        x = a + 1;//a < x
      }
      return x > b ? b : x;//x <= b
    } else /*if (left_open && right_open)*/ {//a < x < b
      float originX = x;
      if (x >= b) {
        x = b - 1;//x < b
      }
      if (x <= a) {
        x = a + 1;//a < x
      }
      return x < b ? x : originX;
    }
  }

  /**
   * @param index The index to check.
   * @param start The start of the allowed range (inclusive).
   * @param limit The end of the allowed range (exclusive).
   * @return Revised index (index >= start && index < limit)
   */
  public static int reviseIndex(int index, int start, int limit) {
    if (index >= limit) {
      index = limit - 1;
    }
    return index < start ? start : index;
  }

  /**
   * @param index The index to check.
   * @param limit The end of the allowed range (exclusive).
   * @return Revised index (index >= 0 && index < limit)
   */
  public static int reviseIndex(int index, int limit) {
    return reviseIndex(index, 0, limit);
  }
}