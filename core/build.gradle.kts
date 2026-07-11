import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.dokka)
    alias(libs.plugins.vanniktech.maven.publish)
}

// ======== Debug: проверяем, что видны переменные окружения ========
val sk = providers.environmentVariable("SIGNING_KEY").orNull
val skId = providers.environmentVariable("SIGNING_KEY_ID").orNull
val skPass = providers.environmentVariable("SIGNING_KEY_PASSWORD").orNull

if (sk != null) {
    logger.lifecycle("[signing] SIGNING_KEY found, length=${sk.length}")
} else {
    logger.lifecycle("[signing] SIGNING_KEY env var is NOT SET")
}

if (skId != null) {
    logger.lifecycle("[signing] SIGNING_KEY_ID found, value=[${skId}]")
} else {
    logger.lifecycle("[signing] SIGNING_KEY_ID env var is NOT SET")
}

if (skPass != null) {
    logger.lifecycle("[signing] SIGNING_KEY_PASSWORD found, length=${skPass.length}")
} else {
    logger.lifecycle("[signing] SIGNING_KEY_PASSWORD env var is NOT SET")
}

// ======== Явная настройка signing для CI ========
if (sk != null && skId != null && skPass != null) {
    pluginManager.withPlugin("signing") {
        extensions.configure<org.gradle.plugins.signing.SigningExtension>("signing") {
            useInMemoryPgpKeys(sk, skId, skPass)
            logger.lifecycle("[signing] Configured in-memory PGP signing: keyId=${skId}")
        }
    }
}

kotlin {
    jvm {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions.jvmTarget.set(JvmTarget.JVM_1_8)
            }
        }
    }
    js {
        browser()
        nodejs()
    }
    linuxX64()
    linuxArm64()
    mingwX64()
    macosX64()
    macosArm64()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    tvosX64()
    tvosArm64()
    tvosSimulatorArm64()
    watchosX64()
    watchosArm32()
    watchosArm64()
    watchosSimulatorArm64()
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        d8()
        nodejs()
    }
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmWasi {
        nodejs()
    }
    targets.all {
        this.compilations.all {
            this.compileTaskProvider.configure {
                this.compilerOptions {
                    freeCompilerArgs.add("-Xcontext-parameters")
                }
            }
        }
    }
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.io.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()
    coordinates(
        groupId = "pw.binom.dns",
        artifactId = "core",
        version = project.version.toString()
    )

    pom {
        name.set("kdns")
        description.set("Kotlin DNS library for Multiplatform")
        url.set("https://github.com/caffeine-mgn/kdns")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("caffeine-mgn")
                name.set("Anton")
                email.set("caffeine.mgn@gmail.com")
            }
        }
        scm {
            connection.set("scm:git:git://github.com/caffeine-mgn/kdns.git")
            developerConnection.set("scm:git:ssh://github.com/caffeine-mgn/kdns.git")
            url.set("https://github.com/caffeine-mgn/kdns")
        }
    }
}
