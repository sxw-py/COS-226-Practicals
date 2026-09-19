/*
Runs one single auction with whichever lock you pick, mostly for the demo and for sanity checking
a lock on its own before throwing it into the full Task 3 sweep (see Experiment.java for that).

usage: java Main [TTAS|CLH|MCS] [threads] [iterations]
defaults to CLH, 4 threads, 200 iterations if you don't pass anything

textbook ref: Herlihy and Shavit, The Art of Multiprocessor Programming, Revised 1st Ed,
Morgan Kaufmann 2012, Chapter 7
*/

public class Main {

  public static void main(String[] args) throws InterruptedException {
    String lockName = args.length > 0 ? args[0].toUpperCase() : "CLH";
    int numberOfThreads = args.length > 1 ? Integer.parseInt(args[1]) : 4;
    int iterations = args.length > 2 ? Integer.parseInt(args[2]) : 200;

    Lock lock;
    switch (lockName) {
      case "TTAS":
        lock = new TTASLock();
        break;
      case "CLH":
        lock = new CLHLock();
        break;
      case "MCS":
        lock = new MCSLock();
        break;
      default:
        throw new IllegalArgumentException("Unknown lock: " + lockName);
    }

    Auction auction = new Auction(AuctionUtils.generateItemName());
    Runner runner = new Runner(numberOfThreads, iterations, auction, lock);
    runner.run();

    System.out.println("Lock: " + lockName);
    runner.reportResults();

    /*
    with a correct lock every bid should raise the highest bid, since newBid is always
    currentHighest + something positive and that whole read/write happens under the lock, so
    total bids has to equal threads * iterations exactly. anything less means a lost update
    somewhere and the lock isn't actually giving mutual exclusion
    */
    long expected = (long) numberOfThreads * iterations;
    System.out.println("Total bids placed: " + runner.totalBids() + " (expected " + expected + ")");
    System.out.println("Average lock wait (us): " + String.format("%.3f", runner.averageWaitMicros()));
    System.out.println("Max lock wait (us): " + String.format("%.3f", runner.maxWaitMicros()));
  }
}