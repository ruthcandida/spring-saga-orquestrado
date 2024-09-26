package com.sagaorchestrated.productvalidationservice.core.service;

import ch.qos.logback.core.encoder.EchoEncoder;
import com.sagaorchestrated.productvalidationservice.config.exception.ValidationException;
import com.sagaorchestrated.productvalidationservice.core.dto.Event;
import com.sagaorchestrated.productvalidationservice.core.dto.History;
import com.sagaorchestrated.productvalidationservice.core.dto.OrderProducts;

import com.sagaorchestrated.productvalidationservice.core.model.Validation;
import com.sagaorchestrated.productvalidationservice.core.producer.KafkaProducer;
import com.sagaorchestrated.productvalidationservice.core.repository.ProductRepository;
import com.sagaorchestrated.productvalidationservice.core.repository.ValidateRepository;
import com.sagaorchestrated.productvalidationservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.sagaorchestrated.productvalidationservice.core.enums.ESagaStatus.*;
import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Service
@AllArgsConstructor
public class ProductValidationService {

  private static final String CURRENT_SOURCE = "PRODUCT_VALIDATION_SERVICE";

  private final JsonUtil jsonUtil;
  private final KafkaProducer producer;
  private final ProductRepository productRepository;
  private final ValidateRepository validateRepository;

  public void validateExistingProducts(Event event) {
    try {
      checkCurrentValidation(event);
      createValidation(event, true);
      handleSuccess(event);
    } catch (Exception ex) {
      log.error("Error trying to validate products: ", ex);
      handleFailCurrentNotExecuted(event, ex.getMessage());
    }

    producer.sendEvent(jsonUtil.toJson(event));
  }

  private void validateProductsInformed(Event event) {
    if(isEmpty(event.getPayload()) || isEmpty(event.getPayload().getProducts())) {
      throw new ValidationException("Product list is empty!");
    }
    if(isEmpty(event.getPayload().getId()) || isEmpty(event.getPayload().getTransactionId())) {
      throw new ValidationException("OrderID and TransactionID must be informed!");
    }
  }

  private void validateProductInformed(OrderProducts product) {
    if (isEmpty(product.getProduct()) || isEmpty(product.getProduct().getCode())) {
      throw new ValidationException("Product must be informed!");
    }
  }

  private void validateExistingProduct(String code) {
    if(!productRepository.existsByCode(code)) {
      throw new ValidationException("Product does not exists in database!");
    }
  }

  private void checkExistsOrderIdAndTransactionId(Event event) {
    if(validateRepository.existsByOrderIdAndTransactionId(event.getOrderId(), event.getTransactionId())) {
      throw new ValidationException("There's another transactionId for this validation!");
    }
  }

  private void  checkCurrentValidation(Event event) {
    validateProductsInformed(event);
    checkExistsOrderIdAndTransactionId(event);

    event.getPayload().getProducts().forEach(product -> {
      validateProductInformed(product);
      validateExistingProduct(product.getProduct().getCode());
    });
  }

  private void createValidation(Event event, boolean success) {
    var validation = Validation
      .builder()
      .orderId(event.getOrderId())
      .transactionId(event.getTransactionId())
      .success(success)
      .build();

    validateRepository.save(validation);
  }

  private void handleSuccess(Event event) {
    event.setStatus(SUCCESS);
    event.setSource(CURRENT_SOURCE);
    addHistory(event, "Products are validated successfully!");
  }

  private void addHistory(Event event, String message) {
    var history = History
      .builder()
      .source(event.getSource())
      .status(event.getStatus())
      .message(message)
      .createdAt(LocalDateTime.now())
      .build();
    event.addToHistory(history);

  }

  private void handleFailCurrentNotExecuted(Event event, String message) {
    event.setStatus(ROLLBACK_PENDING);
    event.setSource(CURRENT_SOURCE);
    addHistory(event, "Fail to validate products: ".concat(message));
  }

  private void changeValidationToFail(Event event) {
    validateRepository
      .findByOrderIdAndTransactionId(event.getPayload().getId(), event.getPayload().getTransactionId())
      .ifPresentOrElse(validation -> {
          validation.setSuccess(false);
          validateRepository.save(validation);
        },
        () -> createValidation(event, false));
  }

  public void rollbackEvent(Event event) {
    changeValidationToFail(event);
    event.setStatus(FAIL);
    event.setSource(CURRENT_SOURCE);
    addHistory(event, "Rollback executed on product validation!");
    producer.sendEvent(jsonUtil.toJson(event));
  }









}
