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

import android.text.TextUtils;
import android.util.Log;

/**
 * L~ a thoughtful(~) log(L)
 */
public class L {

  public static boolean ENABLE = true;
  public static int DEFAULT_PRINT_STACK_TRACE_METHOD_COUNT = 9;

  // ============================@TAG@============================
  public static final String TAG = "L~";

  /**
   * @return L~CustomString
   */
  public static String makeTag(String str) {
    return TAG + str;
  }

  /**
   * @return L~ClassSimpleName
   */
  public static String makeTag(Class cls) {
    return cls != null ? makeTag(cls.getSimpleName() + cls.toString()) : TAG;
  }

  /**
   * @return L~ClassSimpleName@HexHashCode
   */
  public static String makeTag(Object obj) {
    return obj != null ?
        makeTag(obj.getClass().getSimpleName() + "@" + Integer.toHexString(obj.hashCode())) : TAG;
  }

  // ============================@Priority LOG@============================
  public static void v(String tag, String msg) {
    if (ENABLE) {
      Log.v(tag, msg);
    }
  }

  public static void d(String msg) {
    d(TAG, msg);
  }

  public static void d(String tag, String msg) {
    if (ENABLE) {
      Log.d(tag, msg);
    }
  }

  public static void dd(String tag, String msg) {
    if (ENABLE) {
      printStackTrace(Log.DEBUG, tag, msg, 0, DEFAULT_PRINT_STACK_TRACE_METHOD_COUNT);
    }
  }

  public static void ds(String space, Object... msg) {
    if (ENABLE) {
      println(Log.DEBUG, TAG, space, msg);
    }
  }

  public static void i(String tag, String msg) {
    if (ENABLE) {
      Log.i(tag, msg);
    }
  }

  public static void w(String tag, String msg) {
    if (ENABLE) {
      Log.w(tag, msg);
    }
  }

  public static void e(String tag, String msg, Throwable tr) {
    if (ENABLE) {
      Log.e(tag, msg, tr);
    }
  }

  // ============================@Println@============================
  public static void println(int priority, String tag, String space, Object... messages) {
    if (messages == null || messages.length <= 0) {
      return;
    }
    if (messages.length == 1) {
      Log.println(priority, tag, messages[0].toString());
    } else {
      StringBuilder sb = new StringBuilder();
      for (Object m : messages) {
        sb.append(m);
        if (!TextUtils.isEmpty(space)) {
          sb.append(space);
        }
      }
      Log.println(priority, tag, sb.toString());
    }
  }

  public static void printStackTrace(Throwable e) {
    if (ENABLE && e != null) {
      e.printStackTrace();
    }
  }

  public static void printStackTrace(int priority, String tag, String msg,
      int methodOffset, int methodCount) {
    StackTraceElement[] trace = Thread.currentThread().getStackTrace();
    Log.println(priority, tag, "┌────────────────────────────────────────────────────────");
    Log.println(priority, tag, "│ " + msg);
    Log.println(priority, tag, "├┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄");
    Log.println(priority, tag, "│ " + "Thread: " + Thread.currentThread().getName());
    Log.println(priority, tag, "├┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄");

    int stackOffset = getStackOffset(trace, 4, methodOffset, L.class);
    if (methodCount + stackOffset > trace.length) {
      methodCount = trace.length - stackOffset - 1;
    }

    StringBuilder indentLevel = new StringBuilder(" ");
    for (int i = methodCount; i > 0; i--) {
      int stackIndex = i + stackOffset;
      if (stackIndex >= trace.length) {
        continue;
      }
      Log.println(priority, tag, '│' + indentLevel.toString() + format(trace[stackIndex]));
      indentLevel.append("   ");
    }
    Log.println(priority, tag, "└────────────────────────────────────────────────────────");
  }


  /**
   * @return SimpleClassName.MethodName (FileName:LineNumber)
   */
  public static String format(StackTraceElement stackTraceElement) {
    String className = stackTraceElement.getClassName();
    return className.substring(className.lastIndexOf('.') + 1) + '.' +
        stackTraceElement.getMethodName() + ' ' +
        '(' + stackTraceElement.getFileName() + ':' + stackTraceElement.getLineNumber() + ')';
  }

  /**
   * the stack trace
   * <pre>
   *    0 = {StackTraceElement@...} "dalvik.system.VMStack.getThreadStackTrace(NativeMethod)"
   *    1 = {StackTraceElement@...} "java.lang.Thread.getStackTrace(Thread.java:579)"
   *    2 = {StackTraceElement@...} "ignoreClassA..."
   *    3 = {StackTraceElement@...} "ignoreClassB..."
   *    4 = {StackTraceElement@...} "..............."
   *    5 = {StackTraceElement@...} "..............."
   *    6 = {StackTraceElement@...} "targetClass...."
   * </pre>
   * In the above stack trace, want to obtain targetClass stackOffset(5). <ul> <li>Can call
   * getStackOffset(StackTraceElement[], 2 , 2 , ignoreClassA , ignoreClassB)} <li>Can call
   * getStackOffset(StackTraceElement[], 4 , 2 , ignoreClassA/ignoreClassB)} @recommend<ul/>
   *
   * @param trace can call Thread.currentThread().getStackTrace() get (the current method is invoked
   * history)
   * @param minStackOffset general (2 + want ignore the method called level number)
   * @param methodOffset method offset
   * @param ignoreClass ignore class
   * @return the stack offset
   */
  public static int getStackOffset(StackTraceElement[] trace, int minStackOffset, int methodOffset,
      Class... ignoreClass) {
    if (minStackOffset < 2) {
      minStackOffset = 2;
    }
    for (int i = minStackOffset; i < trace.length; i++) {
      StackTraceElement e = trace[i];
      String name = e.getClassName();
      //detection is not ignore class
      boolean isNotIgnoreClass = true;
      for (Class c : ignoreClass) {
        if (name.equals(c.getName())) {
          isNotIgnoreClass = false;
        }
      }
      //get offset (--index)
      if (isNotIgnoreClass) {
        return --i + methodOffset;
      }
    }
    return -1;
  }
}
