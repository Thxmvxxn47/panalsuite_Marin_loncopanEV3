package com.panalbit.panalsuite.tickets;

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
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    private static final String VALID_TICKET = """
            {"title":"Impresora sin tinta","description":"La impresora del piso 2 no imprime",
             "type":"INCIDENTE","category":"INFRAESTRUCTURA","priority":"MEDIA","branch":"Casa Matriz",
             "requesterId":1,"requesterName":"Ana García López"}""";

    private long createTicket() throws Exception {
        String body = mockMvc.perform(post("/api/tickets").contentType(MediaType.APPLICATION_JSON).content(VALID_TICKET))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("NUEVO"))
                .andExpect(jsonPath("$.ticketNumber").value(org.hamcrest.Matchers.startsWith("TKT-")))
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).get("id").asLong();
    }

    @Test
    void listReturnsSeededTicketsAndSupportsFilters() throws Exception {
        mockMvc.perform(get("/api/tickets")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(8)));
        mockMvc.perform(get("/api/tickets").param("status", "RESUELTO")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("RESUELTO"));
        mockMvc.perform(get("/api/tickets").param("assignedToId", "2").param("priority", "CRITICA"))
                .andExpect(status().isOk());
    }

    @Test
    void createWithInvalidDataReturns400() throws Exception {
        mockMvc.perform(post("/api/tickets").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"description\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void fullCrudFlow() throws Exception {
        long id = createTicket();

        mockMvc.perform(get("/api/tickets/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Impresora sin tinta"));

        String update = """
                {"title":"Impresora sin tóner","description":"Cambiar tóner","type":"REQUERIMIENTO",
                 "category":"TI","priority":"BAJA","branch":""}""";
        mockMvc.perform(put("/api/tickets/" + id).contentType(MediaType.APPLICATION_JSON).content(update))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Impresora sin tóner"))
                .andExpect(jsonPath("$.priority").value("BAJA"))
                .andExpect(jsonPath("$.branch").doesNotExist());

        mockMvc.perform(delete("/api/tickets/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/tickets/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void statusChangeAssignsAndNotifies() throws Exception {
        long id = createTicket();
        String change = """
                {"status":"EN_PROGRESO","assignedToId":2,"assignedToName":"Carlos Pérez Muñoz",
                 "actorId":3,"actorName":"María Torres Silva"}""";
        mockMvc.perform(patch("/api/tickets/" + id + "/status").contentType(MediaType.APPLICATION_JSON).content(change))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EN_PROGRESO"))
                .andExpect(jsonPath("$.assignedToId").value(2));

        String body = mockMvc.perform(get("/api/notifications").param("userId", "2")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long notificationId = mapper.readTree(body).get(0).get("id").asLong();
        mockMvc.perform(patch("/api/notifications/" + notificationId + "/read")).andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
        mockMvc.perform(patch("/api/notifications/999999/read")).andExpect(status().isNotFound());
    }

    @Test
    void commentsCanBeAddedAndListed() throws Exception {
        long id = createTicket();
        String comment = "{\"authorId\":2,\"authorName\":\"Carlos Pérez Muñoz\",\"text\":\"Revisando\",\"internal\":true}";
        mockMvc.perform(post("/api/tickets/" + id + "/comments").contentType(MediaType.APPLICATION_JSON).content(comment))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.internal").value(true));
        mockMvc.perform(get("/api/tickets/" + id + "/comments")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        mockMvc.perform(post("/api/tickets/" + id + "/comments").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"authorId\":2,\"authorName\":\"x\",\"text\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void statsCountTicketsByState() throws Exception {
        String body = mockMvc.perform(get("/api/tickets/stats").param("assignedToId", "2"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertEquals(true, mapper.readTree(body).get("total").asLong() >= 8);
        mockMvc.perform(get("/api/tickets/stats")).andExpect(jsonPath("$.myAssigned").value(0));
    }
}
