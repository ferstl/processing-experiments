package com.github.ferstl.processing.event.codec;

import java.util.UUID;

public class InboundMessage extends ProcessingEvent {

  private final int senderId;
  private final byte[] data;

  public InboundMessage(UUID correlationId, int senderId, byte[] data) {
    super(correlationId);
    this.senderId = senderId;
    this.data = data;
  }

  public int getSenderId() {
    return this.senderId;
  }

  public byte[] getData() {
    return this.data;
  }
}
