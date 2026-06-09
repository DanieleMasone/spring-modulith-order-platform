package com.example.orderplatform.payments.api;

import com.example.orderplatform.Money;
import com.example.orderplatform.generated.api.PaymentsApi;
import com.example.orderplatform.generated.model.PaymentAuthorizeRequest;
import com.example.orderplatform.generated.model.PaymentResponse;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
class PaymentRestController implements PaymentsApi {

    private final PaymentManagement payments;

    PaymentRestController(PaymentManagement payments) {
        this.payments = payments;
    }

    @Override
    public ResponseEntity<PaymentResponse> authorizePayment(PaymentAuthorizeRequest request) {
        PaymentSummary payment = payments.authorize(
                request.getOrderId(),
                Money.of(request.getAmount().getAmount(), request.getAmount().getCurrency()));
        return ResponseEntity.ok(toResponse(payment));
    }

    @Override
    public ResponseEntity<PaymentResponse> getPayment(UUID paymentId) {
        return ResponseEntity.ok(toResponse(payments.getPayment(paymentId)));
    }

    private PaymentResponse toResponse(PaymentSummary payment) {
        return new PaymentResponse()
                .id(payment.id())
                .orderId(payment.orderId())
                .status(PaymentResponse.StatusEnum.valueOf(payment.status()))
                .amount(new com.example.orderplatform.generated.model.Money()
                        .amount(payment.amount().amount())
                        .currency(payment.amount().currency()))
                .createdAt(payment.createdAt())
                .authorizedAt(payment.authorizedAt());
    }
}
