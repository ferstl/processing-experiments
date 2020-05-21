package com.github.ferstl.processing.event;

import java.util.UUID;
import com.github.ferstl.processing.MessageTypeOutbound;

public class MessageOutEvent extends ProcessingEvent {

  private final MessageTypeOutbound messageType;
  private final byte[] data;

  public MessageOutEvent(UUID correlationId, MessageTypeOutbound messageType, byte[] data) {
    super(correlationId);
    this.messageType = messageType;
    this.data = data;
  }

  public MessageTypeOutbound getMessageType() {
    return this.messageType;
  }

  public byte[] getData() {
    return this.data;
  }
}
