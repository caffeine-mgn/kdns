plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlinx.serialization) apply false
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
}
allprojects {
//    if (version == "unspecified") {
//        version = "1.0.0-SNAPSHOT"
//    }
    version = "1.0.0-SNAPSHOT"
//    version = "0.0.1"
    group = "pw.binom.dns"
}
