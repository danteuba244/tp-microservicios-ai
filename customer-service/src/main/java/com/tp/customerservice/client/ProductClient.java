package com.tp.customerservice.client;

import com.tp.customerservice.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Feign client hacia product-service (resuelto via Eureka por nombre logico).
 */
@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/products/customer/{customerId}")
    List<ProductDTO> getProductsByCustomer(@PathVariable("customerId") Long customerId);
}
