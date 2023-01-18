package dev.whyoleg.cryptography.jdk.materials

import dev.whyoleg.cryptography.io.*
import dev.whyoleg.cryptography.jdk.*
import dev.whyoleg.cryptography.materials.key.*
import java.security.spec.*

internal abstract class JdkPrivateKeyDecoder<KF : KeyFormat, K : Key>(
    protected val state: JdkCryptographyState,
    algorithm: String,
) : KeyDecoder<KF, K> {
    private val keyFactory = state.keyFactory(algorithm)

    protected abstract fun JPrivateKey.convert(): K

    final override fun decodeFromBlocking(format: KF, input: Buffer): K = when (format) {
        is KeyFormat.DER -> keyFactory.use { it.generatePrivate(PKCS8EncodedKeySpec(input)) }.convert()
        is KeyFormat.PEM -> TODO("fix it")
        else             -> TODO()
    }
}
