package com.sagaorchestrated.productvalidationservice.core.consumer;

import com.sagaorchestrated.productvalidationservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@AllArgsConstructor
@Component
public class ProductValidationConsumer {

  private final JsonUtil jsonUtil;


  @KafkaListener (
    groupId = "${spring.kafka.consumer.group-id}",
    topics = "${spring.kafka.topic.orchestrator}"
  )
  public void consumeOrchestratorEvent(String payload) {
    log.info("Receiving ending notification event {} from orchestrator topic", payload);
    var event = jsonUtil.toEvent(payload);
    log.info(event.toString());
  }

  @KafkaListener (
    groupId = "${spring.kafka.consumer.group-id}",
    topics = "${spring.kafka.topic.product-validation-success}"
  )
  public void consumeSuccessEvent(String payload) {
    log.info("Receiving success event {} from product-validation-success topic", payload);
    var event = jsonUtil.toEvent(payload);
    log.info(event.toString());
  }

  @KafkaListener (
    groupId = "${spring.kafka.consumer.group-id}",
    topics = "${spring.kafka.topic.product-validation-fail}"
  )
  public void consumeFailEvent(String payload) {
    log.info("Receiving rollback event {} from product-validation-fail topic", payload);
    var event = jsonUtil.toEvent(payload);
    log.info(event.toString());
  }

}
