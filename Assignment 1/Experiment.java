/*
Task 3, the actual experiment. Sweeps TTAS/CLH/MCS across 2/4/8/16 threads, repeats each
combination a few times with the same workload, and prints averaged tables plus a raw csv dump.

textbook ref: Herlihy and Shavit, The Art of Multiprocessor Programming, Revised 1st Ed,
Morgan Kaufmann 2012, Chapter 7, Spin Locks and Contention.

usage: java Experiment [iterations] [repeats]
defaults to 10000 iterations, 5 repeats

*/

import java.io.PrintWriter;
import java.util.Arrays;

public class Experiment {

  /* same iteration count across every lock and thread count, keeps the comparison fair */
  private static int ITERATIONS = 10_000;
  private static int REPEATS = 5;

  private static final int[] THREAD_COUNTS = {2, 4, 8, 16};
  private static final String[] LOCK_NAMES = {"TTAS", "CLH", "MCS"};

  private static Lock newLock(String name) {
    switch (name) {
      case "TTAS":
        return new TTASLock();
      case "CLH":
        return new CLHLock();
      case "MCS":
        return new MCSLock();
      default:
        throw new IllegalArgumentException(name);
    }
  }

  public static void main(String[] args) throws Exception {
    if (args.length > 0) ITERATIONS = Integer.parseInt(args[0]);
    if (args.length > 1) REPEATS = Integer.parseInt(args[1]);

    System.out.println("COS 226 Assignment 1, Task 3, lock behaviour under contention");
    System.out.println("Available processors: " + Runtime.getRuntime().availableProcessors());
    System.out.println("JVM: " + System.getProperty("java.version"));
    System.out.println("Iterations per thread: " + ITERATIONS + ", repeats per config: " + REPEATS);
    System.out.println();

    /* warm up so JIT compilation doesn't get charged to whichever lock happens to run first */
    for (String name : LOCK_NAMES) {
      Runner warm = new Runner(2, 1000, new Auction("warmup"), newLock(name));
      warm.run();
    }

    PrintWriter csv = new PrintWriter("results.csv");
    csv.println(
        "lock,threads,run,executionTimeMs,totalBids,finalHighestBid,"
            + "winningBidder,avgWaitUs,maxWaitUs,fairnessGap,bidsWonPerBidder");

    StringBuilder summary = new StringBuilder();
    StringBuilder fairness = new StringBuilder();

    summary.append(
        String.format(
            "%-6s %8s %14s %12s %18s %14s %14s %12s%n",
            "Lock",
            "Threads",
            "Exec time(ms)",
            "Total bids",
            "Final highest bid",
            "Avg wait(us)",
            "Max wait(us)",
            "Fair. gap"));

    for (int threads : THREAD_COUNTS) {
      for (String name : LOCK_NAMES) {
        double sumTime = 0, sumAvgWait = 0, sumMaxWait = 0, sumHighest = 0, sumGap = 0;
        long sumBids = 0;
        double[] wonAvg = new double[threads];

        for (int run = 1; run <= REPEATS; run++) {
          Auction auction = new Auction(AuctionUtils.generateItemName());
          Lock lock = newLock(name); /* fresh lock every run, don't want state carrying over */
          Runner runner = new Runner(threads, ITERATIONS, auction, lock);
          runner.run();

          sumTime += runner.executionTimeMs();
          sumBids += runner.totalBids();
          sumHighest += auction.getHighestBid();
          sumAvgWait += runner.averageWaitMicros();
          sumMaxWait += runner.maxWaitMicros();
          sumGap += runner.fairnessGap();

          long[] won = runner.bidsWon();
          for (int i = 0; i < threads; i++) {
            wonAvg[i] += won[i] / (double) REPEATS;
          }

          csv.printf(
              "%s,%d,%d,%.3f,%d,%.2f,%d,%.3f,%.3f,%d,\"%s\"%n",
              name,
              threads,
              run,
              runner.executionTimeMs(),
              runner.totalBids(),
              auction.getHighestBid(),
              auction.getHighestBidder(),
              runner.averageWaitMicros(),
              runner.maxWaitMicros(),
              runner.fairnessGap(),
              Arrays.toString(won));

          /*
          correctness check, see the note in Main.java, bid count should always equal
          threads * iterations exactly if the lock is actually giving mutual exclusion
          */
          long expected = (long) threads * ITERATIONS;
          if (runner.totalBids() != expected) {
            System.out.printf(
                "WARNING: %s/%d threads recorded %d bids, expected %d%n",
                name, threads, runner.totalBids(), expected);
          }
        }

        summary.append(
            String.format(
                "%-6s %8d %14.2f %12d %18.2f %14.3f %14.3f %12.1f%n",
                name,
                threads,
                sumTime / REPEATS,
                sumBids / REPEATS,
                sumHighest / REPEATS,
                sumAvgWait / REPEATS,
                sumMaxWait / REPEATS,
                sumGap / REPEATS));

        fairness.append(
            String.format(
                "%-6s %8d  bids won per bidder (avg of %d runs): %s%n",
                name, threads, REPEATS, format(wonAvg)));
      }
      summary.append('\n');
      fairness.append('\n');
    }

    csv.close();

    System.out.println("=== Table 1: averaged results per lock and thread count ===");
    System.out.println(summary);
    System.out.println("=== Table 2: distribution of winning bids across bidders ===");
    System.out.println(fairness);
    System.out.println("Per run raw data written to results.csv");
  }

  private static String format(double[] values) {
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < values.length; i++) {
      if (i > 0) sb.append(", ");
      sb.append(String.format("%.0f", values[i]));
    }
    return sb.append(']').toString();
  }
}