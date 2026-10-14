/*
COS 226; Assignment 2: Concurrent Queues and Producer-Consumer Systems
Reference: Herlihy, M. and Shavit, N. (2012). The Art of Multiprocessor Programming,
Revised First Edition. Morgan Kaufmann (Elsevier). Chapter 10, Sections 10.3 and 10.5.

runs a producer-consumer system on a shared queue (BoundedQueue or LockFreeQueue).
Producers split the total jobs between them and enq() Job objects; consumers deq() jobs and count them in a shared AtomicInteger until all jobs are processed. After the producers finish,
one null "poison pill" per consumer is enqueued so that consumers blocked on an empty queue can exit. The program then prints the jobs processed, execution time and throughput.
Usage: java Main <bounded|lockfree> <producers> <consumers> (with no arguments it runs the lock-free queue with 4 producers and 4 consumers)
*/

import java.util.concurrent.atomic.AtomicInteger;
 
public class Main{
    public static void main(String[] args) throws InterruptedException {
        int totalJobs = 12000;
        // optional args: <bounded|lockfree> <producers> <consumers>
        String queueType = args.length == 3 ? args[0] : "lockfree";
        int numProducers = args.length == 3 ? Integer.parseInt(args[1]) : 4;
        int numConsumers = args.length == 3 ? Integer.parseInt(args[2]) : 4;
 
        int jobsPerProducer = totalJobs / numProducers;
        AtomicInteger processedCount = new AtomicInteger(0);
 
        //test with BoundedQueue or LockfreeQueue
        Object queue = queueType.equals("bounded") ? new BoundedQueue<Job>(100) : new LockFreeQueue<Job>();
 
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
 