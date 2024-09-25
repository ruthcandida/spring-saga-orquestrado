package com.sagaorchestrated.orderservice.core.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {

  private String code;
  private double unitValue;
}
