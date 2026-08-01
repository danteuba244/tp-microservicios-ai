package com.tp.customerservice.mapper;

import com.tp.customerservice.dto.CustomerRequestDTO;
import com.tp.customerservice.dto.CustomerResponseDTO;
import com.tp.customerservice.entity.Customer;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerMapperTest {

    private final CustomerMapper mapper = new CustomerMapper();

    @Test
    void toResponse_copiaTodosLosCampos() {
        Customer c = new Customer();
        c.setId(1L);
        c.setNombre("Ozzy");
        c.setApellido("Osbourne");
        c.setEmail("ozzy@sabbath.com");
        c.setDni("11111111");
        c.setTelefono("111");
        c.setDireccion("Birmingham");
        c.setFechaRegistro(LocalDate.of(2026, 1, 1));

        CustomerResponseDTO dto = mapper.toResponse(c);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.nombre()).isEqualTo("Ozzy");
        assertThat(dto.email()).isEqualTo("ozzy@sabbath.com");
        assertThat(dto.dni()).isEqualTo("11111111");
        assertThat(dto.fechaRegistro()).isEqualTo(LocalDate.of(2026, 1, 1));
    }

    @Test
    void toEntity_creaNuevoSinId() {
        CustomerRequestDTO req = new CustomerRequestDTO(
                "Ronnie", "Dio", "ronnie@dio.com", "22222222", "222", "NYC",
                LocalDate.of(2026, 2, 2)
        );

        Customer c = mapper.toEntity(req);

        assertThat(c.getId()).isNull();
        assertThat(c.getEmail()).isEqualTo("ronnie@dio.com");
        assertThat(c.getDni()).isEqualTo("22222222");
    }

    @Test
    void updateEntity_actualizaCamposSinTocarId() {
        Customer existing = new Customer();
        existing.setId(5L);
        existing.setNombre("Original");

        CustomerRequestDTO req = new CustomerRequestDTO(
                "Actualizado", "Nuevo", "n@n.com", "33333333", "333", "LA",
                LocalDate.of(2026, 3, 3)
        );

        mapper.updateEntity(existing, req);

        assertThat(existing.getId()).isEqualTo(5L);
        assertThat(existing.getNombre()).isEqualTo("Actualizado");
        assertThat(existing.getEmail()).isEqualTo("n@n.com");
    }
}
