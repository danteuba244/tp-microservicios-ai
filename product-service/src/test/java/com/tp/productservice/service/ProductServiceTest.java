package com.tp.productservice.service;

import com.tp.productservice.dto.ProductRequestDTO;
import com.tp.productservice.dto.ProductResponseDTO;
import com.tp.productservice.entity.Categoria;
import com.tp.productservice.entity.Product;
import com.tp.productservice.exception.ProductNotFoundException;
import com.tp.productservice.mapper.ProductMapper;
import com.tp.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @Mock
    private ProductMapper mapper;

    @InjectMocks
    private ProductService service;

    private Product ejemplo;
    private ProductResponseDTO ejemploDto;

    @BeforeEach
    void setUp() {
        ejemplo = new Product();
        ejemplo.setId(1L);
        ejemplo.setNombre("Les Paul");
        ejemplo.setCategoria(Categoria.INSTRUMENTO);
        ejemplo.setPrecio(new BigDecimal("2500.00"));
        ejemplo.setStock(3);
        ejemplo.setCustomerId(1L);
        ejemploDto = new ProductResponseDTO(1L, "Les Paul", null, Categoria.INSTRUMENTO,
                "Guns", new BigDecimal("2500.00"), 3, 1L, null);
    }

    @Test
    void findAll_devuelveListaMapeada() {
        when(repository.findAll()).thenReturn(List.of(ejemplo));
        when(mapper.toResponse(ejemplo)).thenReturn(ejemploDto);

        List<ProductResponseDTO> result = service.findAll();

        assertThat(result).hasSize(1).first().isEqualTo(ejemploDto);
    }

    @Test
    void findById_existente_devuelveDto() {
        when(repository.findById(1L)).thenReturn(Optional.of(ejemplo));
        when(mapper.toResponse(ejemplo)).thenReturn(ejemploDto);

        ProductResponseDTO result = service.findById(1L);

        assertThat(result).isEqualTo(ejemploDto);
    }

    @Test
    void findById_inexistente_lanzaProductNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(999L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void findByCustomerId_delegaEnRepositorio() {
        when(repository.findByCustomerId(1L)).thenReturn(List.of(ejemplo));
        when(mapper.toResponse(ejemplo)).thenReturn(ejemploDto);

        List<ProductResponseDTO> result = service.findByCustomerId(1L);

        assertThat(result).hasSize(1);
        verify(repository).findByCustomerId(1L);
    }

    @Test
    void create_persiste_yDevuelveDto() {
        ProductRequestDTO req = new ProductRequestDTO("x", null, Categoria.MERCH,
                null, new BigDecimal("10"), 1, null, null);
        when(mapper.toEntity(req)).thenReturn(ejemplo);
        when(repository.save(ejemplo)).thenReturn(ejemplo);
        when(mapper.toResponse(ejemplo)).thenReturn(ejemploDto);

        ProductResponseDTO result = service.create(req);

        assertThat(result).isEqualTo(ejemploDto);
        verify(repository).save(ejemplo);
    }

    @Test
    void update_existente_actualizaYPersiste() {
        ProductRequestDTO req = new ProductRequestDTO("nuevo", null, Categoria.MERCH,
                null, new BigDecimal("10"), 1, null, null);
        when(repository.findById(1L)).thenReturn(Optional.of(ejemplo));
        when(repository.save(any(Product.class))).thenReturn(ejemplo);
        when(mapper.toResponse(ejemplo)).thenReturn(ejemploDto);

        ProductResponseDTO result = service.update(1L, req);

        assertThat(result).isEqualTo(ejemploDto);
        verify(mapper).updateEntity(ejemplo, req);
    }

    @Test
    void update_inexistente_lanzaProductNotFound() {
        ProductRequestDTO req = new ProductRequestDTO("nuevo", null, Categoria.MERCH,
                null, new BigDecimal("10"), 1, null, null);
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(999L, req))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void delete_existente_llamaDeleteById() {
        when(repository.existsById(1L)).thenReturn(true);

        service.delete(1L);

        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    void delete_inexistente_lanzaProductNotFound() {
        when(repository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(999L))
                .isInstanceOf(ProductNotFoundException.class);
    }
}
