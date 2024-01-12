/*
 * Copyright (c) 2023-2024 Oleg Yukhnevich. Use of this source code is governed by the Apache 2.0 license.
 */

package dev.whyoleg.cryptography.providers.webcrypto.internal

import org.khronos.webgl.*
import kotlin.js.Promise

internal external interface SubtleCrypto {
    fun digest(algorithmName: String, data: ByteArray): Promise<ArrayBuffer>

    fun encrypt(algorithm: Algorithm, key: CryptoKey, data: ByteArray): Promise<ArrayBuffer>
    fun decrypt(algorithm: Algorithm, key: CryptoKey, data: ByteArray): Promise<ArrayBuffer>

    fun sign(algorithm: Algorithm, key: CryptoKey, data: ByteArray): Promise<ArrayBuffer>
    fun verify(algorithm: Algorithm, key: CryptoKey, signature: ByteArray, data: ByteArray): Promise<Boolean>

    fun importKey(
        format: String, /*"raw" | "pkcs8" | "spki"*/
        keyData: Any, /*JSON if jwk, ArrayBuffer otherwise*/
        algorithm: Algorithm,
        extractable: Boolean,
        keyUsages: Array<String>,
    ): Promise<CryptoKey>

    fun exportKey(
        format: String, /*"raw" | "pkcs8" | "spki"*/
        key: CryptoKey,
    ): Promise<Any /*JSON if jwk, ArrayBuffer otherwise*/>

    fun generateKey(
        algorithm: Algorithm,
        extractable: Boolean,
        keyUsages: Array<String>,
    ): Promise<CryptoKey>

    @JsName("generateKey")
    fun generateKeyPair(
        algorithm: Algorithm,
        extractable: Boolean,
        keyUsages: Array<String>,
    ): Promise<CryptoKeyPair>
}

//language=JavaScript
internal fun getSubtleCrypto(): SubtleCrypto {
    return js(
        code = """
    
        var isNodeJs = typeof process !== 'undefined' && process.versions != null && process.versions.node != null
        if (isNodeJs) {
            return (eval('require')('node:crypto').webcrypto).subtle;
        } else {
            return (window ? (window.crypto ? window.crypto : window.msCrypto) : self.crypto).subtle;
        }
    
               """
    ).unsafeCast<SubtleCrypto>()
}
