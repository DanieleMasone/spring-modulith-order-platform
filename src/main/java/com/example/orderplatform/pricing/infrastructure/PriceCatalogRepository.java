package com.example.orderplatform.pricing.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceCatalogRepository extends JpaRepository<PriceCatalogItemEntity, String> {
}
