package com.sagaorchestrated.orderservice.core.service;

import com.sagaorchestrated.orderservice.config.exception.ValidationException;
import com.sagaorchestrated.orderservice.core.document.Event;
import com.sagaorchestrated.orderservice.core.dto.EventFilters;
import com.sagaorchestrated.orderservice.core.repository.EventRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.util.ObjectUtils.isEmpty;

@Service
@AllArgsConstructor
@Slf4j
public class EventService {

  private final EventRepository repository;

  public void notifyEnding(Event event) {
    event.setOrderId(event.getOrderId());
    event.setCreatedAt(LocalDateTime.now());
    save(event);
    log.info("Order {} with saga notified! TransactionId: {}", event.getOrderId(), event.getTransactionId());
  }

  public List<Event> findAll() {
    return repository.findAllByOrderByCreatedAtDesc();
  }

  public Event findByFilters(EventFilters filters) {
      validateEmptyFilters(filters);
      if(!isEmpty(filters.orderId()))
        return findByOrderId(filters.orderId());
      else
        return findByTransactionId(filters.transactionId());
  }

  private Event findByOrderId(String orderId) {
    return repository.findTop1ByOrderIdOrderByCreatedAtDesc(orderId)
      .orElseThrow(() -> new ValidationException("Event not found by orderID"));
  }

  private Event findByTransactionId(String transactionId) {
    return repository.findTop1ByTransactionIdOrderByCreatedAtDesc(transactionId)
      .orElseThrow(() -> new ValidationException("Event not found by transactionID"));
  }

  private void validateEmptyFilters(EventFilters filters) {
    if(isEmpty(filters.orderId()) && isEmpty(filters.transactionId())) {
      throw new ValidationException("OrderID or TransactionID must be informed");
    }
  }

  public Event save(Event event) {
    return repository.save(event);
  }
}
