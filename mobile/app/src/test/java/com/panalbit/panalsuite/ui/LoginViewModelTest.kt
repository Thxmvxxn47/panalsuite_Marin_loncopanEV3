package com.panalbit.panalsuite.ui

import com.panalbit.panalsuite.data.remote.ApiException
import com.panalbit.panalsuite.data.repository.AuthRepository
import com.panalbit.panalsuite.domain.LoginResponse
import com.panalbit.panalsuite.testutil.MainDispatcherRule
import com.panalbit.panalsuite.testutil.Samples
import com.panalbit.panalsuite.ui.login.LoginViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()
    private val repo = mockk<AuthRepository>()
    private val vm = LoginViewModel(repo)

    @Test fun invalid_input_shows_errors_and_does_not_call_repo() {
        vm.submit()
        assertNotNull(vm.state.value.usernameError)
        assertNotNull(vm.state.value.passwordError)
        coVerify(exactly = 0) { repo.login(any(), any()) }
    }

    @Test fun editing_clears_error() {
        vm.submit()
        vm.onUsernameChange("abc")
        vm.onPasswordChange("1234")
        assertNull(vm.state.value.usernameError)
        assertNull(vm.state.value.passwordError)
    }

    @Test fun success_marks_logged_in() {
        coEvery { repo.login("resolutor", "pass123") } returns Result.success(LoginResponse("t", Samples.resolver))
        vm.onUsernameChange("resolutor")
        vm.onPasswordChange("pass123")
        vm.submit()
        assertTrue(vm.state.value.loggedIn)
        assertFalse(vm.state.value.isLoading)
    }

    @Test fun failure_shows_message() {
        coEvery { repo.login(any(), any()) } returns Result.failure(ApiException("Usuario o contraseña incorrectos", 401))
        vm.onUsernameChange("resolutor")
        vm.onPasswordChange("mala")
        vm.submit()
        assertEquals("Usuario o contraseña incorrectos", vm.state.value.error)
        assertFalse(vm.state.value.loggedIn)
    }
}
