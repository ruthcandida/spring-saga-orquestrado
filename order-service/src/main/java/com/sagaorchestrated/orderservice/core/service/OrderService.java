package com.sagaorchestrated.orderservice.core.service;

import com.sagaorchestrated.orderservice.core.document.Event;
import com.sagaorchestrated.orderservice.core.document.Order;
import com.sagaorchestrated.orderservice.core.dto.OrderRequest;
import com.sagaorchestrated.orderservice.core.producer.SagaProducer;
import com.sagaorchestrated.orderservice.core.repository.OrderRepository;
import com.sagaorchestrated.orderservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class OrderService {

  private static final String TRANSACTION_ID_PATTERN = "%s_%s";

  private final OrderRepository repository;
  private final JsonUtil jsonUtil;
  private final EventService eventService;
  private final SagaProducer producer;

  public Order createOrder(OrderRequest request) {
    var order = Order
      .builder()
      .products(request.products())
      .createdAt(LocalDateTime.now())
      .transactionId(
        String.format(TRANSACTION_ID_PATTERN, Instant.now().toEpochMilli(), UUID.randomUUID())
      )
      .build();

    repository.save(order);
    producer.sendEvent(jsonUtil.toJson(createPayload(order)));

    return order;
  }

  private Event createPayload(Order order) {
    var event = Event
      .builder()
      .orderId(order.getId())
      .transactionId(order.getTransactionId())
      .payload(order)
      .createdAt(LocalDateTime.now())
      .build();

    eventService.save(event);

    return event;
  }
}
