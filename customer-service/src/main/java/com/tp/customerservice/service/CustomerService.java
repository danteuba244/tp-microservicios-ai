package com.tp.customerservice.service;

import com.tp.customerservice.client.ProductClient;
import com.tp.customerservice.dto.CustomerRequestDTO;
import com.tp.customerservice.dto.CustomerResponseDTO;
import com.tp.customerservice.dto.CustomerWithProductsDTO;
import com.tp.customerservice.dto.ProductDTO;
import com.tp.customerservice.exception.CustomerNotFoundException;
import com.tp.customerservice.mapper.CustomerMapper;
import com.tp.customerservice.entity.Customer;
import com.tp.customerservice.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository repository;
    private final CustomerMapper mapper;
    private final ProductClient productClient;

    public CustomerService(CustomerRepository repository,
                           CustomerMapper mapper,
                           ProductClient productClient) {
        this.repository = repository;
        this.mapper = mapper;
        this.productClient = productClient;
    }

    @Transactional(readOnly = true)
    public List<CustomerResponseDTO> findAll() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponseDTO findById(Long id) {
        return mapper.toResponse(getById(id));
    }

    @Transactional
    public CustomerResponseDTO create(CustomerRequestDTO dto) {
        Customer saved = repository.save(mapper.toEntity(dto));
        return mapper.toResponse(saved);
    }

    @Transactional
    public CustomerResponseDTO update(Long id, CustomerRequestDTO dto) {
        Customer existing = getById(id);
        mapper.updateEntity(existing, dto);
        return mapper.toResponse(repository.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        Customer existing = getById(id);
        repository.delete(existing);
    }

    /**
     * Agregacion: cliente + productos obtenidos via Feign a product-service.
     */
    @Transactional(readOnly = true)
    public CustomerWithProductsDTO findWithProducts(Long id) {
        CustomerResponseDTO customer = mapper.toResponse(getById(id));
        List<ProductDTO> products = productClient.getProductsByCustomer(id);
        return new CustomerWithProductsDTO(customer, products);
    }

    private Customer getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }
}
