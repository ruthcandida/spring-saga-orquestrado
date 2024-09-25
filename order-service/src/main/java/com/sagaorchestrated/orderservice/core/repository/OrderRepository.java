package com.sagaorchestrated.orderservice.core.repository;

import com.sagaorchestrated.orderservice.core.document.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<Order, String> {
}
