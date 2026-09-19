/*
Runs the bidder threads for one lock and one thread count, and times everything needed for the
Task 3 experiment while it goes, avoids a second pass over the data later.

textbook ref: Herlihy and Shavit, The Art of Multiprocessor Programming, Revised 1st Ed,
Morgan Kaufmann 2012, Chapter 7, Spin Locks and Contention. TTAS is 7.3, CLH is 7.5.2, MCS is 7.5.3.

*/

import java.util.Random;

public class Runner {

  public final int numberOfThreads;
  public final int iterations;
  public final Auction auction;
  public final Lock lock;

  /*
  one slot per thread, thread i only ever writes index i so there's no race here, don't need a
  lock around these arrays
  */
  private final long[] waitTimeSum;
  private final long[] waitTimeMax;
  private final long[] acquisitions;
  private final long[] bidsWon;

  private long executionTime;

  public Runner(int numberOfThreads, int iterations, Auction auction, Lock lock) {
    this.numberOfThreads = numberOfThreads;
    this.iterations = iterations;
    this.auction = auction;
    this.lock = lock;

    this.waitTimeSum = new long[numberOfThreads];
    this.waitTimeMax = new long[numberOfThreads];
    this.acquisitions = new long[numberOfThreads];
    this.bidsWon = new long[numberOfThreads];
  }

  public void run() throws InterruptedException {
    Thread[] threads = new Thread[numberOfThreads];

    for (int i = 0; i < numberOfThreads; i++) {
      final int bidderId = i;
      threads[i] = new Thread(() -> bidder(bidderId));
    }

    long startTime = System.nanoTime();

    for (Thread thread : threads) {
      thread.start();
    }

    for (Thread thread : threads) {
      thread.join();
    }

    long endTime = System.nanoTime();
    executionTime = endTime - startTime;
  }

  /*
  read the current highest bid, work out a new bid that's higher, submit it, all under the same
  lock acquisition. if the read and write weren't in the same critical section two threads could
  read the same highest bid and the slower write just overwrites the other one's bid, lost update,
  so the whole read/compute/write chunk has to be atomic, not just the write.

  timing the lock() call itself separately from the bidding logic, and taking the timestamps
  outside the try block so the timing doesn't add overhead inside the critical section.
  */
  public void bidder(int bidderId) {
    Random random = new Random();

    for (int i = 0; i < iterations; i++) {
      long before = System.nanoTime();
      lock.lock();
      long waited = System.nanoTime() - before;

      try {
        double currentHighest = auction.getHighestBid();
        double newBid = currentHighest + 1 + random.nextDouble() * 10;
        auction.placeBid(bidderId, newBid);

        if (auction.getHighestBidder() == bidderId) {
          bidsWon[bidderId]++;
        }
      } finally {
        lock.unlock();
      }

      waitTimeSum[bidderId] += waited;
      acquisitions[bidderId]++;
      if (waited > waitTimeMax[bidderId]) {
        waitTimeMax[bidderId] = waited;
      }
    }
  }

  /*
  accessors below, used by Experiment.java (and Main.java) to pull the numbers out for the
  report without reaching into the private arrays directly
  */

  public double executionTimeMs() {
    return executionTime / 1_000_000.0;
  }

  public long totalBids() {
    long total = 0;
    for (long b : bidsWon) {
      total += b;
    }
    return total;
  }

  public long totalAcquisitions() {
    long total = 0;
    for (long a : acquisitions) {
      total += a;
    }
    return total;
  }

  /*
  average time a thread spends waiting to get the lock, across every acquisition, this is the
  extra measurement the spec asks for in section 6.1, basically the cost of the lock protocol
  itself, separate from the actual bidding work
  */
  public double averageWaitMicros() {
    long sum = 0;
    for (long w : waitTimeSum) {
      sum += w;
    }
    return (sum / (double) totalAcquisitions()) / 1000.0;
  }

  /*
  worst wait time across all threads, not per thread, good for seeing whether a lock lets one
  thread get properly unlucky under contention (TTAS does this, queue locks shouldn't)
  */
  public double maxWaitMicros() {
    long max = 0;
    for (long w : waitTimeMax) {
      if (w > max) {
        max = w;
      }
    }
    return max / 1000.0;
  }

  /*
  gap between the bidder who won the most and the one who won the least, quick fairness check,
  big gap means some threads are getting starved while others keep winning
  */
  public long fairnessGap() {
    long min = Long.MAX_VALUE;
    long max = Long.MIN_VALUE;
    for (long b : bidsWon) {
      min = Math.min(min, b);
      max = Math.max(max, b);
    }
    return max - min;
  }

  public long[] bidsWon() {
    return bidsWon;
  }

  /* prints one run's results, used for sanity checks and the demo */
  public void reportResults() {
    System.out.println("Item: " + auction.getItemName());
    System.out.println("Winning Bid: " + String.format("%.2f", auction.getHighestBid()));
    System.out.println("Winning Bidder: " + auction.getHighestBidder());
    System.out.println("Threads: " + numberOfThreads + ", iterations each: " + iterations);
    System.out.println("Execution Time (ms): " + String.format("%.2f", executionTimeMs()));
  }
}