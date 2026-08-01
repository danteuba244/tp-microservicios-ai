package com.tp.customerservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tp.customerservice.dto.CustomerRequestDTO;
import com.tp.customerservice.dto.CustomerResponseDTO;
import com.tp.customerservice.exception.CustomerNotFoundException;
import com.tp.customerservice.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @MockBean
    private CustomerService service;

    private CustomerResponseDTO dto(long id) {
        return new CustomerResponseDTO(id, "Ozzy", "Osbourne", "ozzy@sabbath.com",
                "11111111", null, null, LocalDate.now());
    }

    @Test
    void getAll_devuelve200() throws Exception {
        when(service.findAll()).thenReturn(List.of(dto(1)));

        mvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Ozzy"));
    }

    @Test
    void getById_existente_devuelve200() throws Exception {
        when(service.findById(1L)).thenReturn(dto(1));

        mvc.perform(get("/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ozzy@sabbath.com"));
    }

    @Test
    void getById_inexistente_devuelve404() throws Exception {
        when(service.findById(999L)).thenThrow(new CustomerNotFoundException(999L));

        mvc.perform(get("/customers/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void create_bodyInvalido_emailMalFormado_devuelve400() throws Exception {
        CustomerRequestDTO req = new CustomerRequestDTO("Ozzy", "Osbourne",
                "no-es-email", "11111111", null, null, LocalDate.now());

        mvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void create_emailDuplicado_devuelve409() throws Exception {
        CustomerRequestDTO req = new CustomerRequestDTO("Ozzy", "Osbourne",
                "ozzy@sabbath.com", "11111111", null, null, LocalDate.now());
        when(service.create(any(CustomerRequestDTO.class)))
                .thenThrow(new DataIntegrityViolationException(
                        "unique constraint violated: EMAIL"));

        mvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("email o dni duplicado")));
    }
}
