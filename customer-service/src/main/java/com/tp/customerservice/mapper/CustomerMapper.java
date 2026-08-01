package com.tp.customerservice.mapper;

import com.tp.customerservice.dto.CustomerRequestDTO;
import com.tp.customerservice.dto.CustomerResponseDTO;
import com.tp.customerservice.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public CustomerResponseDTO toResponse(Customer customer) {
        return new CustomerResponseDTO(
                customer.getId(),
                customer.getNombre(),
                customer.getApellido(),
                customer.getEmail(),
                customer.getDni(),
                customer.getTelefono(),
                customer.getDireccion(),
                customer.getFechaRegistro()
        );
    }

    public Customer toEntity(CustomerRequestDTO dto) {
        Customer customer = new Customer();
        applyDto(customer, dto);
        return customer;
    }

    public void updateEntity(Customer customer, CustomerRequestDTO dto) {
        applyDto(customer, dto);
    }

    private void applyDto(Customer customer, CustomerRequestDTO dto) {
        customer.setNombre(dto.nombre());
        customer.setApellido(dto.apellido());
        customer.setEmail(dto.email());
        customer.setDni(dto.dni());
        customer.setTelefono(dto.telefono());
        customer.setDireccion(dto.direccion());
        customer.setFechaRegistro(dto.fechaRegistro());
    }
}
