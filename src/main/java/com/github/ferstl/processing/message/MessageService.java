package com.github.ferstl.processing.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

// Not thread-safe!
public class MessageService {

  private final Marshaller marshaller;
  private final Unmarshaller unmarshaller;

  public MessageService() {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(Payment.class);
      this.marshaller = jaxbContext.createMarshaller();
      this.unmarshaller = jaxbContext.createUnmarshaller();
    } catch (JAXBException e) {
      throw new RuntimeException(e);
    }
  }

  public Payment readPayment(byte[] xml) {
    try {
      return (Payment) this.unmarshaller.unmarshal(new ByteArrayInputStream(xml));
    } catch (JAXBException e) {
      throw new RuntimeException(e);
    }
  }

  public byte[] writePayment(Payment payment) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try {
      this.marshaller.marshal(payment, outputStream);
    } catch (JAXBException e) {
      throw new RuntimeException(e);
    }
    return outputStream.toByteArray();
  }
}
