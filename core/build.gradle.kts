import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("maven-publish")
    id("signing")
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
    macosX64()
    macosArm64()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    tvosX64()
    tvosArm64()
    tvosSimulatorArm64()
    wasmJs {
        browser()
        d8()
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
//            api(libs.kotlinx.coroutines.core)
            api(libs.ktor.io)
//            api(libs.ktor.server.cio)
        }
    }
}

signing {
    useInMemoryPgpKeys(
        providers.gradleProperty("binom.gpg.key_id").orNull ?: System.getenv("GPG_KEY_ID"),
        providers.gradleProperty("binom.gpg.private_key").orNull ?: System.getenv("GPG_PASSWORD"),
        providers.gradleProperty("binom.gpg.password").orNull ?: System.getenv("GPG_PRIVATE_KEY")
    )
    sign(publishing.publications)
}



publishing {
    publications.withType<MavenPublication> {
//        groupId = "pw.binom.dns"
//        artifactId = "protocol"
//        version = project.version.toString()

        pom {
            name.set("kdns")
            description.set("Kotlin DNS library for Multiplatform")
            url.set("https://github.com/caffeine-mgn/kdns")

            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
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

    repositories {
        mavenLocal()
        if (project.version.toString().endsWith("-SNAPSHOT")) {
            maven {
                name = "sonatype"
                url = uri("https://central.sonatype.com/repository/maven-snapshots/")

                credentials {
                    username = providers.gradleProperty("mavenCentralPortalUsername").orNull
                        ?: System.getenv("SONATYPE_USERNAME")
                    password = providers.gradleProperty("mavenCentralPortalPassword").orNull
                        ?: System.getenv("SONATYPE_PASSWORD")
                }
            }
        }
    }
}

afterEvaluate {
    publishing.publications.all {
        println("Publication: $name")
    }
}