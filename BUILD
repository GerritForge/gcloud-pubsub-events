load("//tools/bzl:junit.bzl", "junit_tests")
load(
    "//tools/bzl:plugin.bzl",
    "PLUGIN_DEPS",
    "PLUGIN_TEST_DEPS",
    "gerrit_plugin",
)

gerrit_plugin(
    name = "gcloud-pubsub-events",
    srcs = glob(["src/main/java/**/*.java"]),
    manifest_entries = [
        "Gerrit-PluginName: gcloud-pubsub-events",
        "Gerrit-Module: com.googlesource.gerrit.plugins.pubsub.Module",
        "Implementation-Title: Gerrit Apache Gcloud pubsub plugin",
        "Implementation-URL: https://GerritForge/gcloud-pubsub-events",
    ],
    resources = glob(["src/main/resources/**/*"]),
    deps = [
        "@google-cloud-pubsub//jar",
        "@google-cloud-pubsub-proto//jar",
        "@api-common//jar",
        "@google-auth-library-credentials//jar",
        "@google-auth-library-oauth2-http//jar",
        "@gax//jar",
        "@events-broker//jar",
        "@grpc-api//jar",
        "@gax-grpc//jar",
        "@grpc-netty-shaded//jar",
        "@grpc-core//jar",
        "@threetenbp//jar",
        "@grpc-alts//jar",
        "@grpc-protobuf//jar",
        "@grpc-protobuf-lite//jar",
        "@proto-google-iam-v1//jar",
        "@proto-google-common-protos//jar",
        "@google-http-client//jar",
        "@grpc-context//jar",
        "@grpc-stub//jar",
        "@grpc-auth//jar",
        "@perfmark-api//jar",
        "@google-http-client-gson//jar",
        "@opencensus-api//jar",
        "@opencensus-contrib-http-util//jar",
    ],
)

junit_tests(
    name = "gcloud-pubsub-events_tests",
    srcs = glob(["src/test/java/**/*.java"]),
    tags = ["gcloud-pubsub-events"],
    deps = [
        ":gcloud-pubsub-events__plugin_test_deps",
        "//lib/testcontainers",
        "@testcontainers-gcloud//jar",
        "@google-cloud-pubsub//jar",
        "@google-cloud-pubsub-proto//jar",
        "@api-common//jar",
        "@google-auth-library-credentials//jar",
        "@google-auth-library-oauth2-http//jar",
        "@gax//jar",
        "@events-broker//jar",
        "@grpc-api//jar",
        "@gax-grpc//jar",
        "@grpc-netty-shaded//jar",
        "@grpc-core//jar",
        "@threetenbp//jar",
        "@grpc-alts//jar",
        "@grpc-protobuf//jar",
        "@grpc-protobuf-lite//jar",
        "@proto-google-iam-v1//jar",
        "@proto-google-common-protos//jar",
        "@google-http-client//jar",
        "@grpc-context//jar",
        "@grpc-stub//jar",
        "@perfmark-api//jar",
        "@google-http-client-gson//jar",
        "@opencensus-api//jar",
        "@opencensus-contrib-http-util//jar",
    ],
)

java_library(
    name = "gcloud-pubsub-events__plugin_test_deps",
    testonly = 1,
    visibility = ["//visibility:public"],
    exports = PLUGIN_DEPS + PLUGIN_TEST_DEPS + [
        ":gcloud-pubsub-events__plugin",
        "//lib/jackson:jackson-annotations",
        "//lib/testcontainers",
        "//lib/testcontainers:docker-java-api",
        "//lib/testcontainers:docker-java-transport",
        "@testcontainers-gcloud//jar",
        "@grpc-api//jar",
        "@gax-grpc//jar",
        "@grpc-netty-shaded//jar",
        "@grpc-core//jar",
        "@threetenbp//jar",
        "@grpc-alts//jar",
        "@grpc-protobuf//jar",
        "@grpc-protobuf-lite//jar",
        "@proto-google-iam-v1//jar",
        "@proto-google-common-protos//jar",
        "@google-http-client//jar",
        "@grpc-context//jar",
        "@grpc-stub//jar",
        "@perfmark-api//jar",
        "@google-http-client-gson//jar",
        "@opencensus-api//jar",
        "@opencensus-contrib-http-util//jar",
    ],
)
