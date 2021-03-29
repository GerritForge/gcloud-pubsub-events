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

package com.googlesource.gerrit.plugins.pubsub.local;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.AlreadyExistsException;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminSettings;
import com.google.inject.Inject;
import com.google.pubsub.v1.TopicName;
import com.googlesource.gerrit.plugins.pubsub.PubSubConfiguration;
import com.googlesource.gerrit.plugins.pubsub.PublisherProvider;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.io.IOException;

public class LocalPublisherProvider extends PublisherProvider {
  private LocalHostAndPort localHostAndPort;

  @Inject
  public LocalPublisherProvider(
      CredentialsProvider credentials,
      PubSubConfiguration pubSubProperties,
      LocalHostAndPort localHostAndPort) {
    super(credentials, pubSubProperties);
    this.localHostAndPort = localHostAndPort;
  }

  @Override
  public Publisher get(String topic) throws IOException {
    ManagedChannel channel =
        ManagedChannelBuilder.forTarget(localHostAndPort.getLocalHostAndPort().get())
            .usePlaintext()
            .build();
    TransportChannelProvider channelProvider =
        FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));
    createTopic(channel, config.getProject(), topic);
    return Publisher.newBuilder(TopicName.of(config.getProject(), topic))
        .setChannelProvider(channelProvider)
        .setCredentialsProvider(credentials)
        .build();
  }

  private static void createTopic(ManagedChannel channel, String project, String topicId)
      throws IOException {

    TopicAdminSettings topicAdminSettings =
        TopicAdminSettings.newBuilder()
            .setTransportChannelProvider(
                FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel)))
            .setCredentialsProvider(NoCredentialsProvider.create())
            .build();
    try (TopicAdminClient topicAdminClient = TopicAdminClient.create(topicAdminSettings)) {
      TopicName topicName = TopicName.of(project, topicId);
      topicAdminClient.createTopic(topicName);
    } catch (AlreadyExistsException e) {
      // topic already exists do nothing
    }
  }
}
