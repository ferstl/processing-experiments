package com.github.ferstl.processing.application;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.github.ferstl.processing.message.Payment;

@RestController
public class MessagingController {

  @PostMapping(value = "/payment", consumes = MediaType.APPLICATION_XML_VALUE)
  public void submit(@RequestBody Payment payment) {

  }
}
