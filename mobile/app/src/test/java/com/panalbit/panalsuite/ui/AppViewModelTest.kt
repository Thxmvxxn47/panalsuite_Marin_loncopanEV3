package com.panalbit.panalsuite.ui

import com.panalbit.panalsuite.data.remote.AuthApi
import com.panalbit.panalsuite.data.repository.AuthRepository
import com.panalbit.panalsuite.domain.Session
import com.panalbit.panalsuite.testutil.FakeSessionStorage
import com.panalbit.panalsuite.testutil.MainDispatcherRule
import com.panalbit.panalsuite.testutil.Samples
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AppViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()

    @Test fun logged_out_when_no_session() {
        val vm = AppViewModel(AuthRepository(mockk<AuthApi>(), FakeSessionStorage()))
        assertEquals(SessionState.LoggedOut, vm.sessionState.value)
    }

    @Test fun logged_in_and_logout_clears() {
        val storage = FakeSessionStorage(Session("tok", Samples.admin))
        val vm = AppViewModel(AuthRepository(mockk<AuthApi>(), storage))
        assertTrue(vm.sessionState.value is SessionState.LoggedIn)
        vm.logout()
        assertEquals(SessionState.LoggedOut, vm.sessionState.value)
    }
}
