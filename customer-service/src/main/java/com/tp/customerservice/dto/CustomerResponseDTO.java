package com.tp.customerservice.dto;

import java.time.LocalDate;

public record CustomerResponseDTO(
        Long id,
        String nombre,
        String apellido,
        String email,
        String dni,
        String telefono,
        String direccion,
        LocalDate fechaRegistro
) {}
