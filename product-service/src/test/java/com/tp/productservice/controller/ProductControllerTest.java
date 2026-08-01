package com.tp.productservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tp.productservice.dto.ProductRequestDTO;
import com.tp.productservice.dto.ProductResponseDTO;
import com.tp.productservice.entity.Categoria;
import com.tp.productservice.exception.ProductNotFoundException;
import com.tp.productservice.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @MockBean
    private ProductService service;

    private ProductResponseDTO dto(long id) {
        return new ProductResponseDTO(id, "Les Paul", null, Categoria.INSTRUMENTO,
                "Guns", new BigDecimal("2500.00"), 3, 1L, null);
    }

    @Test
    void getAll_devuelve200() throws Exception {
        when(service.findAll()).thenReturn(List.of(dto(1)));

        mvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Les Paul"));
    }

    @Test
    void getById_existente_devuelve200() throws Exception {
        when(service.findById(1L)).thenReturn(dto(1));

        mvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.categoria").value("INSTRUMENTO"));
    }

    @Test
    void getById_inexistente_devuelve404() throws Exception {
        when(service.findById(999L)).thenThrow(new ProductNotFoundException(999L));

        mvc.perform(get("/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Producto no encontrado con id: 999"));
    }

    @Test
    void create_bodyValido_devuelve201() throws Exception {
        ProductRequestDTO req = new ProductRequestDTO("Nuevo", null, Categoria.MERCH,
                "Nirvana", new BigDecimal("50.00"), 10, null, null);
        when(service.create(any(ProductRequestDTO.class))).thenReturn(dto(1));

        mvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void create_bodyInvalido_devuelve400() throws Exception {
        ProductRequestDTO req = new ProductRequestDTO("", null, null, null, null, null, null, null);

        mvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void delete_devuelve204() throws Exception {
        mvc.perform(delete("/products/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_inexistente_devuelve404() throws Exception {
        org.mockito.Mockito.doThrow(new ProductNotFoundException(999L))
                .when(service).delete(eq(999L));

        mvc.perform(delete("/products/999"))
                .andExpect(status().isNotFound());
    }
}
