package com.example.orderplatform.customers.api;

import com.example.orderplatform.customers.application.CustomerApplicationService;
import com.example.orderplatform.generated.api.CustomersApi;
import com.example.orderplatform.generated.model.CustomerCreateRequest;
import com.example.orderplatform.generated.model.CustomerResponse;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
class CustomerRestController implements CustomersApi {

    private final CustomerApplicationService customers;

    CustomerRestController(CustomerApplicationService customers) {
        this.customers = customers;
    }

    @Override
    public ResponseEntity<CustomerResponse> createCustomer(CustomerCreateRequest request) {
        CustomerSnapshot customer = customers.createCustomer(request.getFullName(), request.getEmail());
        return ResponseEntity
                .created(URI.create("/customers/" + customer.id()))
                .body(toResponse(customer));
    }

    @Override
    public ResponseEntity<CustomerResponse> getCustomer(UUID customerId) {
        return ResponseEntity.ok(toResponse(customers.getRequiredCustomer(customerId)));
    }

    private CustomerResponse toResponse(CustomerSnapshot customer) {
        return new CustomerResponse()
                .id(customer.id())
                .email(customer.email())
                .fullName(customer.fullName())
                .status(CustomerResponse.StatusEnum.valueOf(customer.status()));
    }
}
