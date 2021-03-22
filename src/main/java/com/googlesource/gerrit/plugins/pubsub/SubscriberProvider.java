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

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedExecutorProvider;
import com.google.api.gax.rpc.NotFoundException;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings;
import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PushConfig;
import com.google.pubsub.v1.Subscription;
import com.google.pubsub.v1.TopicName;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;

public class SubscriberProvider {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();
  protected CredentialsProvider credentials;
  protected PubSubConfiguration pubSubProperties;
  protected ScheduledExecutorService executor;

  @Inject
  public SubscriberProvider(
      CredentialsProvider credentials,
      PubSubConfiguration pubSubProperties,
      @ConsumerExecutor ScheduledExecutorService executor) {
    this.credentials = credentials;
    this.pubSubProperties = pubSubProperties;
    this.executor = executor;
  }

  public Subscriber get(String topic, MessageReceiver receiver) throws IOException {
    SubscriptionAdminSettings subscriptionAdminSettings =
        SubscriptionAdminSettings.newBuilder().setCredentialsProvider(credentials).build();
    return Subscriber.newBuilder(
            getOrCreateSubscription(topic, subscriptionAdminSettings).getName(), receiver)
        .setExecutorProvider(FixedExecutorProvider.create(executor))
        .setCredentialsProvider(credentials)
        .build();
  }

  protected Subscription getOrCreateSubscription(
      String topicId, SubscriptionAdminSettings subscriptionAdminSettings) throws IOException {
    SubscriptionAdminClient subscriptionAdminClient =
        SubscriptionAdminClient.create(subscriptionAdminSettings);
    String subscriptionName = String.format("%s-%s", pubSubProperties.getSubscriptionId(), topicId);
    ProjectSubscriptionName projectSubscriptionName =
        ProjectSubscriptionName.of(pubSubProperties.getProject(), subscriptionName);

    return getSubscription(subscriptionAdminClient, projectSubscriptionName)
        .orElseGet(
            () ->
                subscriptionAdminClient.createSubscription(
                    projectSubscriptionName,
                    TopicName.of(pubSubProperties.getProject(), topicId),
                    PushConfig.getDefaultInstance(),
                    pubSubProperties.getAckDeadlineSeconds()));
  }

  protected Optional<Subscription> getSubscription(
      SubscriptionAdminClient subscriptionAdminClient,
      ProjectSubscriptionName projectSubscriptionName) {
    try {
      // we should use subscriptionAdminClient.listSubscriptions but for local setup this method
      // throws UNKNOWN_EXCEPTION
      return Optional.of(subscriptionAdminClient.getSubscription(projectSubscriptionName));
    } catch (NotFoundException e) {
      return Optional.empty();
    }
  }
}
