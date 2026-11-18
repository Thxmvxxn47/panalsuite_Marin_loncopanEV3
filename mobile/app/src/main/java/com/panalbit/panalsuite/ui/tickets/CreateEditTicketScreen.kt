@file:OptIn(ExperimentalMaterial3Api::class)

package com.panalbit.panalsuite.ui.tickets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.panalbit.panalsuite.domain.Branches
import com.panalbit.panalsuite.domain.Categories
import com.panalbit.panalsuite.domain.Priority
import com.panalbit.panalsuite.domain.TicketTypes
import com.panalbit.panalsuite.ui.components.DropdownField
import com.panalbit.panalsuite.util.Labels
import com.panalbit.panalsuite.util.Validators

@Composable
fun CreateEditTicketScreen(viewModel: CreateEditTicketViewModel, onBack: () -> Unit, onSaved: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved) { if (state.saved) onSaved() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isEditing) "Editar ticket" else "Nueva solicitud") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                label = { Text("Título") },
                singleLine = true,
                isError = state.titleError != null,
                supportingText = { Text(state.titleError ?: "${state.title.length}/${Validators.MAX_TITLE}") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::onDescriptionChange,
                label = { Text("Descripción") },
                minLines = 4,
                isError = state.descriptionError != null,
                supportingText = { Text(state.descriptionError ?: "${state.description.length}/${Validators.MAX_DESCRIPTION}") },
                modifier = Modifier.fillMaxWidth()
            )
            DropdownField("Tipo", TicketTypes.all, state.type, viewModel::onTypeChange, Labels::type)
            DropdownField("Categoría", Categories.all, state.category, viewModel::onCategoryChange, Labels::category)
            DropdownField("Prioridad", Priority.all, state.priority, viewModel::onPriorityChange, Labels::priority)
            DropdownField("Sucursal (opcional)", Branches.all, state.branch, viewModel::onBranchChange)

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Button(onClick = viewModel::submit, enabled = !state.isLoading, modifier = Modifier.fillMaxWidth()) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (viewModel.isEditing) "Guardar cambios" else "Crear solicitud")
                }
            }
        }
    }
}
