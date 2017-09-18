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

import android.os.Build;
import android.os.Environment;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.util.Properties;

@SuppressWarnings("SpellCheckingInspection")
public class OS {

  // ============================@Singleton@============================
  private OS() {
  }

  private static class SingletonHolder {

    private static final OS INSTANCE = new OS();
  }

  /**
   * @return instance
   */
  public static OS i() {
    return SingletonHolder.INSTANCE;
  }


  // ============================@Properties@============================
  private Properties prop;

  public boolean isPropertiesExist(String... keys) {
    try {
      if (prop == null) {
        prop = new Properties();
        prop
            .load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
      }
      for (String key : keys) {
        String str = prop.getProperty(key);
        if (str != null) {
          return true;
        }
      }
      return false;
    } catch (Exception e) {//IO
      return false;
    }
  }

  // ============================@Is *Os@============================
  public final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
  public final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
  public final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";
  public final String KEY_EMUI_VERSION_CODE = "ro.build.version.emui";


  public boolean isMIUI() {
    return isPropertiesExist(KEY_MIUI_VERSION_CODE, KEY_MIUI_VERSION_NAME,
        KEY_MIUI_INTERNAL_STORAGE) || isMIUI_Model();
  }

  public boolean isMIUI_Model() {
    String model = Build.MODEL;
    return model != null && (model.contains("MI ") || model.contains("HM "));
  }

  public boolean isEMUI() {
    return isPropertiesExist(KEY_EMUI_VERSION_CODE);
  }

  public boolean isFlyme() {
    try {
      final Method method = Build.class.getMethod("hasSmartBar");
      return method != null;
    } catch (Exception e) {
      return false;
    }
  }
}