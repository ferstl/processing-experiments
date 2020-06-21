package com.github.ferstl.processing.event.codec;

import java.util.UUID;

public abstract class ProcessingEvent {

  private final UUID correlationId;

  public ProcessingEvent(UUID correlationId) {
    this.correlationId = correlationId;
  }

  public final UUID getCorrelationId() {
    return this.correlationId;
  }
}
