package com.panalbit.panalsuite.util

import org.junit.Assert.assertEquals
import org.junit.Test

class LabelsTest {
    @Test fun status_labels() {
        assertEquals("En progreso", Labels.status("EN_PROGRESO"))
        assertEquals("Nuevo", Labels.status("NUEVO"))
        assertEquals("Pendiente", Labels.status("PENDIENTE"))
        assertEquals("Resuelto", Labels.status("RESUELTO"))
        assertEquals("Cerrado", Labels.status("CERRADO"))
        assertEquals("OTRO", Labels.status("OTRO"))
    }
    @Test fun priority_labels() {
        assertEquals("Crítica", Labels.priority("CRITICA"))
        assertEquals("Alta", Labels.priority("ALTA"))
        assertEquals("Media", Labels.priority("MEDIA"))
        assertEquals("Baja", Labels.priority("BAJA"))
        assertEquals("X", Labels.priority("X"))
    }
    @Test fun type_category_role_action_labels() {
        assertEquals("Incidente", Labels.type("INCIDENTE"))
        assertEquals("Requerimiento", Labels.type("REQUERIMIENTO"))
        assertEquals("Consulta", Labels.type("CONSULTA"))
        assertEquals("x", Labels.type("x"))
        assertEquals("TI", Labels.category("TI"))
        assertEquals("Infraestructura", Labels.category("INFRAESTRUCTURA"))
        assertEquals("Cobranza", Labels.category("COBRANZA"))
        assertEquals("Gastos", Labels.category("GASTOS"))
        assertEquals("x", Labels.category("x"))
        assertEquals("Solicitante", Labels.role("SOLICITANTE"))
        assertEquals("Resolutor", Labels.role("RESOLUTOR"))
        assertEquals("Supervisor", Labels.role("SUPERVISOR"))
        assertEquals("Gerencia", Labels.role("GERENCIA"))
        assertEquals("Administrador", Labels.role("ADMINISTRADOR"))
        assertEquals("x", Labels.role("x"))
        listOf("TOMAR", "RESOLVER", "PAUSAR", "REANUDAR", "CERRAR", "REABRIR", "ESCALAR", "RECHAZAR", "ASIGNAR", "REASIGNAR")
            .forEach { assertEquals(it.lowercase().replaceFirstChar(Char::uppercase), Labels.action(it)) }
        assertEquals("x", Labels.action("x"))
    }
    @Test fun dateTime_formats() {
        assertEquals("17-11-2026 12:30", Labels.dateTime("2026-11-17T12:30:45.123"))
        assertEquals("—", Labels.dateTime(null))
        assertEquals("—", Labels.dateTime(""))
        assertEquals("sin-formato", Labels.dateTime("sin-formato"))
        assertEquals("2026-11T10:00", Labels.dateTime("2026-11T10:00"))
    }
    @Test fun ticketNumber_fallback() {
        assertEquals("TKT-2026-001", Labels.ticketNumber("TKT-2026-001", 1))
        assertEquals("TKT-7", Labels.ticketNumber(null, 7))
    }
}
