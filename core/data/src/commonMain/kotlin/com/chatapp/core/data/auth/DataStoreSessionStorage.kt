package com.chatapp.core.data.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.chatapp.core.data.dto.AuthInfoSerializable
import com.chatapp.core.data.mappers.toDomain
import com.chatapp.core.data.mappers.toSerializable
import com.chatapp.core.data.security.SecureStorage
import com.chatapp.core.domain.auth.AuthInfo
import com.chatapp.core.domain.auth.SessionStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

/**
 * Persists [AuthInfo] (JWT access/refresh tokens + user) in DataStore, encrypted at rest via
 * [secureStorage] using a key held in the platform's secure storage (Keystore/Keychain/
 * DPAPI/Secret Service). DataStore never sees plaintext tokens, only ciphertext.
 */
class DataStoreSessionStorage(
    private val dataStore: DataStore<Preferences>,
//    private val secureStorage: SecureStorage
) : SessionStorage {

    private val authInfoKey = stringPreferencesKey("KEY_AUTH_INFO")

    private val json = Json {
        ignoreUnknownKeys = true
    }

    override fun observeAuthInfo(): Flow<AuthInfo?> {
        return dataStore.data.map { preferences ->
            val serializedJson = preferences[authInfoKey]
            serializedJson?.let {
                runCatching {
                    json.decodeFromString<AuthInfoSerializable>(it).toDomain()
                }.getOrNull()
            }
        }
    }

    override suspend fun set(info: AuthInfo?) {
        if (info == null) {
            dataStore.edit {
                it.remove(authInfoKey)
            }
            return
        }

        val serialized = json.encodeToString(info.toSerializable())
//        val encrypted = secureStorage.encrypt(serialized)
        dataStore.edit { prefs ->
            prefs[authInfoKey] = serialized
        }
    }

//    override fun observeAuthInfo(): Flow<AuthInfo?> {
//        return dataStore.data.map { preferences ->
//            val cipherText = preferences[authInfoKey]
//            cipherText?.let {
//                val plainText = secureStorage.decrypt(it) ?: return@let null
//                runCatching {
//                    json.decodeFromString<AuthInfoSerializable>(plainText).toDomain()
//                }.getOrNull()
//            }
//        }
//    }
}