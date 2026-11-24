package com.panalbit.panalsuite.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    private String loginJson(String user, String pass) {
        return "{\"username\":\"" + user + "\",\"password\":\"" + pass + "\"}";
    }

    @Test
    void loginWithValidCredentialsReturnsTokenAndUser() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("resolutor", "pass123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.role").value("RESOLUTOR"));
    }

    @Test
    void loginWithWrongPasswordReturns401() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("resolutor", "mala")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Usuario o contraseña incorrectos"));
    }

    @Test
    void loginWithBlankFieldsReturns400() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("", "")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void meReturnsCurrentUserForValidToken() throws Exception {
        String body = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("admin", "admin123")))
                .andReturn().getResponse().getContentAsString();
        JsonNode json = mapper.readTree(body);

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + json.get("token").asText()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"));
        mockMvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }

    private String authorization(String user, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(user, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + mapper.readTree(response).get("token").asText();
    }

    @Test
    void userMutationsRequireAnAdministrator() throws Exception {
        String resolver = authorization("resolutor", "pass123");
        String body = """
                {"username":"prueba","name":"Prueba","email":"prueba@example.org","role":"ADMINISTRADOR","password":"prueba123"}
                """;
        for (String endpoint : new String[]{"/api/users", "/api/auth/register"}) {
            mockMvc.perform(post(endpoint).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isUnauthorized());
            mockMvc.perform(post(endpoint).header("Authorization", resolver)
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isForbidden());
        }

        mockMvc.perform(put("/api/users/1").header("Authorization", resolver)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/users/1").header("Authorization", resolver))
                .andExpect(status().isForbidden());
    }

    @Test
    void userCrudFlow() throws Exception {
        String admin = authorization("admin", "admin123");
        String createBody = "{\"username\":\"nuevo1\",\"name\":\"Usuario Nuevo\",\"email\":\"nuevo@empresa.cl\","
                + "\"role\":\"SOLICITANTE\",\"password\":\"secreto1\"}";
        String created = mockMvc.perform(post("/api/users").header("Authorization", admin).contentType(MediaType.APPLICATION_JSON).content(createBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = mapper.readTree(created).get("id").asLong();

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("nuevo1", "secreto1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.role").value("SOLICITANTE"));

        mockMvc.perform(post("/api/users").header("Authorization", admin).contentType(MediaType.APPLICATION_JSON).content(createBody))
                .andExpect(status().isConflict());

        String updateBody = "{\"username\":\"nuevo1\",\"name\":\"Nombre Editado\",\"email\":\"nuevo@empresa.cl\","
                + "\"role\":\"RESOLUTOR\"}";
        mockMvc.perform(put("/api/users/" + id).header("Authorization", admin).contentType(MediaType.APPLICATION_JSON).content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nombre Editado"))
                .andExpect(jsonPath("$.role").value("RESOLUTOR"));

        mockMvc.perform(get("/api/users/" + id)).andExpect(status().isOk());
        mockMvc.perform(delete("/api/users/" + id).header("Authorization", admin)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/users/" + id)).andExpect(status().isNotFound());
        assertEquals(5, mapper.readTree(mockMvc.perform(get("/api/users")).andReturn()
                .getResponse().getContentAsString()).size());
    }

    @Test
    void createUserWithShortPasswordReturns400() throws Exception {
        String admin = authorization("admin", "admin123");
        String body = "{\"username\":\"corto\",\"name\":\"Corto\",\"email\":\"c@empresa.cl\","
                + "\"role\":\"SOLICITANTE\",\"password\":\"123\"}";
        mockMvc.perform(post("/api/users").header("Authorization", admin).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }
}
