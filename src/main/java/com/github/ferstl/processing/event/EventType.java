package com.github.ferstl.processing.event;

public enum EventType {
  RESERVATION,
  SETTLEMENT;

  public static <T extends ProcessingEvent> EventType forEvent(T event) {
    if (event instanceof ReservationEvent) {
      return RESERVATION;
    } else {
      throw new IllegalArgumentException("Unknown event instance " + event + ", class " + event.getClass());
    }
  }
}
