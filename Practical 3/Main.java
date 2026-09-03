public class Main 
{

    private static final int NUMBER_OF_THREADS = 2;
    private static final int INCREMENTS_PER_THREAD = 1000000;
    private static int counter = 0;

    public static void main(String[] args) throws InterruptedException 
    {

        TASLock lock = new TASLock();/*Your lock implementation here (You may also swap out the TAS lock for your optimised lock here)*/
        Thread[] threads = new Thread[NUMBER_OF_THREADS];
        long startTime = System.nanoTime();

        for(int i = 0; i < NUMBER_OF_THREADS; i++) 
        {

            threads[i] = new Thread(() -> {

                for(int j = 0; j < INCREMENTS_PER_THREAD; j++) 
                {
                    lock.lock();
                    counter++;
                    lock.unlock();
                }
            });

            threads[i].start();
        }

        for(Thread thread : threads) 
        {
            thread.join();
        }

        long endTime = System.nanoTime();

        System.out.println("Expected counter: " + (NUMBER_OF_THREADS * INCREMENTS_PER_THREAD));
        System.out.println("Actual counter: " + counter);
        System.out.println("Execution time: " + (endTime - startTime) / 1000000 + " ms");
    }
}