package com.tp.productservice.service;

import com.tp.productservice.dto.ProductRequestDTO;
import com.tp.productservice.dto.ProductResponseDTO;
import com.tp.productservice.exception.ProductNotFoundException;
import com.tp.productservice.mapper.ProductMapper;
import com.tp.productservice.entity.Product;
import com.tp.productservice.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository repository;
    private final ProductMapper mapper;

    public ProductService(ProductRepository repository, ProductMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO findById(Long id) {
        Product p = repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return mapper.toResponse(p);
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> findByCustomerId(Long customerId) {
        return repository.findByCustomerId(customerId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional
    public ProductResponseDTO create(ProductRequestDTO dto) {
        Product saved = repository.save(mapper.toEntity(dto));
        return mapper.toResponse(saved);
    }

    @Transactional
    public ProductResponseDTO update(Long id, ProductRequestDTO dto) {
        Product p = repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        mapper.updateEntity(p, dto);
        return mapper.toResponse(repository.save(p));
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        repository.deleteById(id);
    }
}
