package com.tp.customerservice.service;

import com.tp.customerservice.client.ProductClient;
import com.tp.customerservice.dto.CustomerRequestDTO;
import com.tp.customerservice.dto.CustomerResponseDTO;
import com.tp.customerservice.dto.CustomerWithProductsDTO;
import com.tp.customerservice.dto.ProductDTO;
import com.tp.customerservice.entity.Customer;
import com.tp.customerservice.exception.CustomerNotFoundException;
import com.tp.customerservice.mapper.CustomerMapper;
import com.tp.customerservice.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository repository;

    @Mock
    private CustomerMapper mapper;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private CustomerService service;

    private Customer ejemplo;
    private CustomerResponseDTO ejemploDto;

    @BeforeEach
    void setUp() {
        ejemplo = new Customer();
        ejemplo.setId(1L);
        ejemplo.setNombre("Ozzy");
        ejemplo.setApellido("Osbourne");
        ejemplo.setEmail("ozzy@sabbath.com");
        ejemplo.setDni("11111111");
        ejemploDto = new CustomerResponseDTO(1L, "Ozzy", "Osbourne", "ozzy@sabbath.com",
                "11111111", null, null, LocalDate.now());
    }

    @Test
    void findAll_devuelveListaMapeada() {
        when(repository.findAll()).thenReturn(List.of(ejemplo));
        when(mapper.toResponse(ejemplo)).thenReturn(ejemploDto);

        List<CustomerResponseDTO> result = service.findAll();

        assertThat(result).hasSize(1).first().isEqualTo(ejemploDto);
    }

    @Test
    void findById_existente_devuelveDto() {
        when(repository.findById(1L)).thenReturn(Optional.of(ejemplo));
        when(mapper.toResponse(ejemplo)).thenReturn(ejemploDto);

        assertThat(service.findById(1L)).isEqualTo(ejemploDto);
    }

    @Test
    void findById_inexistente_lanzaCustomerNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(999L))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void create_persisteYDevuelveDto() {
        CustomerRequestDTO req = new CustomerRequestDTO("Ozzy", "Osbourne",
                "ozzy@sabbath.com", "11111111", null, null, LocalDate.now());
        when(mapper.toEntity(req)).thenReturn(ejemplo);
        when(repository.save(ejemplo)).thenReturn(ejemplo);
        when(mapper.toResponse(ejemplo)).thenReturn(ejemploDto);

        assertThat(service.create(req)).isEqualTo(ejemploDto);
        verify(repository).save(ejemplo);
    }

    @Test
    void update_existente_actualizaYPersiste() {
        CustomerRequestDTO req = new CustomerRequestDTO("Ozzy", "Osbourne",
                "ozzy2@sabbath.com", "11111111", null, null, LocalDate.now());
        when(repository.findById(1L)).thenReturn(Optional.of(ejemplo));
        when(repository.save(any(Customer.class))).thenReturn(ejemplo);
        when(mapper.toResponse(ejemplo)).thenReturn(ejemploDto);

        service.update(1L, req);

        verify(mapper).updateEntity(ejemplo, req);
        verify(repository).save(ejemplo);
    }

    @Test
    void delete_existente_llamaDelete() {
        when(repository.findById(1L)).thenReturn(Optional.of(ejemplo));

        service.delete(1L);

        verify(repository).delete(ejemplo);
    }

    @Test
    void delete_inexistente_lanzaCustomerNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(999L))
                .isInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    void findWithProducts_agregaClienteYProductosViaFeign() {
        when(repository.findById(1L)).thenReturn(Optional.of(ejemplo));
        when(mapper.toResponse(ejemplo)).thenReturn(ejemploDto);

        ProductDTO producto = new ProductDTO(10L, "Les Paul", "desc", "INSTRUMENTO",
                "Guns", new BigDecimal("2500.00"), 3, 1L, null);
        when(productClient.getProductsByCustomer(1L)).thenReturn(List.of(producto));

        CustomerWithProductsDTO result = service.findWithProducts(1L);

        assertThat(result.customer()).isEqualTo(ejemploDto);
        assertThat(result.products()).hasSize(1);
        assertThat(result.products().get(0).nombre()).isEqualTo("Les Paul");
        verify(productClient).getProductsByCustomer(1L);
    }
}
