/*
 * Copyright (c) 2023 Oleg Yukhnevich. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("buildx-multiplatform-test")
    id("buildx-target-all")
    id("buildx-target-android")

    id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.cryptographyCore)
                api(libs.kotlinx.coroutines.test)
                api(libs.kotlinx.serialization.json)
            }
        }
        jsMain {
            dependencies {
                implementation(projects.cryptographyProviderWebcrypto)
            }
        }
        jvmMain {
            dependencies {
                implementation(projects.cryptographyProviderJdk)
                implementation(libs.bouncycastle.jdk8)
            }
        }
        androidMain {
            dependencies {
                implementation(projects.cryptographyProviderJdk)
                implementation(libs.bouncycastle.jdk8)
            }
        }

        appleMain {
            dependencies {
                implementation(projects.cryptographyProviderApple)
                implementation(projects.cryptographyProviderOpenssl3Prebuilt)
            }
        }

        linuxMain {
            dependencies {
                implementation(projects.cryptographyProviderOpenssl3Prebuilt)
            }
        }

        mingwMain {
            dependencies {
                implementation(projects.cryptographyProviderOpenssl3Prebuilt)
            }
        }
    }
}
