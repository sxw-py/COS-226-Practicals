/*Producer thread responsible fo rgenerating a designated share of jobs and inserting
 them into the shared queue using the enq() operation */

public class Producer implements Runnable {
    private final Object queue; //can accept either boundedQueue<Job> or LockFreeQueue<Job>
    private final int jobsToProduce;
    private final int producerId;

    public Producer(Object queue, int jobsToProduce, int producerId) {
        this.queue = queue;
        this.jobsToProduce = jobsToProduce;
        this.producerId = producerId;
    }

    @Override
    public void run() {
        try{
            for (int i = 0; i < jobsToProduce; i++) {
            int jobId = producerId * 100000 + i; //Generates a unique ID
            Job job = new Job(jobId);

            //Invoke enq depending on queue type
            if (queue instanceof BoundedQueue) {
                ((BoundedQueue<Job>) queue).enq(job);
             }else if (queue instanceof LockFreeQueue) {
                ((LockFreeQueue<Job>) queue).enq(job);
          }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
  }
        
}