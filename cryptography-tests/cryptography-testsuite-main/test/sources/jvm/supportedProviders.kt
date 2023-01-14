package dev.whyoleg.cryptography.testcase.main

import dev.whyoleg.cryptography.jdk.*
import dev.whyoleg.cryptography.provider.*

internal actual val supportedProviders: List<CryptographyProvider> = listOf(
    CryptographyProvider.JDK
)

internal actual val currentPlatform: String get() = "JVM"
