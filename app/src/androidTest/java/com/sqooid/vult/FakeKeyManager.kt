package com.sqooid.vult

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.security.crypto.MasterKey
import androidx.test.core.app.ApplicationProvider
import com.sqooid.vult.auth.Crypto
import com.sqooid.vult.auth.IKeyManager
import com.sqooid.vult.auth.KeyManagerModule
import dagger.Binds
import dagger.Module
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import java.nio.charset.Charset
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class FakeKeyManager: IKeyManager {
    private var key: SecretKey? = null
    override fun createSyncKey(seed: String, fixedSalt: ByteArray?): String {
        val (salt, innerKey) = Crypto.generateSeededKey(seed, fixedSalt)
        val masterKey = SecretKeySpec(innerKey, "AES")
        key = masterKey
        return Base64.encode(salt, Base64.NO_PADDING or Base64.NO_WRAP).toString(
            Charset.defaultCharset()
        )
    }

    override fun getSyncKey(): SecretKey? {
        return key
    }

    override fun getLocalKey(): MasterKey {
        val spec = KeyGenParameterSpec.Builder(
            "test_master_key",
            KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_ENCRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        return MasterKey.Builder(ApplicationProvider.getApplicationContext(), "test_master_key").setKeyGenParameterSpec(spec).build()
    }
}

@Module
@TestInstallIn(
    components = [ActivityComponent::class],
    replaces = [KeyManagerModule::class]
)
abstract class FakeKeyManagerModule {
    @Binds
    abstract fun bindKeyManager(
        keyManager: FakeKeyManager
    ): IKeyManager
}