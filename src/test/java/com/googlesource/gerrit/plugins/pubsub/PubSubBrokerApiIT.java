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

import static com.google.common.truth.Truth.assertThat;

import com.gerritforge.gerrit.eventbroker.BrokerApi;
import com.gerritforge.gerrit.eventbroker.EventGsonProvider;
import com.gerritforge.gerrit.eventbroker.EventMessage;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminSettings;
import com.google.cloud.pubsub.v1.stub.GrpcSubscriberStub;
import com.google.cloud.pubsub.v1.stub.SubscriberStub;
import com.google.cloud.pubsub.v1.stub.SubscriberStubSettings;
import com.google.gerrit.acceptance.LightweightPluginDaemonTest;
import com.google.gerrit.acceptance.NoHttpd;
import com.google.gerrit.acceptance.TestPlugin;
import com.google.gerrit.extensions.config.FactoryModule;
import com.google.gerrit.extensions.events.LifecycleListener;
import com.google.gerrit.extensions.registration.DynamicSet;
import com.google.gerrit.server.events.Event;
import com.google.gerrit.server.events.ProjectCreatedEvent;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PullRequest;
import com.google.pubsub.v1.PullResponse;
import com.google.pubsub.v1.PushConfig;
import com.google.pubsub.v1.TopicName;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.io.IOException;
import java.util.UUID;
import java.util.function.Consumer;
import org.junit.Test;
import org.testcontainers.containers.PubSubEmulatorContainer;
import org.testcontainers.utility.DockerImageName;

@NoHttpd
@TestPlugin(
    name = "pubsub-events",
    sysModule = "com.googlesource.gerrit.plugins.pubsub.PubSubBrokerApiIT$TestModule")
public class PubSubBrokerApiIT extends LightweightPluginDaemonTest {
  private static final String PROJECT_ID = "test_project";
  private static final String TOPIC_ID = "test_topic";
  private static final String SUBSCRIPTION_ID = "test_subscription_id";

  @Inject private Gson gson;

  private static String hostport;

  private TransportChannelProvider channelProvider;
  private NoCredentialsProvider credentialsProvider;
  private ManagedChannel channel;

  private BrokerApi objectUnderTest;

  public PubSubEmulatorContainer emulator =
      new PubSubEmulatorContainer(
          DockerImageName.parse("gcr.io/google.com/cloudsdktool/cloud-sdk:316.0.0-emulators"));

  @Override
  public void setUpTestPlugin() throws Exception {
    emulator.start();
    hostport = emulator.getEmulatorEndpoint();

    channel = ManagedChannelBuilder.forTarget(hostport).usePlaintext().build();
    channelProvider = FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));
    credentialsProvider = NoCredentialsProvider.create();

    createTopic(TOPIC_ID, channelProvider, credentialsProvider);

    createSubscription(SUBSCRIPTION_ID, TOPIC_ID, channelProvider, credentialsProvider);
    super.setUpTestPlugin();

    objectUnderTest = plugin.getSysInjector().getInstance(BrokerApi.class);
  }

  @Override
  public void tearDownTestPlugin() {
    channel.shutdown();
    emulator.close();
    super.tearDownTestPlugin();
  }

  @Test
  public void shouldSendEvent() throws IOException {
    UUID id = UUID.randomUUID();
    Event event = new ProjectCreatedEvent();
    EventMessage eventMessage = new EventMessage(new EventMessage.Header(id, id), event);
    String expectedMessageJson = gson.toJson(eventMessage);

    objectUnderTest.send(TOPIC_ID, eventMessage);

    readMessageAndValidate(
        (pullResponse) -> {
          assertThat(pullResponse.getReceivedMessagesList()).hasSize(1);
          assertThat(pullResponse.getReceivedMessages(0).getMessage().getData().toStringUtf8())
              .isEqualTo(expectedMessageJson);
        });
  }

  private void readMessageAndValidate(Consumer<PullResponse> validate) throws IOException {
    SubscriberStubSettings subscriberStubSettings =
        SubscriberStubSettings.newBuilder()
            .setTransportChannelProvider(channelProvider)
            .setCredentialsProvider(credentialsProvider)
            .build();
    try (SubscriberStub subscriber = GrpcSubscriberStub.create(subscriberStubSettings)) {
      PullRequest pullRequest =
          PullRequest.newBuilder()
              .setMaxMessages(1)
              .setSubscription(ProjectSubscriptionName.format(PROJECT_ID, SUBSCRIPTION_ID))
              .build();
      PullResponse pullResponse = subscriber.pullCallable().call(pullRequest);

      validate.accept(pullResponse);
    }
  }

  private void createTopic(
      String topicId,
      TransportChannelProvider channelProvider,
      NoCredentialsProvider credentialsProvider)
      throws IOException {
    TopicAdminSettings topicAdminSettings =
        TopicAdminSettings.newBuilder()
            .setTransportChannelProvider(channelProvider)
            .setCredentialsProvider(credentialsProvider)
            .build();
    try (TopicAdminClient topicAdminClient = TopicAdminClient.create(topicAdminSettings)) {
      TopicName topicName = TopicName.of(PROJECT_ID, topicId);
      topicAdminClient.createTopic(topicName);
    }
  }

  private void createSubscription(
      String subscriptionId,
      String topicId,
      TransportChannelProvider channelProvider,
      NoCredentialsProvider credentialsProvider)
      throws IOException {
    SubscriptionAdminSettings subscriptionAdminSettings =
        SubscriptionAdminSettings.newBuilder()
            .setTransportChannelProvider(channelProvider)
            .setCredentialsProvider(credentialsProvider)
            .build();
    SubscriptionAdminClient subscriptionAdminClient =
        SubscriptionAdminClient.create(subscriptionAdminSettings);
    ProjectSubscriptionName subscriptionName =
        ProjectSubscriptionName.of(PROJECT_ID, subscriptionId);
    subscriptionAdminClient.createSubscription(
        subscriptionName, TopicName.of(PROJECT_ID, topicId), PushConfig.getDefaultInstance(), 10);
  }

  @SuppressWarnings("unused")
  private static class TestModule extends FactoryModule {
    PubSubApiModule pubSubApiModule;

    @Inject
    public TestModule(PubSubApiModule pubSubApiModule) {
      this.pubSubApiModule = pubSubApiModule;
    }

    @Override
    protected void configure() {
      bind(Gson.class).toProvider(EventGsonProvider.class).in(Singleton.class);
      DynamicSet.bind(binder(), LifecycleListener.class).to(Manager.class);
      factory(PubSubPublisher.Factory.class);
      factory(PubSubEventSubscriber.Factory.class);
      bind(PublisherProvider.class).to(TestPublisherProvider.class);

      install(pubSubApiModule);
    }
  }

  private static class TestPublisherProvider extends PublisherProvider {

    @Inject
    public TestPublisherProvider() {
      super(null);
    }

    @Override
    public Publisher get(String topic) throws IOException {
      ManagedChannel channel = ManagedChannelBuilder.forTarget(hostport).usePlaintext().build();
      TransportChannelProvider channelProvider =
          FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));
      return Publisher.newBuilder(TopicName.of(PROJECT_ID, topic))
          .setChannelProvider(channelProvider)
          .setCredentialsProvider(NoCredentialsProvider.create())
          .build();
    }
  }
}
