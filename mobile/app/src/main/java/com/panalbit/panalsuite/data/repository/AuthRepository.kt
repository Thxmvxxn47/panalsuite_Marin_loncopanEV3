package com.panalbit.panalsuite.data.repository

import com.panalbit.panalsuite.data.local.SessionStorage
import com.panalbit.panalsuite.data.remote.ApiException
import com.panalbit.panalsuite.data.remote.AuthApi
import com.panalbit.panalsuite.domain.CreateUserRequest
import kotlinx.coroutines.flow.first
import com.panalbit.panalsuite.data.remote.apiResult
import com.panalbit.panalsuite.domain.LoginRequest
import com.panalbit.panalsuite.domain.Roles

class AuthRepository(private val api: AuthApi, private val storage: SessionStorage) {
    val session = storage.session

    suspend fun login(username: String, password: String) = apiResult {
        api.login(LoginRequest(username.trim(), password)).also {
            storage.save(it.token, it.user)
        }
    }

    suspend fun logout() = storage.clear()

    suspend fun getUsers() = apiResult { api.users() }

    suspend fun createUser(request: CreateUserRequest) = apiResult {
        val currentSession = session.first()
            ?: throw ApiException("Vuelve a iniciar sesión para crear usuarios", 401)
        api.createUser("Bearer ${currentSession.token}", request)
    }

    suspend fun getResolvers() = apiResult {
        api.users().filter { it.role == Roles.RESOLVER }
    }
}
