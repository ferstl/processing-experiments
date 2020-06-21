package com.github.ferstl.processing.message;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.file.Paths;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JaxbTests {

  @Test
  void test() throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(Payment.class);
    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
    Payment payment = (Payment) unmarshaller.unmarshal(Paths.get("src/test/resources/payment.xml").toFile());
    assertEquals("12345", payment.getDebtorAccount());
    assertEquals("56789", payment.getCreditorAccount());
    assertEquals(new BigDecimal("100.25"), payment.getAmount());

    Marshaller marshaller = jaxbContext.createMarshaller();
    StringWriter writer = new StringWriter();
    marshaller.marshal(payment, writer);
  }
}
