package com.github.ferstl.processing.journal;

import java.util.concurrent.BlockingQueue;
import java.util.function.Supplier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import com.github.ferstl.processing.ProcessingConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ProcessingConfiguration.class)
public class JournalProcessorTest {

  @Autowired
  private JournalProcessor inboundProcessor;

  @Autowired
  private Supplier<BlockingQueue<String>> inboundQueueSupplier;

  private BlockingQueue<String> inboundQueue;

  @Before
  public void before() {
    this.inboundQueue = this.inboundQueueSupplier.get();
    this.inboundProcessor.start();
  }

  @After
  public void after() {
    this.inboundProcessor.stop();
  }

  @Test
  public void test() throws InterruptedException {
    for (int i = 0; i < 1000; i++) {
      this.inboundQueue.add("Test");
    }

    Thread.sleep(5000);
  }

}
