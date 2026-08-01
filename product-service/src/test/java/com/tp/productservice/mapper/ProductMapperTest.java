package com.tp.productservice.mapper;

import com.tp.productservice.dto.ProductRequestDTO;
import com.tp.productservice.dto.ProductResponseDTO;
import com.tp.productservice.entity.Categoria;
import com.tp.productservice.entity.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMapperTest {

    private final ProductMapper mapper = new ProductMapper();

    @Test
    void toResponse_copiaTodosLosCampos() {
        Product p = new Product();
        p.setId(10L);
        p.setNombre("Guitarra Test");
        p.setDescripcion("desc");
        p.setCategoria(Categoria.INSTRUMENTO);
        p.setBanda("Metallica");
        p.setPrecio(new BigDecimal("1999.99"));
        p.setStock(5);
        p.setCustomerId(2L);
        p.setFechaCompra(LocalDate.of(2026, 1, 15));

        ProductResponseDTO dto = mapper.toResponse(p);

        assertThat(dto.id()).isEqualTo(10L);
        assertThat(dto.nombre()).isEqualTo("Guitarra Test");
        assertThat(dto.categoria()).isEqualTo(Categoria.INSTRUMENTO);
        assertThat(dto.precio()).isEqualByComparingTo("1999.99");
        assertThat(dto.stock()).isEqualTo(5);
        assertThat(dto.customerId()).isEqualTo(2L);
        assertThat(dto.fechaCompra()).isEqualTo(LocalDate.of(2026, 1, 15));
    }

    @Test
    void toEntity_creaProductoNuevoSinId() {
        ProductRequestDTO req = new ProductRequestDTO(
                "Baqueta", "Vic Firth 5A", Categoria.INSTRUMENTO, "Neurosis",
                new BigDecimal("15.00"), 20, 3L, LocalDate.of(2026, 3, 1)
        );

        Product p = mapper.toEntity(req);

        assertThat(p.getId()).isNull();
        assertThat(p.getNombre()).isEqualTo("Baqueta");
        assertThat(p.getCategoria()).isEqualTo(Categoria.INSTRUMENTO);
        assertThat(p.getStock()).isEqualTo(20);
    }

    @Test
    void updateEntity_actualizaCamposSinTocarId() {
        Product existing = new Product();
        existing.setId(7L);
        existing.setNombre("Original");
        existing.setStock(1);

        ProductRequestDTO req = new ProductRequestDTO(
                "Actualizado", null, Categoria.MERCH, "Iron Maiden",
                new BigDecimal("100.00"), 50, null, null
        );

        mapper.updateEntity(existing, req);

        assertThat(existing.getId()).isEqualTo(7L);
        assertThat(existing.getNombre()).isEqualTo("Actualizado");
        assertThat(existing.getStock()).isEqualTo(50);
        assertThat(existing.getCategoria()).isEqualTo(Categoria.MERCH);
    }
}
