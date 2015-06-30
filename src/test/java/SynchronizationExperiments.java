import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import org.junit.Test;

public class SynchronizationExperiments {

  @Test
  public void readWriteLock() {
    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    ReadLock readLock = lock.readLock();
    WriteLock writeLock = lock.writeLock();
    CountDownLatch latch = new CountDownLatch(1);
    ExecutorService pool = Executors.newFixedThreadPool(3);

    pool.submit(() -> {
      work(readLock, latch, "submitting");
    });

    pool.submit(() -> {
      work(readLock, latch, "settling");
    });

    pool.submit(() -> {
      system(writeLock, latch);
    });

    System.out.println("start processing");
    latch.countDown();

    try {
      TimeUnit.SECONDS.sleep(30);
    } catch (InterruptedException e) {
      // NOP
    }

    pool.shutdownNow();
    try {
      pool.awaitTermination(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      // NOP
    }
  }

  private void work(ReadLock readLock, CountDownLatch latch, String message) throws RuntimeException {
    try {
      latch.await();
    } catch (Exception e) {
      throw new RuntimeException();
    }

    while (!Thread.currentThread().isInterrupted()) {

      try {
        readLock.lock();
        System.out.println(message);
        try {
          TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
          return;
        }

      } finally {
        readLock.unlock();
      }

    }
  }

  private void system(WriteLock writeLock, CountDownLatch latch) throws RuntimeException {
    try {
      latch.await();
    } catch (Exception e) {
      throw new RuntimeException();
    }

    while (!Thread.currentThread().isInterrupted()) {
      try {
        TimeUnit.SECONDS.sleep(1);
      } catch (InterruptedException e) {
        return;
      }

      try {
        System.out.println("start doing system work");
        writeLock.lock();
        System.out.println("system work");
        try {
          TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException e) {
          return;
        }

      } finally {
        System.out.println("system work done");
        writeLock.unlock();
      }

    }
  }

}
