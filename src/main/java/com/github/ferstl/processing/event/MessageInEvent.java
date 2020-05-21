package com.github.ferstl.processing.event;

import java.util.UUID;
import com.github.ferstl.processing.MessageTypeInbound;

public class MessageInEvent extends ProcessingEvent {

  private final MessageTypeInbound messageType;
  private final byte[] data;

  public MessageInEvent(UUID correlationId, MessageTypeInbound messageType, byte[] data) {
    super(correlationId);
    this.messageType = messageType;
    this.data = data;
  }

  public MessageTypeInbound getMessageType() {
    return this.messageType;
  }

  public byte[] getData() {
    return this.data;
  }
}
