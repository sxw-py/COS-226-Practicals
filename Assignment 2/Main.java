import java.util.concurrent.atomic.AtomicInteger;

public class Main{
    public static void main(String[] args) throws InterruptedException {
        int totalJobs = 12000;
        int numProducers = 4;
        int numConsumers = 4;

        int jobsPerProducer = totalJobs / numProducers;
        AtomicInteger processedCount = new AtomicInteger(0);

        //test with BoundedQueue or LockfreeQueue
        //Object queue = new BoundedQueue<>(100);
        Object queue = new LockFreeQueue<>();

        Thread[] producers = new Thread[numProducers];
        Thread[] consumers = new Thread[numConsumers];

        long startTime = System.currentTimeMillis();

        //start producers
        for (int i = 0; i < numProducers; i++) {
            producers[i] = new Thread(new Producer(queue, jobsPerProducer, i));
            producers[i].start();
        }

        //start consumers
        for (int i = 0; i < numConsumers; i++) {
            consumers[i] = new Thread(new Consumer(queue, processedCount, totalJobs));
            consumers[i].start();
        }

        //Wait for all producers to finish
        for (int i = 0; i < numProducers; i++) {
            producers[i].join();
        }

        for (int i = 0; i < numConsumers; i++) {
            if (queue instanceof BoundedQueue) {
                ((BoundedQueue<Job>) queue).enq(null);
            } else if (queue instanceof LockFreeQueue) {
                ((LockFreeQueue<Job>) queue).enq(null);
            }
        }

        //join consumer threads to ensure clean shutdown before exiting
        for (int i=0 ; i<numConsumers; i++){
            consumers[i].join();
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        double throughput = (double) totalJobs / (duration / 1000.0);

        System.out.println("Execution completed successfully.");
        System.out.println("Total jobs processed: " + processedCount.get());
        System.out.println("Execution Time: " + duration + " ms");
        System.out.println("Throughput: " + throughput + " jobs/sec");

    }
}
