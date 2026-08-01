package com.tp.customerservice.dto;

import java.util.List;

/**
 * Agregacion cliente + sus productos (via Feign a product-service).
 */
public record CustomerWithProductsDTO(
        CustomerResponseDTO customer,
        List<ProductDTO> products
) {}
