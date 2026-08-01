package com.tp.productservice.dto;

import com.tp.productservice.model.Categoria;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductRequestDTO(
        @NotBlank(message = "El nombre no puede estar vacio") String nombre,
        String descripcion,
        @NotNull(message = "La categoria es obligatoria") Categoria categoria,
        String banda,
        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.0", inclusive = true, message = "El precio no puede ser negativo")
        BigDecimal precio,
        @NotNull(message = "El stock es obligatorio")
        @Min(value = 0, message = "El stock no puede ser negativo")
        Integer stock,
        Long customerId,
        LocalDate fechaCompra
) {
}
