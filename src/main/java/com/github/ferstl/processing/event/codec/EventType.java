package com.github.ferstl.processing.event.codec;

public enum EventType {
  RESERVATION,
  COMMUNICATION,
  INBOUND_MESSAGE,
  SETTLEMENT;

  public static <T extends ProcessingEvent> EventType forEvent(T event) {
    if (event instanceof Reservation) {
      return RESERVATION;
    } else {
      throw new IllegalArgumentException("Unknown event instance " + event + ", class " + event.getClass());
    }
  }
}
