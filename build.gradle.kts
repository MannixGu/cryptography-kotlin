/*
 * Copyright (c) 2023 Oleg Yukhnevich. Use of this source code is governed by the Apache 2.0 license.
 */

import org.jetbrains.kotlin.gradle.targets.js.yarn.*

plugins {
    id("build-parameters")

    alias(kotlinLibs.plugins.multiplatform) apply false
    alias(kotlinLibs.plugins.serialization) apply false
}

plugins.withType<YarnPlugin> {
    yarn.apply {
        lockFileDirectory = rootDir.resolve("gradle/js")
        yarnLockMismatchReport = YarnLockMismatchReport.WARNING
        resolution("ua-parser-js", "0.7.33")
    }
}

val skipTest = buildParameters.skip.test
val skipLink = buildParameters.skip.link
val kotlinVersionOverriden = buildParameters.kotlin.override.version.isPresent

subprojects {
    if (skipTest) tasks.matching { it.name.endsWith("test", ignoreCase = true) }.configureEach { onlyIf { false } }
    if (skipLink) tasks.matching { it.name.startsWith("link", ignoreCase = true) }.configureEach { onlyIf { false } }
}
