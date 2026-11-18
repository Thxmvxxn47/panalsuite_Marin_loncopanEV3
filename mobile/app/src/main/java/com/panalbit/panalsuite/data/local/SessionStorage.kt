package com.panalbit.panalsuite.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.panalbit.panalsuite.domain.Session
import com.panalbit.panalsuite.domain.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

interface SessionStorage {
    val session: Flow<Session?>
    suspend fun save(token: String, user: User)
    suspend fun clear()
}

private val Context.sessionStore by preferencesDataStore(name = "session")

class DataStoreSessionStorage(context: Context) : SessionStorage {
    private val store = context.applicationContext.sessionStore
    private val sessionKey = stringPreferencesKey("current_session")
    private val gson = Gson()

    override val session: Flow<Session?> = store.data
        .map { preferences -> preferences[sessionKey]?.let { gson.fromJson(it, Session::class.java) } }
        .catch { error -> if (error is IOException) emit(null) else throw error }

    override suspend fun save(token: String, user: User) {
        store.edit { it[sessionKey] = gson.toJson(Session(token, user)) }
    }

    override suspend fun clear() {
        store.edit { it.remove(sessionKey) }
    }
}
