/**
 * Payment module that reacts to committed orders and authorizes payments against the submitted order total.
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = "orders :: api"
)
package com.example.orderplatform.payments;
