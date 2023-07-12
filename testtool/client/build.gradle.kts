/*
 * Copyright (c) 2023 Oleg Yukhnevich. Use of this source code is governed by the Apache 2.0 license.
 */

import org.jetbrains.kotlin.gradle.*

plugins {
    //replace `kotlin` with `embeddedKotlin` with gradle 8.3
    kotlin("multiplatform") version "1.8.20"
}

kotlin {
    jvmToolchain(8)

    jvm()
    js(IR) {
        nodejs()
        browser()
    }

    iosArm64()
    iosX64()
    iosSimulatorArm64()

    macosX64()
    macosArm64()

    linuxX64()
    mingwX64()

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    targetHierarchy.default()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.ktor.client.core)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(libs.ktor.client.okhttp)
            }
        }
        val linuxMain by getting {
            dependencies {
                implementation(libs.ktor.client.cio)
            }
        }
        val appleMain by getting {
            dependencies {
                implementation(libs.ktor.client.cio)
            }
        }
        val mingwMain by getting {
            dependencies {
                implementation(libs.ktor.client.winhttp)
            }
        }
    }
}