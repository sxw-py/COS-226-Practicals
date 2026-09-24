import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BoundedQueue<T> {
    ReentrantLock enqLock, deqLock;
    Condition notEmptyCondition, notFullCondition;
    AtomicInteger size;
    volatile Node head, tail;
    int capacity;

    public BoundedQueue(int _capacity) {
        capacity = _capacity;
        head = new Node(null);
        tail = head;
        size = new AtomicInteger(0);
        enqLock = new ReentrantLock();
        notFullCondition = enqLock.newCondition();
        deqLock = new ReentrantLock();
        notEmptyCondition = deqLock.newCondition();
    }

    public void enq(T x) {
        boolean mustWakeDequeuers = false;
        enqLock.lock();
        try {
            while (size.get() == capacity) {
                try {
                    notFullCondition.await();
                } catch (InterruptedException e) {
                    // Handle interruption
                }
            }
            Node eNode = new Node(x);
            tail.next = eNode; // Wait, textbook says: tail.next = tail; tail = e; That's a typo in the book!
            // Correct logic: tail.next = eNode; tail = eNode;
            tail = eNode;
            if (size.getAndIncrement() == 0) {
                mustWakeDequeuers = true;
            }
        } finally {
            enqLock.unlock();
        }
        if (mustWakeDequeuers) {
            deqLock.lock();
            try {
                notEmptyCondition.signalAll();
            } finally {
                deqLock.unlock();
            }
        }
    }

    public T deq() {
        T result = null;
        boolean mustWakeEnqueuers = false;
        deqLock.lock();
        try {
            while (size.get() == 0) {
                try {
                    notEmptyCondition.await();
                } catch (InterruptedException e) {
                    // Handle interruption
                }
            }
            result = head.next.value;
            head = head.next;
            if (size.getAndDecrement() == capacity) {
                mustWakeEnqueuers = true;
            }
        } finally {
            deqLock.unlock();
        }
        if (mustWakeEnqueuers) {
            enqLock.lock();
            try {
                notFullCondition.signalAll();
            } finally {
                enqLock.unlock();
            }
        }
        return result;
    }

    protected class Node {
        public T value;
        public volatile Node next;

        public Node(T x) {
            value = x;
            next = null;
        }
    }
}
