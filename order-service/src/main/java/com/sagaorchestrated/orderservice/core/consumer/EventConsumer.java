package com.sagaorchestrated.orderservice.core.consumer;

import com.sagaorchestrated.orderservice.core.service.EventService;
import com.sagaorchestrated.orderservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@AllArgsConstructor
@Component
public class EventConsumer {

  private final EventService eventService;
  private final JsonUtil jsonUtil;

  @KafkaListener (
    groupId = "${spring.kafka.consumer.group-id}",
    topics = "${spring.kafka.topic.notify-ending}"
  )
  public void consumeNotifyEndingEvent(String payload) {
    log.info("Receiving ending notification event {} from notify-ending topic", payload);
    var event = jsonUtil.toEvent(payload);
    eventService.notifyEnding(event);
  }

}
