/**
 * Order module that validates customers, quotes requested products and publishes order creation events.
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = { "customers :: api", "pricing :: api" }
)
package com.example.orderplatform.orders;
