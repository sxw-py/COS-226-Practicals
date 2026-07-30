public class Main {
    // LockOne Implementation:
    static int counter = 0; // shared memory (critical section)
    static LockOne lock1 = new LockOne();

    static void doWork(int myID) {
        for (int i = 0; i < 5; ++i) {
            lock1.lock(myID); // get lock before touching critical section
            ++counter;
            System.out.println("ThreadID: " + myID + "     counter: " + counter);
            lock1.unlock(myID);
        } // must unlock immediately after critical section or else on iteration 2 the
          // other thread might be already waiting causing 2 true flags = Deadlock
    }

    // ****Other two locks can go below this comment (implementation)****

    public static void main(String[] args) throws InterruptedException {
        // LockOne Main
        Thread t0 = new Thread(() -> doWork(0));
        Thread t1 = new Thread(() -> doWork(1));
        // Let threads start executing
        t0.start();
        t1.start();
        // tells main to wait for the threads to finish before it terminates
        t0.join();
        t1.join();

    }

    // ****Testing Main for other two locks can go below this comment*****

}
