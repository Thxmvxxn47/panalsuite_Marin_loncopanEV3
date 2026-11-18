package com.panalbit.panalsuite.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel

/** Factory generica: permite crear ViewModels con dependencias (repositorios) sin Hilt. */
class VmFactory<VM : ViewModel>(private val creator: () -> VM) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = creator() as T
}

@Composable
inline fun <reified VM : ViewModel> rememberViewModel(key: String? = null, noinline creator: () -> VM): VM =
    viewModel(key = key, factory = VmFactory(creator))
