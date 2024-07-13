/*
 * Copyright (c) 2023-2024 Oleg Yukhnevich. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.whyoleg.cryptography.providers.webcrypto.algorithms

import dev.whyoleg.cryptography.*
import dev.whyoleg.cryptography.algorithms.asymmetric.*
import dev.whyoleg.cryptography.algorithms.digest.*
import dev.whyoleg.cryptography.bigint.*
import dev.whyoleg.cryptography.operations.signature.*
import dev.whyoleg.cryptography.providers.webcrypto.internal.*
import dev.whyoleg.cryptography.providers.webcrypto.materials.*
import dev.whyoleg.cryptography.providers.webcrypto.operations.*
import dev.whyoleg.cryptography.serialization.asn1.*
import dev.whyoleg.cryptography.serialization.asn1.modules.*

internal object WebCryptoEcdsa : WebCryptoEc<ECDSA.PublicKey, ECDSA.PrivateKey, ECDSA.KeyPair>(
    algorithmName = "ECDSA",
    publicKeyWrapper = WebCryptoKeyWrapper(arrayOf("verify"), ::EcdsaPublicKey),
    privateKeyWrapper = WebCryptoKeyWrapper(arrayOf("sign"), ::EcdsaPrivateKey),
    keyPairWrapper = ::EcdsaKeyPair
), ECDSA {
    private class EcdsaKeyPair(
        override val publicKey: ECDSA.PublicKey,
        override val privateKey: ECDSA.PrivateKey,
    ) : ECDSA.KeyPair

    private class EcdsaPublicKey(publicKey: CryptoKey) : EcPublicKey(publicKey), ECDSA.PublicKey {
        override fun signatureVerifier(digest: CryptographyAlgorithmId<Digest>, format: ECDSA.SignatureFormat): SignatureVerifier {
            val verifier = WebCryptoSignatureVerifier(EcdsaSignatureAlgorithm(digest.hashAlgorithmName()), publicKey)
            return when (format) {
                ECDSA.SignatureFormat.RAW -> verifier
                ECDSA.SignatureFormat.DER -> EcdsaDerSignatureVerifier(
                    verifier,
                    curveOrderSize(publicKey.algorithm.ecKeyAlgorithmNamedCurve)
                )
            }
        }
    }

    private class EcdsaPrivateKey(privateKey: CryptoKey) : EcPrivateKey(privateKey), ECDSA.PrivateKey {
        override fun signatureGenerator(digest: CryptographyAlgorithmId<Digest>, format: ECDSA.SignatureFormat): SignatureGenerator {
            val generator = WebCryptoSignatureGenerator(EcdsaSignatureAlgorithm(digest.hashAlgorithmName()), privateKey)
            return when (format) {
                ECDSA.SignatureFormat.RAW -> generator
                ECDSA.SignatureFormat.DER -> EcdsaDerSignatureGenerator(generator)
            }
        }
    }
}

private class EcdsaDerSignatureGenerator(
    private val rawGenerator: SignatureGenerator,
) : SignatureGenerator {
    override suspend fun generateSignature(dataInput: ByteArray): ByteArray {
        val rawSignature = rawGenerator.generateSignature(dataInput)

        val r = rawSignature.copyOfRange(0, rawSignature.size / 2).makePositive()
        val s = rawSignature.copyOfRange(rawSignature.size / 2, rawSignature.size).makePositive()

        val signature = EcdsaSignatureValue(
            r = r.decodeToBigInt(),
            s = s.decodeToBigInt()
        )

        return DER.encodeToByteArray(EcdsaSignatureValue.serializer(), signature)
    }

    override fun generateSignatureBlocking(dataInput: ByteArray): ByteArray = nonBlocking()
}

private class EcdsaDerSignatureVerifier(
    private val rawVerifier: SignatureVerifier,
    private val curveOrderSize: Int,
) : SignatureVerifier {
    override suspend fun verifySignature(dataInput: ByteArray, signatureInput: ByteArray): Boolean {
        val signature = DER.decodeFromByteArray(EcdsaSignatureValue.serializer(), signatureInput)

        val r = signature.r.encodeToByteArray().trimLeadingZeros()
        val s = signature.s.encodeToByteArray().trimLeadingZeros()

        val rawSignature = ByteArray(curveOrderSize * 2)

        r.copyInto(rawSignature, curveOrderSize - r.size)
        s.copyInto(rawSignature, curveOrderSize * 2 - s.size)

        return rawVerifier.verifySignature(dataInput, rawSignature)
    }

    override fun verifySignatureBlocking(dataInput: ByteArray, signatureInput: ByteArray): Boolean = nonBlocking()
}

private fun ByteArray.makePositive(): ByteArray = if (this[0] < 0) byteArrayOf(0, *this) else this
private fun ByteArray.trimLeadingZeros(): ByteArray {
    val firstNonZeroIndex = indexOfFirst { it != 0.toByte() }
    if (firstNonZeroIndex == -1) return this
    return copyOfRange(firstNonZeroIndex, size)
}

internal fun curveOrderSize(namedCurve: String): Int = when (namedCurve) {
    EC.Curve.P256.name -> 32
    EC.Curve.P384.name -> 48
    EC.Curve.P521.name -> 66
    else               -> error("Unknown curve: $namedCurve")
}
