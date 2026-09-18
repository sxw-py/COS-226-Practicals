import java.util.concurrent.atomic.AtomicLong;
import java.util.Random;
/*Optional Helper Runner Class*/
public class Runner 
{

    public final int numberOfThreads;
    public final int iterations;
    public final Auction auction;
    public final Lock lock;

    private final AtomicLong totalWaitingTime = new AtomicLong(0);

    public Runner(int numberOfThreads,int iterations,Auction auction,Lock lock) 
    {
        this.numberOfThreads = numberOfThreads;
        this.iterations = iterations;
        this.auction = auction;
        this.lock = lock;
    }

    public void run() throws InterruptedException 
    {
        Thread[] threads = new Thread[numberOfThreads];

        for(int i = 0; i < numberOfThreads; i++) 
        {
            final int bidderId = i;

            threads[i] = new Thread(() -> {
                bidder(bidderId);
            });
        }

        long startTime = System.nanoTime();

        for(Thread thread : threads) 
        {
            thread.start();
        }

        for(Thread thread : threads) 
        {
            thread.join();
        }

        long endTime = System.nanoTime();

        reportResults(endTime - startTime);
    }

    /*Defines the behaviour of an individual bidder. Note you have to decide how to incorporate your lock.*/
    public void bidder(int bidderId) 
    {
        Random random = new Random();

        for (int i=0; i < iterations; i++){
            lock.lock();
            try{
                //if another thread's placeBid is higher than previous but lower than the newest one
                //the next bid must be based off the highest bid value to ensure that no attempt is wasted
                double currentHighest = auction.getHighestBid();
                double newBid = currentHighest + 1 + random.nextDouble() * 10;
                auction.placeBid(bidderId, newBid);
            }
            finally 
            {
                lock.unlock();
            }
        }
       
    }

    /*Optional Helper: Records and reports the results of the experiment.*/
    public void reportResults(long executionTime) 
    {
        System.out.println("Item: " +  auction.getItemName());
        System.out.println("Winning Bid: " +  String.format("%.2f",auction.getHighestBid()));
        System.out.println("Winning Bidder: " + auction.getHighestBidder());
        System.out.println( "Threads: " + numberOfThreads + ", iterations each: " + iterations);
        System.out.println("Execution Time (ms): " + String.format("%.2f",executionTime / 1_000_000.0));
    }
}