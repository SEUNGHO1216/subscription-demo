plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

rootProject.name = "v2-subscription"
include("subscription-core")
include("subscription-infrastructure")
include("subscription-app")
