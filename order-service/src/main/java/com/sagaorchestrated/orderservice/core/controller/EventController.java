package com.sagaorchestrated.orderservice.core.controller;

import com.sagaorchestrated.orderservice.core.document.Event;
import com.sagaorchestrated.orderservice.core.dto.EventFilters;
import com.sagaorchestrated.orderservice.core.service.EventService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/event")
public class EventController {

  private final EventService eventService;

  @GetMapping
  public Event findByFilters(EventFilters filters) {
    return eventService.findByFilters(filters);
  }

  @GetMapping("all")
  public List<Event> findAll() {
    return eventService.findAll();
  }
}
