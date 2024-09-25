package com.sagaorchestrated.orderservice.core.dto;

import com.sagaorchestrated.orderservice.core.document.OrderProducts;

import java.util.List;

public record OrderRequest(List<OrderProducts> products) {
}
