/*
Reference:
  Herlihy, M. and Shavit, N., "The Art of Multiprocessor Programming",
  Chapter 9 - Linked Lists: The Role of Locking
    - Section 9.3 Coarse-Grained Synchronization (basis for CoarseList)
    - Section 9.4 Fine-Grained Synchronization (node-level locks)
    - Section 9.5 Optimistic Synchronization (hand-over-hand traversal
      pattern used before validation, referenced for the lock-then-check structure used in FineList)

    This file enchmarks CoarseList against FineList under identical mixed add/contains/remove workloads
    at increasing thread counts, to compare the trade-off between locking overhead and achievable concurrency.
*/

public class Experiment {

    // small wrapper interface so the same benchmarking loop works for both
    // list types without changing CoarseList or FineList themselves
    interface ListOps {
        void add(int value);
        void remove(int value);
        void contains(int value);
    }

    public static void main(String[] args) throws InterruptedException {

        int operationsPerThread = 1000;
        int[] threadCounts = {2, 4, 8, 16};

        System.out.println("Threads\tCoarse-Grained (ms)\tFine-Grained (ms)");

        for (int numThreads : threadCounts) {

            // fresh list per run, otherwise leftover state from the
            // previous run skews the timing (e.g. list already full of values)
            CoarseList coarseList = new CoarseList();
            FineList fineList = new FineList();

            long coarseTime = runBenchmark(coarseOps(coarseList), numThreads, operationsPerThread);
            long fineTime = runBenchmark(fineOps(fineList), numThreads, operationsPerThread);

            System.out.println(numThreads + "\t" + coarseTime + "\t\t\t" + fineTime);
        }
    }

    // runs the same mixed add/contains/remove workload as the original Main,
    // just parameterised on thread count so we can loop over configurations
    private static long runBenchmark(ListOps list, int numThreads, int operationsPerThread) throws InterruptedException {

        Thread[] threads = new Thread[numThreads];
        long startTime = System.nanoTime();

        for (int i = 0; i < numThreads; i++) {
            final int threadID = i;

            threads[i] = new Thread(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    int value = (threadID * 1000) + (j % 1000);

                    if (j % 3 == 0) {
                        list.add(value);
                    } else if (j % 3 == 1) {
                        list.contains(value);
                    } else {
                        list.remove(value);
                    }
                }
            });

            threads[i].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        long endTime = System.nanoTime();
        return (endTime - startTime) / 1000000; // convert to ms
    }

    // adapters so CoarseList and FineList can both be passed as ListOps
    // without needing to modify either class to implement an interface
    private static ListOps coarseOps(CoarseList list) {
        return new ListOps() {
            public void add(int value) { list.add(value); }
            public void remove(int value) { list.remove(value); }
            public void contains(int value) { list.contains(value); }
        };
    }

    private static ListOps fineOps(FineList list) {
        return new ListOps() {
            public void add(int value) { list.add(value); }
            public void remove(int value) { list.remove(value); }
            public void contains(int value) { list.contains(value); }
        };
    }
}