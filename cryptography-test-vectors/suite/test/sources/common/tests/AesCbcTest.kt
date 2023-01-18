package dev.whyoleg.cryptography.test.vectors.suite.tests

import dev.whyoleg.cryptography.algorithms.symmetric.*
import dev.whyoleg.cryptography.provider.*
import dev.whyoleg.cryptography.random.*
import dev.whyoleg.cryptography.test.support.*
import dev.whyoleg.cryptography.test.vectors.suite.*
import dev.whyoleg.cryptography.test.vectors.suite.api.*
import kotlinx.serialization.*

private const val keyIterations = 10
private const val cipherIterations = 10
private const val maxPlaintextSize = 10000
private const val blockSize = 16 //for no padding

// WebCrypto doesn't support 192bits - TODO: WHY???
private fun CryptographyProvider.supportsKeySize(keySize: SymmetricKeySize): Boolean = skipUnsupported(
    feature = "192bit key",
    supports = keySize != SymmetricKeySize.B192 || !isWebCrypto
)

private fun CryptographyProvider.supportsPadding(padding: Boolean): Boolean = skipUnsupported(
    feature = "NoPadding",
    supports = padding || !isWebCrypto // WebCrypto does not support NoPadding
)

private fun Int.withPadding(padding: Boolean): Int = if (padding) this else this + blockSize - this % blockSize

@Serializable
private data class KeyParameters(
    val keySize: @Contextual SymmetricKeySize,
) : TestVectorParameters

@Serializable
private data class CipherParameters(
    val padding: Boolean,
) : TestVectorParameters

class AesCbcTest : TestVectorTest<AES.CBC>(AES.CBC) {
    override suspend fun TestLoggingContext.generate(api: TestVectorApi, provider: CryptographyProvider, algorithm: AES.CBC) {
        generateSymmetricKeySize { keySize ->
            if (!provider.supportsKeySize(keySize)) return@generateSymmetricKeySize

            val keyParametersId = api.keys.saveParameters(KeyParameters(keySize))
            algorithm.keyGenerator(keySize).generateKeys(keyIterations) { key ->
                val keyReference = api.keys.saveData(keyParametersId, KeyData {
                    put(StringKeyFormat.RAW, key.encodeTo(AES.Key.Format.RAW))
                    if (provider.supportsJwk) put(StringKeyFormat.JWK, key.encodeTo(AES.Key.Format.JWK))
                })
                generateBoolean { padding ->
                    if (!provider.supportsPadding(padding)) return@generateBoolean

                    val cipherParametersId = api.ciphers.saveParameters(CipherParameters(padding))
                    val cipher = key.cipher(padding)
                    repeat(cipherIterations) {
                        val plaintextSize = CryptographyRandom.nextInt(maxPlaintextSize).withPadding(padding)
                        log("plaintext.size  = $plaintextSize")
                        val plaintext = CryptographyRandom.nextBytes(plaintextSize)
                        val ciphertext = cipher.encrypt(plaintext)
                        log("ciphertext.size = ${ciphertext.size}")

                        //only simple check here to fail fast
                        cipher.decrypt(ciphertext).assertContentEquals(plaintext)

                        api.ciphers.saveData(cipherParametersId, CipherData(keyReference, plaintext, ciphertext))
                    }
                }
            }
        }
    }

    override suspend fun TestLoggingContext.validate(api: TestVectorApi, provider: CryptographyProvider, algorithm: AES.CBC) {
        val keyDecoder = algorithm.keyDecoder()

        val keys = buildMap {
            api.keys.getParameters<KeyParameters> { (keySize), parametersId ->
                if (!provider.supportsKeySize(keySize)) return@getParameters

                api.keys.getData<KeyData>(parametersId) { (formats), keyReference ->
                    val keys = formats.mapNotNull { (stringFormat, data) ->
                        keyDecoder.decodeFrom(
                            format = when (stringFormat) {
                                StringKeyFormat.RAW -> AES.Key.Format.RAW
                                StringKeyFormat.JWK -> AES.Key.Format.JWK.takeIf { provider.supportsJwk }
                                else                -> error("Unsupported key format: $stringFormat") //TODO
                            },
                            input = data
                        )
                    }
                    keys.forEach { key ->
                        formats[StringKeyFormat.RAW]?.let { bytes ->
                            key.encodeTo(AES.Key.Format.RAW).assertContentEquals(bytes)
                        }
                    }
                    put(keyReference, keys)
                }
            }
        }

        api.ciphers.getParameters<CipherParameters> { (padding), parametersId ->
            if (!provider.supportsPadding(padding)) return@getParameters

            api.ciphers.getData<CipherData>(parametersId) { (keyReference, plaintext, ciphertext), _ ->
                keys.getValue(keyReference).forEach { key ->
                    val cipher = key.cipher(padding)
                    cipher.decrypt(ciphertext).assertContentEquals(plaintext)
                    cipher.decrypt(cipher.encrypt(plaintext)).assertContentEquals(plaintext)
                }
            }
        }
    }
}