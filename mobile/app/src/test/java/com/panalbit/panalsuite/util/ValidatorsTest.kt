package com.panalbit.panalsuite.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidatorsTest {
    @Test fun username_blank_short_ok() {
        assertNotNull(Validators.validateUsername(" "))
        assertNotNull(Validators.validateUsername("ab"))
        assertNull(Validators.validateUsername("abc"))
    }
    @Test fun password_blank_short_ok() {
        assertNotNull(Validators.validatePassword(""))
        assertNotNull(Validators.validatePassword("123"))
        assertNull(Validators.validatePassword("1234"))
    }
    @Test fun title_rules() {
        assertEquals("El título es obligatorio", Validators.validateTitle(""))
        assertNotNull(Validators.validateTitle("abcd"))
        assertNotNull(Validators.validateTitle("a".repeat(151)))
        assertNull(Validators.validateTitle("Título válido"))
    }
    @Test fun description_rules() {
        assertEquals("La descripción es obligatoria", Validators.validateDescription(" "))
        assertNotNull(Validators.validateDescription("corta"))
        assertNotNull(Validators.validateDescription("a".repeat(2001)))
        assertNull(Validators.validateDescription("Una descripción suficientemente larga"))
    }
    @Test fun comment_rules() {
        assertNotNull(Validators.validateComment(""))
        assertNotNull(Validators.validateComment("a".repeat(1001)))
        assertNull(Validators.validateComment("ok"))
    }
    @Test fun required_rules() {
        assertEquals("Selecciona tipo", Validators.validateRequired(null, "tipo"))
        assertEquals("Selecciona tipo", Validators.validateRequired(" ", "tipo"))
        assertNull(Validators.validateRequired("X", "tipo"))
    }
}
