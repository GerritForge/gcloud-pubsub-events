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

import com.gerritforge.gerrit.eventbroker.EventMessage;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.core.FixedExecutorProvider;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class PubSubEventSubscriber {

  public interface Factory {
    public PubSubEventSubscriber create(String topic, Consumer<EventMessage> messageProcessor);
  }

  private final Gson gson;
  private final String topic;
  private final Consumer<EventMessage> messageProcessor;
  private final PubSubConfiguration pubSubProperties;
  private Subscriber subscriber;
  private CredentialsProvider credentials;
  private ScheduledExecutorService executor;

  @Inject
  public PubSubEventSubscriber(
      PubSubConfiguration pubSubProperties,
      Gson gson,
      CredentialsProvider credentialsProvider,
      @ConsumerExecutor ScheduledExecutorService executor,
      @Assisted String topic,
      @Assisted Consumer<EventMessage> messageProcessor) {
    this.gson = gson;
    this.topic = topic;
    this.messageProcessor = messageProcessor;
    this.pubSubProperties = pubSubProperties;
    this.credentials = credentialsProvider;
    this.executor = executor;
  }

  public void subscribe() {
    ProjectSubscriptionName subscriptionName =
        ProjectSubscriptionName.of(pubSubProperties.getProject(), topic);

    MessageReceiver receiver =
        (PubsubMessage message, AckReplyConsumer consumer) -> {
          EventMessage event = gson.fromJson(message.getData().toStringUtf8(), EventMessage.class);
          messageProcessor.accept(event);
          consumer.ack();
        };
    subscriber =
        Subscriber.newBuilder(subscriptionName, receiver)
            .setExecutorProvider(FixedExecutorProvider.create(executor))
            .setCredentialsProvider(FixedCredentialsProvider.create(credentials.get()))
            .build();

    try {
      // Start the subscriber.
      // TODO: Read timeout from config
      subscriber.startAsync().awaitRunning(60000, TimeUnit.SECONDS);
    } catch (TimeoutException e) {
      // TODO: handle exception
      e.printStackTrace();
    }
  }

  public String getTopic() {
    return topic;
  }

  public Consumer<EventMessage> getMessageProcessor() {
    return messageProcessor;
  }

  public void shutdown() {
    try {
      // TODO: Read timeout from config
      subscriber.stopAsync().awaitTerminated(60000, TimeUnit.SECONDS);
    } catch (TimeoutException e) {
      // TODO: handle exception
      e.printStackTrace();
    }
  }
}
