import java.util.concurrent.atomic.AtomicReference;

public class CLHLock implements Lock {

    // QNode is a simple wrapper around a boolean flag.
    // It acts as a node in the implicit linked list of waiting threads.
    static class QNode {
        volatile boolean locked = false;
    }

    AtomicReference<QNode> tail;
    ThreadLocal<QNode> myPred;
    ThreadLocal<QNode> myNode;

    public CLHLock() {
        // Initialize the tail to a dummy node (not null) textbook's code had a typo
        // setting it to null would cause a NullPointerException
        tail = new AtomicReference<QNode>(new QNode());

        myNode = new ThreadLocal<QNode>() {
            protected QNode initialValue() {
                return new QNode();
            }
        }; // Added missing semicolon here

        myPred = new ThreadLocal<QNode>() {
            protected QNode initialValue() {
                return null;
            }
        };
    }

    public void lock() {
        QNode qnode = myNode.get();
        qnode.locked = true;
        // Make myself the new tail, and get the previous tail (my predecessor)
        QNode pred = tail.getAndSet(qnode);
        myPred.set(pred);
        // Spin while my predecessor is locked
        while (pred.locked) {
            // spin
        }
    }

    public void unlock() {
        QNode qnode = myNode.get();
        // Release the lock
        qnode.locked = false;
        // Recycle my predecessor's node for my next lock attempt
        myNode.set(myPred.get());
    }
}