public class Main 
{

    private static final int numThreads = 4;
    private static final int incrementsPerThread = 5;

    public static void main(String[] args) throws InterruptedException {

        System.out.println("==== Filter Lock ====");
        runPractical(new FilterLock(numThreads));

        System.out.println();

        System.out.println("==== Bakery Lock ====");
        runPractical(new BakeryLock(numThreads));

        System.out.println();
    }

    private static void runPractical(Lock lock) throws InterruptedException 
    {

        Counter counter = new Counter();
        Thread[] threads = new Thread[numThreads];

        for(int i = 0; i < numThreads; i++) 
        {

            final int threadId = i;

            threads[i] = new Thread(() -> {

                for (int j = 1; j <= incrementsPerThread; j++) {

                    System.out.println("Thread " + threadId + " attempting to acquire lock...");
                    lock.lock(threadId);

                    try {
                        counter.increment();

                        System.out.println("Thread " + threadId + " acquired the lock -> counter = " + counter.getValue() + ". Thread " + threadId + " " + TaskGenerator.getRandomTask());

                        try
                        {
                            Thread.sleep(1000);
                        }
                        catch(InterruptedException e) 
                        {
                            Thread.currentThread().interrupt();
                        }

                    }
                    finally 
                    {
                        lock.unlock(threadId);
                    }
                }
            });
        }

        for(Thread thread : threads)
        {
            thread.start();
        }

        for(Thread thread : threads) 
        {
            thread.join();
        }

        int expected = numThreads * incrementsPerThread;
        System.out.println();
        System.out.println("Expected: " + expected);
        System.out.println("Actual:   " + counter.getValue());
    }
}