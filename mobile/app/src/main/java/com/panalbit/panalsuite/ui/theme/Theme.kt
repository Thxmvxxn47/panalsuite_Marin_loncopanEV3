package com.panalbit.panalsuite.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = Color(0xFFB45309),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFEF3C7),
    onPrimaryContainer = Color(0xFF451A03),
    secondary = Color(0xFF6B7280),
    background = Color(0xFFF8F9FA),
    onBackground = Color(0xFF1F2937),
    surface = Color.White,
    onSurface = Color(0xFF1F2937),
    surfaceVariant = Color(0xFFF3F4F6),
    onSurfaceVariant = Color(0xFF6B7280),
    error = Color(0xFFDC2626)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFF59E0B),
    onPrimary = Color(0xFF451A03),
    primaryContainer = Color(0xFF78350F),
    onPrimaryContainer = Color(0xFFFEF3C7),
    secondary = Color(0xFF9CA3AF),
    background = Color(0xFF111827),
    onBackground = Color(0xFFF3F4F6),
    surface = Color(0xFF1F2937),
    onSurface = Color(0xFFF3F4F6),
    surfaceVariant = Color(0xFF374151),
    onSurfaceVariant = Color(0xFFD1D5DB),
    error = Color(0xFFF87171)
)

private val AppTypography = Typography(
    headlineSmall = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp),
    bodyMedium = TextStyle(fontSize = 14.sp),
    labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium)
)

@Composable
fun PanalSuiteTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}

/** Colores semanticos de prioridad y estado. */
object AppColors {

    fun priority(value: String): Color = when (value) {
        "CRITICA" -> Color(0xFFEF4444)
        "ALTA" -> Color(0xFFF97316)
        "MEDIA" -> Color(0xFFCA8A04)
        else -> Color(0xFF16A34A)
    }

    fun status(value: String): Color = when (value) {
        "NUEVO" -> Color(0xFF3B82F6)
        "EN_PROGRESO" -> Color(0xFF8B5CF6)
        "PENDIENTE" -> Color(0xFFD97706)
        "RESUELTO" -> Color(0xFF059669)
        "CERRADO" -> Color(0xFF6B7280)
        else -> Color(0xFF3B82F6)
    }
}
