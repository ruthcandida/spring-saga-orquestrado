package com.sagaorchestrated.paymentservice.core.service;

import com.sagaorchestrated.paymentservice.config.exception.ValidationException;
import com.sagaorchestrated.paymentservice.core.dto.Event;
import com.sagaorchestrated.paymentservice.core.dto.History;
import com.sagaorchestrated.paymentservice.core.dto.OrderProducts;
import com.sagaorchestrated.paymentservice.core.enums.EPaymentStatus;
import com.sagaorchestrated.paymentservice.core.model.Payment;
import com.sagaorchestrated.paymentservice.core.producer.KafkaProducer;
import com.sagaorchestrated.paymentservice.core.repository.PaymentRepository;
import com.sagaorchestrated.paymentservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.sagaorchestrated.paymentservice.core.enums.ESagaStatus.*;

@Slf4j
@Service
@AllArgsConstructor
public class PaymentService {

  private static final String CURRENT_SOURCE = "PAYMENT_SERVICE";
  private static final Double REDUCE_SUM_VALUE = 0.0;
  private static final Double MIN_AMOUNT_VALUE = 0.1;

  private final JsonUtil jsonUtil;
  private final KafkaProducer producer;
  private final PaymentRepository paymentRepository;

  public void realizePayment(Event event) {
    try {
      checkCurrentValidation(event);
      createPendingPayment(event);
      var payment = findByOrderIdANdTransactionId(event);
      validateAmount(payment.getTotalAmount());
      changePaymentToSuccess(payment);
      handleSuccess(event);
    } catch (Exception ex) {
      log.error("Error trying to make payment: ", ex);
      handleFailCurrentNotExecuted(event, ex.getMessage());
    }

    producer.sendEvent(jsonUtil.toJson(event));

  }

  private void  checkCurrentValidation(Event event) {
    if(paymentRepository.existsByOrderIdAndTransactionId(event.getOrderId(), event.getTransactionId())) {
      throw new ValidationException("There's another transactionId for this validation!");
    }
  }

  private void createPendingPayment(Event event) {
    var totalAmount = calculateAmount(event);
    var totalItems = calculateItems(event);
    var payment = Payment
      .builder()
      .orderId(event.getOrderId())
      .transactionId(event.getTransactionId())
      .totalAmount(totalAmount)
      .totalItems(totalItems)
      .build();
    save(payment);
    setEventAmountItems(event, payment);
  }

  private double calculateAmount(Event event) {
    return event
      .getPayload()
      .getProducts()
      .stream()
      .map(product -> product.getQuantity() * product.getProduct().getUnitValue())
      .reduce(REDUCE_SUM_VALUE, Double::sum);
  }

  private int calculateItems(Event event) {
    return event
      .getPayload()
      .getProducts()
      .stream()
      .map(OrderProducts::getQuantity)
      .reduce(REDUCE_SUM_VALUE.intValue(), Integer::sum);
  }

  private void setEventAmountItems(Event event, Payment payment) {
    event.getPayload().setTotalAmount(payment.getTotalAmount());
    event.getPayload().setTotalItems(payment.getTotalItems());
  }

  private void validateAmount(double amount) {
    if(amount < 0.1) {
      throw new ValidationException("The minimum amount available is ".concat(MIN_AMOUNT_VALUE.toString()));
    }
  }

  private void changePaymentToSuccess(Payment payment) {
    payment.setStatus(EPaymentStatus.SUCCESS);
    save(payment);
  }

  private void handleSuccess(Event event) {
    event.setStatus(SUCCESS);
    event.setSource(CURRENT_SOURCE);
    addHistory(event, "Payment realized successfully!");
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
    addHistory(event, "Fail to realize payment: ".concat(message));
  }

  public void realizeRefund(Event event) {
    event.setStatus(FAIL);
    event.setSource(CURRENT_SOURCE);
    try {
      changePaymentStatusToRefund(event);
      addHistory(event, "Rollback executed for payment!");
    } catch (Exception ex) {
      addHistory(event, "Rollback NOT executed for payment: ".concat(ex.getMessage()));
    }
    producer.sendEvent(jsonUtil.toJson(event));
  }

  private void changePaymentStatusToRefund(Event event) {
    var payment = findByOrderIdANdTransactionId(event);
    payment.setStatus(EPaymentStatus.REFUND);
    setEventAmountItems(event, payment);
    save(payment);
  }

  private Payment findByOrderIdANdTransactionId(Event event) {
    return paymentRepository
      .findByOrderIdAndTransactionId(event.getPayload().getId(), event.getTransactionId())
      .orElseThrow(() -> new ValidationException("Payment not found by orderID and TransactionID"));
  }

  private void save(Payment payment) {
    paymentRepository.save(payment);
  }


}
