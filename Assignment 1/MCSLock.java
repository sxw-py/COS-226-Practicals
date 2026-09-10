import java.util.concurrent.atomic.AtomicReference;

public class MCSLock implements Lock {

    // Node representing a waiting thread in the queue
    static class QNode {
        volatile boolean locked = false; // True if waiting for the lock
        volatile QNode next = null; // Next thread in line
    }

    AtomicReference<QNode> tail;
    ThreadLocal<QNode> myNode;

    public MCSLock() {
        tail = new AtomicReference<QNode>(null);
        myNode = new ThreadLocal<QNode>() {
            protected QNode initialValue() {
                return new QNode();
            }
        };
    }

    public void lock() {
        QNode qnode = myNode.get();
        // Add ourselves to the end of the queue
        QNode pred = tail.getAndSet(qnode);

        // If queue wasn't empty, we must wait our turn
        if (pred != null) {
            qnode.locked = true;
            pred.next = qnode; // Link ourselves to our predecessor

            // Spin locally until our predecessor hands us the lock
            while (qnode.locked) {
            }
        }
    }

    public void unlock() {
        QNode qnode = myNode.get();

        if (qnode.next == null) {
            if (tail.compareAndSet(qnode, null))
                return; // Successfully emptied, we are done

            // Another thread is joining but hasn't linked to thread yet; wait for them
            while (qnode.next == null) {
            }
        }

        // Hand the lock over to successor
        qnode.next.locked = false;
        qnode.next = null;
    }
}
