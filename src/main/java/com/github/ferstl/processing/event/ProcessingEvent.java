package com.github.ferstl.processing.event;

import java.util.UUID;

public abstract class ProcessingEvent {

  private final UUID correlationId;

  public ProcessingEvent(UUID correlationId) {
    this.correlationId = correlationId;
  }

  public UUID getCorrelationId() {
    return this.correlationId;
  }
}
