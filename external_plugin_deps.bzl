load("//tools/bzl:maven_jar.bzl", "maven_jar")

def external_plugin_deps():
    maven_jar(
        name = "junit-platform",
        artifact = "org.junit.platform:junit-platform-commons:1.4.0",
        sha1 = "34d9983705c953b97abb01e1cd04647f47272fe5",
    )

    maven_jar(
        name = "google-cloud-pubsub",
        artifact = "com.google.cloud:google-cloud-pubsub:1.111.4",
        sha1 = "01988db8241471b09fc317c803d20403d93d6ca5",
    )
    
    maven_jar(
        name = "google-cloud-pubsub-proto",
        artifact = "com.google.api.grpc:proto-google-cloud-pubsub-v1:1.93.4",
        sha1 = "167bfae34ec63215ee3b9e95a4deb0b67104c021",
    )
    
    maven_jar(
        name = "api-common",
        artifact = "com.google.api:api-common:1.10.1",
        sha1 = "d157681b5909cf959a9fa60ced9bed9da741ffef",
    )
    
    maven_jar(
        name = "google-auth-library-credentials",
        artifact = "com.google.auth:google-auth-library-credentials:0.24.1",
        sha1 = "5f43498fae558213e27cd904944626c88cf03d03",
    )

    maven_jar(
        name = "events-broker",
        artifact = "com.gerritforge:events-broker:3.4-alpha-20210205083200",
        sha1 = "3fec2bfee13b9b0a2889616e3c039ead686b931f",
    )
