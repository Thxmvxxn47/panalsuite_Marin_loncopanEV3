package com.panalbit.panalsuite.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.panalbit.panalsuite.domain.RegisterRequest
import kotlinx.coroutines.launch

@Composable
fun RegisterDialog(viewModel: LoginViewModel, onClose: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var done by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    AlertDialog(onDismissRequest = { if (!busy) onClose() }, title = { Text("Crear cuenta de solicitante") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(username, { username = it }, label = { Text("Usuario") }, singleLine = true)
            OutlinedTextField(name, { name = it }, label = { Text("Nombre completo") }, singleLine = true)
            OutlinedTextField(email, { email = it }, label = { Text("Correo") }, singleLine = true)
            OutlinedTextField(password, { password = it }, label = { Text("Contraseña") }, singleLine = true, visualTransformation = PasswordVisualTransformation())
            if (message.isNotEmpty()) Text(message, color = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
        }
    }, confirmButton = { TextButton(enabled = !busy, onClick = {
        if (done) { onClose() } else if (username.trim().length < 3 || name.isBlank() || !email.contains('@') || password.length < 6) {
            message = "Completa los campos y usa al menos 6 caracteres de contraseña"
        } else {
            busy = true
            scope.launch {
                viewModel.register(RegisterRequest(username.trim(), name.trim(), email.trim(), password)).fold(
                    onSuccess = { done = true; message = "Cuenta creada. Ya puedes iniciar sesión." },
                    onFailure = { message = it.message ?: "No se pudo crear la cuenta" }
                )
                busy = false
            }
        }
    }) { Text(if (done) "Volver al inicio" else if (busy) "Creando..." else "Registrar") } }, dismissButton = { TextButton(enabled = !busy, onClick = onClose) { Text("Cerrar") } })
}
