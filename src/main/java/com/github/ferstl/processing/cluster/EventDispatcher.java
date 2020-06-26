package com.github.ferstl.processing.cluster;

import org.agrona.DirectBuffer;
import com.github.ferstl.processing.accounting.AccountingResult;
import com.github.ferstl.processing.accounting.AccountingService;
import com.github.ferstl.processing.event.codec.CommunicationEvent;
import com.github.ferstl.processing.event.codec.EventType;
import com.github.ferstl.processing.event.codec.InboundMessage;
import com.github.ferstl.processing.event.codec.Reservation;
import com.github.ferstl.processing.event.codec.codec.CommunicationEventCodec;
import com.github.ferstl.processing.event.codec.codec.InboundMessageCodec;
import com.github.ferstl.processing.event.codec.codec.ReservationCodec;
import com.github.ferstl.processing.messaging.MessagingService;

public class EventDispatcher {

  private final int memberId;
  private final AccountingService accountingService;
  private final MessagingService messagingService;
  private final ReservationCodec reservationCodec;
  private final InboundMessageCodec inboundMessageCodec;
  private final CommunicationEventCodec communicationEventCodec;

  public EventDispatcher(int memberId, AccountingService accountingService, MessagingService messagingService) {
    this.memberId = memberId;
    this.accountingService = accountingService;
    this.messagingService = messagingService;
    this.reservationCodec = new ReservationCodec();
    this.communicationEventCodec = new CommunicationEventCodec();
    this.inboundMessageCodec = new InboundMessageCodec();
  }


  public AccountingResult dispatch(DirectBuffer buffer, int offset) {
    String eventTypeName = buffer.getStringAscii(offset);
    EventType eventType = EventType.valueOf(eventTypeName);

    switch (eventType) {
      case INBOUND_MESSAGE:
        InboundMessage inboundMessage = this.inboundMessageCodec.decode(buffer, offset);
        this.messagingService.handleInboundMessage(inboundMessage);
        return null;
      case RESERVATION:
        Reservation reservation = this.reservationCodec.decode(buffer, offset);
        return this.accountingService.reserve(reservation);
      case COMMUNICATION:
        CommunicationEvent communicationEvent = this.communicationEventCodec.decode(buffer, offset);
        this.messagingService.communicated(communicationEvent);
        // TODO Better Return type
        return null;
      default:
        throw new IllegalArgumentException("Unknown event type: " + eventType);
    }
  }
}
