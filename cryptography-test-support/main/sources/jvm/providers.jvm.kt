package dev.whyoleg.cryptography.test.support

import dev.whyoleg.cryptography.jdk.*
import dev.whyoleg.cryptography.provider.*

actual val availableProviders: List<CryptographyProvider> = listOf(
    CryptographyProvider.JDK
)