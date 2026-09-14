import java.util.function.Supplier;
 
/* task 3: the actual experiment. running both locks at 2/4/8/16/32 threads, 5 runs each, same total workload every time so it's a fair comparison.
tracking avg time + avg testAndSet() calls per config like the spec wants.
basically trying to reproduce fig 7.4 from the textbook (TASLock curve shoots up way faster than TTASLock as threads increase, since TAS keeps hammering the bus with every single testAndSet() while TTAS mostly just
spins locally in cache and only touches the bus when the lock looks free) run directly: java ContentionExperiment
*/

public class ContentionExperiment
{
    private static final int[] THREAD_COUNTS = {2, 4, 8, 16, 32};
    private static final int RUNS_PER_CONFIG = 5;
 
    //same total work no matter how many threads, just split differently so we're comparing apples to apples across configs
    private static final int TOTAL_INCREMENTS = 8_000_000;
 
    public static void main(String[] args)
    {
        run();
    }
 
    public static void run()
    {
        System.out.println("Implementation | Threads | Avg Time (ms) | Avg testAndSet() calls");
        System.out.println("---------------|---------|---------------|-----------------------");
 
        for (int threads : THREAD_COUNTS)
        {
            printRowFor("TASLock", TASLock::new, threads);
        }
        for (int threads : THREAD_COUNTS)
        {
            printRowFor("TTASLock", TTASLock::new, threads);
        }
    }
 
    private static void printRowFor(String name, Supplier<SpinLock> lockFactory, int threads)
    {
        long totalTime = 0;
        long totalTasCalls = 0;
 
        //just running it 5x and averaging like the spec asks, single runs are too noisy (OS scheduling, JVM warmup etc, book mentions this too)
        for (int run = 0; run < RUNS_PER_CONFIG; run++)
        {
            Result r = singleRun(lockFactory.get(), threads);
            totalTime += r.elapsedMillis;
            totalTasCalls += r.tasCount;
        }
 
        double avgTime = totalTime / (double) RUNS_PER_CONFIG;
        double avgTas = totalTasCalls / (double) RUNS_PER_CONFIG;
 
        System.out.printf("%-14s | %7d | %13.2f | %.2f%n", name, threads, avgTime, avgTas);
    }
 
    private static Result singleRun(SpinLock lock, int numThreads)
    {
        int incrementsPerThread = TOTAL_INCREMENTS / numThreads;
        Counter counter = new Counter();
 
        Thread[] threads = new Thread[numThreads];
 
        long startTime = System.nanoTime();
 
        for (int i = 0; i < numThreads; i++)
        {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < incrementsPerThread; j++)
                {
                    lock.lock();
                    counter.value++;
                    lock.unlock();
                }
            });
            threads[i].start();
        }
 
        for (Thread t : threads)
        {
            try { t.join(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
 
        long endTime = System.nanoTime();
        long elapsedMillis = (endTime - startTime) / 1_000_000;
 
        //if this ever fails the lock is broken
        int expected = incrementsPerThread * numThreads;
        if (counter.value != expected)
        {
            System.err.println("WARNING: mutual exclusion violated! expected=" + expected + " actual=" + counter.value);
        }
 
        return new Result(elapsedMillis, lock.getTestAndSetCount());
    }
 
    //plain int, not atomic, deliberately, since correctness should come entirely from the lock, not from making the counter itself thread-safe
    private static class Counter
    {
        int value = 0;
    }
 
    private static class Result
    {
        final long elapsedMillis;
        final long tasCount;
 
        Result(long elapsedMillis, long tasCount)
        {
            this.elapsedMillis = elapsedMillis;
            this.tasCount = tasCount;
        }
    }
}
 