package com.sagaorchestrated.productvalidationservice.core.repository;

import com.sagaorchestrated.productvalidationservice.core.model.Validation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ValidateRepository extends JpaRepository<Validation, Integer> {

  Boolean existsByOrderIdAndTransactionId(String orderId, String transactionId);
  Optional<Validation> findByOrderIdAndTransactionId(String orderId, String transactionId);

}
