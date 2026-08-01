package com.tp.productservice.mapper;

import com.tp.productservice.dto.ProductRequestDTO;
import com.tp.productservice.dto.ProductResponseDTO;
import com.tp.productservice.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponseDTO toResponse(Product p) {
        return new ProductResponseDTO(
                p.getId(),
                p.getNombre(),
                p.getDescripcion(),
                p.getCategoria(),
                p.getBanda(),
                p.getPrecio(),
                p.getStock(),
                p.getCustomerId(),
                p.getFechaCompra()
        );
    }

    public Product toEntity(ProductRequestDTO dto) {
        Product p = new Product();
        applyDto(p, dto);
        return p;
    }

    public void updateEntity(Product p, ProductRequestDTO dto) {
        applyDto(p, dto);
    }

    private void applyDto(Product p, ProductRequestDTO dto) {
        p.setNombre(dto.nombre());
        p.setDescripcion(dto.descripcion());
        p.setCategoria(dto.categoria());
        p.setBanda(dto.banda());
        p.setPrecio(dto.precio());
        p.setStock(dto.stock());
        p.setCustomerId(dto.customerId());
        p.setFechaCompra(dto.fechaCompra());
    }
}
