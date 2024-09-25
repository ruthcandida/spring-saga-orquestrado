package com.sagaorchestrated.orderservice.core.controller;

import com.sagaorchestrated.orderservice.core.document.Order;
import com.sagaorchestrated.orderservice.core.dto.OrderRequest;
import com.sagaorchestrated.orderservice.core.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/order")
public class OrderController {

  private final OrderService orderService;

  @PostMapping
  public Order createOrder(@RequestBody OrderRequest orderRequest) {
    return  orderService.createOrder(orderRequest);
  }
}
