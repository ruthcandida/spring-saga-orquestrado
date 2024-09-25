package com.sagaorchestrated.paymentservice.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order {

  private String id;
  private List<OrderProducts> products;
  private LocalDateTime createdAt;
  private String transactionId;
  private Double totalAmount;
  private int totalItems;

}
