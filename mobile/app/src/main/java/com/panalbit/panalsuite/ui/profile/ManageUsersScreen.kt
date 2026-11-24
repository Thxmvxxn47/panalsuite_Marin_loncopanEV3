@file:OptIn(ExperimentalMaterial3Api::class)

package com.panalbit.panalsuite.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.panalbit.panalsuite.domain.CreateUserRequest
import com.panalbit.panalsuite.domain.Roles
import com.panalbit.panalsuite.ui.components.DropdownField
import com.panalbit.panalsuite.util.Labels

@Composable
fun ManageUsersScreen(viewModel: ManageUsersViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    val search = query.trim()
    val visible = state.users.filter { user ->
        listOf(user.name, user.username, user.email, Labels.role(user.role))
            .any { it.contains(search, ignoreCase = true) }
    }.sortedBy { it.name.lowercase() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestionar usuarios") },
                navigationIcon = {
                    IconButton(onClick = onBack, enabled = !state.saving) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refresh, enabled = !state.loading && !state.saving) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar usuarios")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Button(
                    onClick = { viewModel.setFormVisible(true) },
                    enabled = !state.loading
                ) { Text("Nuevo usuario") }
            }
            state.createdUsername?.let { username ->
                item { Text("Cuenta creada. $username ya puede iniciar sesión.") }
            }
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Buscar usuarios") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            if (state.loading) {
                item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
            }
            state.listError?.let { error ->
                item { Text(error, color = MaterialTheme.colorScheme.error) }
            }
            if (visible.isEmpty() && !state.loading && state.listError == null) {
                item { Text(if (search.isEmpty()) "No hay usuarios" else "No hay coincidencias") }
            }
            items(visible, key = { it.id }) { user ->
                ListItem(
                    headlineContent = { Text(user.name) },
                    supportingContent = { Text("@${user.username} · ${user.email}") },
                    overlineContent = { Text(Labels.role(user.role)) }
                )
            }
        }
    }

    if (state.showForm) {
        CreateUserDialog(
            saving = state.saving,
            error = state.formError,
            onDismiss = { viewModel.setFormVisible(false) },
            onCreate = { request ->
                query = ""
                viewModel.create(request)
            }
        )
    }
}

@Composable
private fun CreateUserDialog(
    saving: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onCreate: (CreateUserRequest) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var role by rememberSaveable { mutableStateOf(Roles.REQUESTER) }
    var password by remember { mutableStateOf("") }
    val roles = listOf(Roles.REQUESTER, Roles.RESOLVER, Roles.SUPERVISOR, Roles.MANAGEMENT, Roles.ADMIN)
    val valid = name.isNotBlank() && username.trim().length in 3..50 &&
        email.isNotBlank() && password.length >= 6 && password.toByteArray().size <= 72

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo usuario") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UserField("Nombre", name, !saving) { name = it }
                UserField("Usuario", username, !saving) { username = it }
                UserField("Correo", email, !saving, KeyboardType.Email) { email = it }
                DropdownField("Rol", roles, role, { if (!saving) role = it }, Labels::role)
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    supportingText = { Text("Mínimo 6 caracteres") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !saving
                )
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid && !saving,
                onClick = {
                    onCreate(CreateUserRequest(username.trim(), name.trim(), email.trim(), role, password))
                }
            ) { Text(if (saving) "Guardando…" else "Crear cuenta") }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !saving) { Text("Cancelar") } }
    )
}

@Composable
private fun UserField(
    label: String,
    value: String,
    enabled: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        enabled = enabled
    )
}
