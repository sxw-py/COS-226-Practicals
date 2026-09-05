/*
Just the basic demo, it proves the lock works(counter comes out exact) no race condition
switch between TAS/TTASLock be low to test each one
full contention experiment task 3 lives in Contention experiment.java
*/
public class Main 
{

    private static final int NUMBER_OF_THREADS = 4;
    private static final int INCREMENTS_PER_THREAD = 1000000 ; 
    private static int counter = 0;

    public static void main(String[] args) throws InterruptedException 
    {
        //Task 1: TASLock
        SpinLock lock = new TASLock();  
        
        //Task 2: TTASLock (optimized)
        //SpinLock lock = new TTASLock();
        Thread[] threads = new Thread[NUMBER_OF_THREADS];
        long startTime = System.nanoTime();

        for(int i = 0; i < NUMBER_OF_THREADS; i++) 
        {
           
            final int threadId = i;
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

        //if actual != expected here, mutual exclusion is broken, shouldn't happen
        System.out.println("Expected counter: " + (NUMBER_OF_THREADS * INCREMENTS_PER_THREAD));
        System.out.println("Actual counter: " + counter);
        System.out.println("Execution time: " + (endTime - startTime) / 1000000 + " ms");

        //run this for actual task 3 result table, //contentionExperiment.run();
    }
}