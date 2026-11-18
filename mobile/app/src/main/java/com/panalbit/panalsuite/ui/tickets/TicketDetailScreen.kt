@file:OptIn(ExperimentalMaterial3Api::class)

package com.panalbit.panalsuite.ui.tickets

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.panalbit.panalsuite.domain.Comment
import com.panalbit.panalsuite.domain.Ticket
import com.panalbit.panalsuite.domain.TicketRules
import com.panalbit.panalsuite.domain.User
import com.panalbit.panalsuite.ui.components.ErrorMessage
import com.panalbit.panalsuite.ui.components.LabeledValue
import com.panalbit.panalsuite.ui.components.LoadingBox
import com.panalbit.panalsuite.ui.components.OfflineBanner
import com.panalbit.panalsuite.ui.components.PriorityChip
import com.panalbit.panalsuite.ui.components.SectionTitle
import com.panalbit.panalsuite.ui.components.StatusChip
import com.panalbit.panalsuite.util.EvidenceFiles
import com.panalbit.panalsuite.util.Labels

@Composable
fun TicketDetailScreen(
    viewModel: TicketDetailViewModel,
    user: User,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    var pendingAction by remember { mutableStateOf<String?>(null) }
    var selectedResolver by remember { mutableStateOf<User?>(null) }
    var showDelete by rememberSaveable { mutableStateOf(false) }
    var commentText by rememberSaveable { mutableStateOf("") }
    var internalComment by rememberSaveable { mutableStateOf(false) }

    // Recurso nativo: camara del dispositivo
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap != null) {
            viewModel.addEvidence(EvidenceFiles.save(context, state.ticket?.id ?: 0L, bitmap))
        }
    }

    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(state.deleted) { if (state.deleted) onBack() }
    LaunchedEffect(state.commentSentCount) { if (state.commentSentCount > 0) commentText = "" }
    LaunchedEffect(state.message) {
        state.message?.let {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            snackbarHostState.showSnackbar(it)
            viewModel.messageShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.ticket?.let { Labels.ticketNumber(it.ticketNumber, it.id) } ?: "Detalle") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") }
                },
                actions = {
                    state.ticket?.let { ticket ->
                        if (TicketRules.canEdit(user.role)) {
                            IconButton(onClick = { onEdit(ticket.id) }) { Icon(Icons.Default.Edit, contentDescription = "Editar") }
                        }
                        if (TicketRules.canDelete(user.role)) {
                            IconButton(onClick = { showDelete = true }) { Icon(Icons.Default.Delete, contentDescription = "Eliminar") }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        val ticket = state.ticket
        val error = state.error
        when {
            ticket == null && state.isLoading -> LoadingBox(Modifier.padding(padding))
            ticket == null -> ErrorMessage(error ?: "No se pudo cargar el ticket", onRetry = { viewModel.load() }, modifier = Modifier.padding(padding))
            else -> Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (state.isWorking) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                if (state.offline) OfflineBanner()

                TicketInfoCard(ticket)

                if (state.actions.isNotEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            SectionTitle("Acciones")
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                state.actions.forEach { action ->
                                    AssistChip(
                                        onClick = {
                                            if (TicketRules.requiresAssignee(action)) viewModel.loadResolvers()
                                            selectedResolver = null
                                            pendingAction = action
                                        },
                                        label = { Text(Labels.action(action)) },
                                        enabled = !state.isWorking
                                    )
                                }
                            }
                        }
                    }
                }

                EvidenceCard(
                    paths = state.evidence.map { it.filePath },
                    canAdd = TicketRules.canAddEvidence(user.role),
                    onTakePhoto = { cameraLauncher.launch(null) }
                )

                CommentsCard(
                    comments = state.comments,
                    canComment = TicketRules.canComment(user.role),
                    canInternal = TicketRules.canWriteInternalComments(user.role),
                    text = commentText,
                    onTextChange = { commentText = it },
                    internal = internalComment,
                    onInternalChange = { internalComment = it },
                    error = state.commentError,
                    onSend = { viewModel.addComment(commentText, internalComment) },
                    enabled = !state.isWorking
                )
            }
        }
    }

    pendingAction?.let { action ->
        val needsResolver = TicketRules.requiresAssignee(action)
        AlertDialog(
            onDismissRequest = { pendingAction = null },
            title = { Text("Confirmar acción") },
            text = {
                Column {
                    Text("¿Ejecutar \"${Labels.action(action)}\" en este ticket?")
                    if (needsResolver) {
                        Text("Selecciona el resolutor:", modifier = Modifier.padding(top = 12.dp))
                        if (state.resolvers.isEmpty()) Text("Cargando resolutores…", style = MaterialTheme.typography.bodyMedium)
                        state.resolvers.forEach { resolver ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedResolver = resolver },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = selectedResolver?.id == resolver.id, onClick = { selectedResolver = resolver })
                                Text(resolver.name)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !needsResolver || selectedResolver != null,
                    onClick = {
                        viewModel.executeAction(action, selectedResolver)
                        pendingAction = null
                    }
                ) { Text("Confirmar") }
            },
            dismissButton = { TextButton(onClick = { pendingAction = null }) { Text("Cancelar") } }
        )
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Eliminar ticket") },
            text = { Text("Esta acción no se puede deshacer. ¿Eliminar el ticket?") },
            confirmButton = {
                TextButton(onClick = {
                    showDelete = false
                    viewModel.deleteTicket()
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { showDelete = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun TicketInfoCard(ticket: Ticket) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(ticket.title, style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusChip(ticket.status)
                PriorityChip(ticket.priority)
            }
            Text(ticket.description, style = MaterialTheme.typography.bodyLarge)
            Divider()
            Row(modifier = Modifier.fillMaxWidth()) {
                LabeledValue("Tipo", Labels.type(ticket.type), Modifier.weight(1f))
                LabeledValue("Categoría", Labels.category(ticket.category), Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                LabeledValue("Sucursal", ticket.branch ?: "Sin sucursal", Modifier.weight(1f))
                LabeledValue("Solicitante", ticket.requesterName, Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                LabeledValue("Asignado a", ticket.assignedToName ?: "Sin asignar", Modifier.weight(1f))
                LabeledValue("Creado", Labels.dateTime(ticket.createdAt), Modifier.weight(1f))
            }
            LabeledValue("Última actualización", Labels.dateTime(ticket.updatedAt))
        }
    }
}

@Composable
private fun EvidenceCard(paths: List<String>, canAdd: Boolean, onTakePhoto: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle("Evidencia fotográfica (guardada en el dispositivo)")
            if (paths.isEmpty()) {
                Text("Aún no hay fotos", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    paths.forEach { path ->
                        val bitmap = remember(path) { BitmapFactory.decodeFile(path)?.asImageBitmap() }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap,
                                contentDescription = "Evidencia",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                        }
                    }
                }
            }
            if (canAdd) {
                OutlinedButton(onClick = onTakePhoto) { Text("Tomar foto") }
            }
        }
    }
}

@Composable
private fun CommentsCard(
    comments: List<Comment>,
    canComment: Boolean,
    canInternal: Boolean,
    text: String,
    onTextChange: (String) -> Unit,
    internal: Boolean,
    onInternalChange: (Boolean) -> Unit,
    error: String?,
    onSend: () -> Unit,
    enabled: Boolean
) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionTitle("Comentarios (${comments.size})")
            if (comments.isEmpty()) {
                Text("Sin comentarios todavía", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            comments.forEach { comment ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (comment.internal) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = comment.authorName + if (comment.internal) " · interno" else "",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(comment.text, style = MaterialTheme.typography.bodyMedium)
                        Text(Labels.dateTime(comment.createdAt), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if (canComment) {
                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChange,
                    label = { Text("Escribe un comentario") },
                    isError = error != null,
                    supportingText = { error?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
                if (canInternal) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Switch(checked = internal, onCheckedChange = onInternalChange)
                        Text("Comentario interno", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Button(onClick = onSend, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                    Text("Enviar comentario", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}
