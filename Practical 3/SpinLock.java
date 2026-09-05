public interface SpinLock {
    void lock();
    void unlock();

    //how many times testAndSet() got invoked so far
    long getTestAndSetCount();
}