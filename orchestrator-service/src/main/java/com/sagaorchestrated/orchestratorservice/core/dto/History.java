package com.sagaorchestrated.orchestratorservice.core.dto;

import com.sagaorchestrated.orchestratorservice.core.enums.EEventSource;
import com.sagaorchestrated.orchestratorservice.core.enums.ESagaStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class History {

  private EEventSource source;
  private ESagaStatus status;
  private String message;
  private LocalDateTime createdAt;
}
