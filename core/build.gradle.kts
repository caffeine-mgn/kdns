import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.dokka)
    alias(libs.plugins.vanniktech.maven.publish)
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

pluginManager.withPlugin("signing") {
    val key = providers.gradleProperty("signingInMemoryKey").orNull
    val keyId = providers.gradleProperty("signingInMemoryKeyId").orNull
    val password = providers.gradleProperty("signingInMemoryKeyPassword").orNull
    if (key != null && keyId != null && password != null) {
        val normalizedKey = key
            .replace("\\n", "\n")  // resolve double-escaped \\n (rare)
            .replace("\\n", "\n")     // resolve single-escaped \n -> real newline (.properties / GH secrets)
        logger.lifecycle("[signing] Normalized key via build.gradle.kts, length=${normalizedKey.length}")
        extensions.getByType(org.gradle.plugins.signing.SigningExtension::class.java)
            .useInMemoryPgpKeys(normalizedKey, keyId, password)
    }
}
