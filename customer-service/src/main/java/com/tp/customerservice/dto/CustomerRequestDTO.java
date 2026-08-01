package com.tp.customerservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CustomerRequestDTO(

        @NotBlank(message = "El nombre no puede estar vacio")
        String nombre,

        @NotBlank(message = "El apellido no puede estar vacio")
        String apellido,

        @NotBlank(message = "El email no puede estar vacio")
        @Email(message = "Email invalido")
        String email,

        @NotBlank(message = "El DNI no puede estar vacio")
        String dni,

        String telefono,

        String direccion,

        @NotNull(message = "La fecha de registro no puede ser nula")
        LocalDate fechaRegistro
) {}
