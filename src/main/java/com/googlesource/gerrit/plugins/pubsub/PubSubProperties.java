// Copyright (C) 2021 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.googlesource.gerrit.plugins.pubsub;

import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.server.config.PluginConfig;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.inject.Inject;

public class PubSubProperties {
  private static final String DEFAULT_NUMBER_OF_SUBSCRIBERS = "6";
  private final String groupId;
  private final Integer numberOfSubscribers;
  private Boolean sendAsync;

  @Inject
  public PubSubProperties(PluginConfigFactory configFactory, @PluginName String pluginName) {
    PluginConfig fromGerritConfig = configFactory.getFromGerritConfig(pluginName);
    this.sendAsync = fromGerritConfig.getBoolean("sendAsync", true);
    this.groupId = fromGerritConfig.getString("groupId");
    this.numberOfSubscribers =
        Integer.parseInt(
            fromGerritConfig.getString("numberOfSubscribers", DEFAULT_NUMBER_OF_SUBSCRIBERS));
  }

  public Boolean isSendAsync() {
    return sendAsync;
  }

  public String getGroupId() {
    return groupId;
  }

  public Integer getNumberOfSubscribers() {
    return numberOfSubscribers;
  }
}
