Configuration
=========================

The gcloud-pubsub-events plugin is configured by adding a plugin stanza in the
`gerrit.config` file, for example:

```text
[plugin "gcloud-pubsub-events"]
    numberOfSubscribers = 6
    subscriptionId = instance-1
    gcloudProject = test_project
    privateKeyLocation = /var/gerrit/etc/secured_key.json

```

`plugin.gcloud-pubsub-events.numberOfSubscribers`
:   The number of expected gcloud-pubsub-events subscribers. This will be used
    to allocate a thread pool able to run all subscribers.
    Default: 6
    
`plugin.gcloud-pubsub-events.gcloudProject`
:   GCloud [project name](https://cloud.google.com/docs/overview#projects)

`plugin.gcloud-pubsub-events.subscriptionId`
:   This value identifies the subscriber and it must be unique within your
    gerrit cluster to allow different gerrit nodes to consume data from the
    stream independently.
    Default: Gerrit instance-id

`plugin.gcloud-pubsub-events.privateKeyLocation`
:   Path to the JSON file that contains service account key. The file
    should be readable only by the daemon process because it contains information
    that wouldn’t normally be exposed to everyone.

`plugin.gcloud-pubsub-events.sendAsync`
:   Send messages to GCloud PubSub asynchronously, detaching the calling
    process from the acknowledge of the message being sent.
    Default: true

`plugin.gcloud-pubsub-events.ackDeadlineSeconds`
:   The approximate amount of time (on a best-effort basis) Pub/Sub waits for
    the subscriber to acknowledge receipt before resending the message.
    Default: 10

`plugin.gcloud-pubsub-events.subscribtionTimeoutInSeconds`
:   Maximum time to wait for the subscriber to connect to GCloud PubSub topic.
    Default: 10

`plugin.gcloud-pubsub-events.shutdownTimeout`
:   Maximum time to wait for the subscriber to disconnect from GCloud PubSub topic.
    Default: 10
