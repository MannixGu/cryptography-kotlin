package dev.whyoleg.cryptography.algorithms.symmetric.mac

import dev.whyoleg.cryptography.*
import dev.whyoleg.cryptography.algorithms.*
import dev.whyoleg.cryptography.algorithms.digest.*
import dev.whyoleg.cryptography.materials.key.*
import dev.whyoleg.cryptography.operations.*
import dev.whyoleg.cryptography.operations.signature.*
import dev.whyoleg.cryptography.provider.*

//TODO: decide on how we can support CMAC/GMAC

@SubclassOptInRequired(CryptographyProviderApi::class)
public interface HMAC : CryptographyAlgorithm {
    override val id: CryptographyAlgorithmId<HMAC> get() = Companion

    public companion object : CryptographyAlgorithmId<HMAC>("HMAC")

    public fun keyDecoder(digest: CryptographyAlgorithmId<Digest>): KeyDecoder<Key.Format, Key>
    public fun keyGenerator(digest: CryptographyAlgorithmId<Digest> = SHA512): KeyGenerator<Key>

    @SubclassOptInRequired(CryptographyProviderApi::class)
    public interface Key : EncodableKey<Key.Format> {
        public fun signatureGenerator(): SignatureGenerator
        public fun signatureVerifier(): SignatureVerifier

        public sealed class Format : KeyFormat {
            public object RAW : Format(), KeyFormat.RAW
            public object JWK : Format(), KeyFormat.JWK
        }
    }
}
