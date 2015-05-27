package com.github.ferstl.processing;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.function.Supplier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import com.github.ferstl.processing.journal.JournalProcessor;
import com.github.ferstl.processing.model.Payment;
import com.google.gson.Gson;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ProcessingConfiguration.class)
public class ProcessingTest {


  @Autowired
  private JournalProcessor inboundProcessor;

  @Autowired
  private Supplier<BlockingQueue<String>> inboundQueueSupplier;

  @Autowired
  private SubmissionProcessor submissionProcessor;

  @Autowired
  private Gson gson;

  private Random rand;

  private BlockingQueue<String> inboundQueue;

  @Before
  public void before() {
    this.rand = new Random();
    this.inboundQueue = this.inboundQueueSupplier.get();
    this.inboundProcessor.start();
    this.submissionProcessor.start();
  }

  @After
  public void after() {
    this.inboundProcessor.stop();
    this.submissionProcessor.stop();
  }

  @Test
  public void test() throws InterruptedException {
    for (int i = 0; i < 1000; i++) {
      this.inboundQueue.add(this.gson.toJson(createPayment()));
    }

    Thread.sleep(5000);
  }

  private Payment createPayment() {
    Payment p = new Payment();
    p.setFrom("12345");
    p.setTo("56789");
    p.setAmount(new BigDecimal(this.rand.nextInt(100)));
    p.setValueDate(LocalDate.now());

    return p;
  }

}
