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



    //PetersonsLock Implementation
    //im doing this similar to the existing implementation of LockOne, but with PetersonsLock
    //but please refer to textbook page 24-27

    static int counterPeterson =0; //shared memory(critical selection)
    static PetersonsLock lockPeterson = new PetersonsLock();

    static void doWorkPeterson(int myID){
        for(int i=0; i<5; ++i){
            lockPeterson.lock(myID); //get lock before even touching critical selection
            ++counterPeterson;
            System.out.println("ThreadId: " + myID+ "   counterPeterson: " +counterPeterson);
            lockPeterson.unlock(myID);
        }
    }

    // LockTwo implementation

    static int counterTwo = 0;
    static LockTwo lock2 = new LockTwo();

    static void doWorkTwo(int myID){
        for (int i=0; i<5; ++i){
            lock2.lock(myID);
            ++counterTwo;
            System.out.println("Thread ID: " + myID + "   counter two: " + counterTwo);
            lock2.unlock();
        }
    }


    public static void main(String[] args) throws InterruptedException {
        // LockOne Main
        System.out.println("---- Lock One----");
        Thread t0 = new Thread(() -> doWork(0));
        Thread t1 = new Thread(() -> doWork(1));
        // Let threads start executing
        t0.start();
        t1.start();
        // tells main to wait for the threads to finish before it terminates
        t0.join();
        t1.join();
        
        //PetersonsLock Main
        System.out.println("\n ---- Petersons Lock ----");
        Thread p0 = new Thread(() -> doWorkPeterson(0));
        Thread p1 = new Thread(() -> doWorkPeterson(1));
        p0.start();
        p1.start();
        //tells main to wait for the threads to finish before it terminates
        p0.join();
        p1.join();

        //LockTwo Main
        System.out.println("\n ---- Lock Two ----");
        Thread t2 = new Thread(() -> doWorkTwo(0));
        Thread t3 = new Thread(() -> doWorkTwo(1));
        t2.start();
        t3.start();
        t2.join();
        t3.join();

    }

}
