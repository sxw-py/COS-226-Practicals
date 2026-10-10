import java.util.concurrent.atomic.AtomicInteger;

/* Consumer thread responsible for continuously removing jobs from the shared queue
  using deq() , processing them, and tracking the total processed count until completion. */

public class Consumer implements Runnable{
    private final Object queue;
    private final AtomicInteger processedCount;
    private final int totalJobsToProcess;

    public Consumer(Object queue, AtomicInteger processedCount, int totalJobsToProcess) {
        this.queue = queue;
        this.processedCount = processedCount;
        this.totalJobsToProcess = totalJobsToProcess;
    }

    @Override
    public void run() {
        while (processedCount.get() < totalJobsToProcess) {
            Job job = null;
                try {
                    if (queue instanceof BoundedQueue) {
                        //BoundedQueue dep blocks/wait when empty
                        job = ((BoundedQueue<Job>) queue).deq();
                    } else if (queue instanceof LockFreeQueue) {
                        try{
                            job = ((LockFreeQueue<Job>) queue).deq();
                        } catch (EmptyException e) {
                            // Queue is currently empty, yield or continue to retry
                            Thread.yield();
                            continue;
                        }  
                    }
                    if (job == null){
                        break;
                    }
                    processedCount.incrementAndGet();
                
                }catch (Exception e){
                    //catches any other general exceptions
                    e.printStackTrace();
                    break;
            }
        }
    }

}