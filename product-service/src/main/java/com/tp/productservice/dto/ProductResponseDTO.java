package com.tp.productservice.dto;

import com.tp.productservice.entity.Categoria;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductResponseDTO(
        Long id,
        String nombre,
        String descripcion,
        Categoria categoria,
        String banda,
        BigDecimal precio,
        Integer stock,
        Long customerId,
        LocalDate fechaCompra
) {
}
