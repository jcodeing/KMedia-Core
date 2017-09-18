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
package com.jcodeing.kmedia;

import android.content.Context;
import com.jcodeing.kmedia.service.PlayerService;

/**
 * Simple Player Binding
 */
public final class PlayerBinding extends APlayerBinding<PlayerBinding> {

  public <PService extends PlayerService> PlayerBinding(Context context,
      Class<PService> playerServiceClass, BindPlayer bindPlayer) {
    super(context, playerServiceClass, bindPlayer);
  }

  public <PService extends PlayerService> PlayerBinding(Context context,
      Class<PService> playerServiceClass, BindPlayer bindPlayer,
      BindingListener serviceConnected) {
    super(context, playerServiceClass, bindPlayer, serviceConnected);
  }

  @Override
  protected PlayerBinding returnThis() {
    return this;
  }
}