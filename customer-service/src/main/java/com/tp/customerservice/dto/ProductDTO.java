package com.tp.customerservice.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Copia local del ProductResponseDTO expuesto por product-service.
 * Se usa como tipo de retorno del Feign client para deserializar el JSON.
 */
public record ProductDTO(
        Long id,
        String nombre,
        String descripcion,
        String categoria,
        String banda,
        BigDecimal precio,
        Integer stock,
        Long customerId,
        LocalDate fechaCompra
) {}
