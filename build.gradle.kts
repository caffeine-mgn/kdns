plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlinx.serialization) apply false
}
allprojects {
    if (version == "unspecified") {
        version = "1.0.0-SNAPSHOT"
    }
    group = "pw.binom.dns"

    repositories {
        mavenLocal()
//        maven(url = "https://repo.binom.pw")
        google()
        mavenCentral()
    }
}
