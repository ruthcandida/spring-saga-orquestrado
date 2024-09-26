package com.sagaorchestrated.productvalidationservice.core.repository;

import com.sagaorchestrated.productvalidationservice.core.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {

  Boolean existsByCode(String code);
}
