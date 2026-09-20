import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CoarseList {
    
    private class Node {
        int key;
        Node next;

        Node(int item) {
            this.key = item; // For integers, the item itself acts as the key for sorting
        }
    }

    private Node head;
    // A single, coarse-grained lock that protects the ENTIRE list.
    // Any thread wanting to read or write must acquire this lock first.
    private Lock lock = new ReentrantLock();

    public CoarseList() {
        // Sentinel nodes (MIN and MAX) are placed at the start and end of the list.
        // This trick ensures 'pred' and 'curr' always exist during traversal,
        // so we don't need messy if-statements for empty lists or end-of-list bounds!
        head = new Node(Integer.MIN_VALUE);
        head.next = new Node(Integer.MAX_VALUE);
    }

    public boolean add(int value) {
        Node pred, curr;
        int key = value;
        
        // 1. Acquire the lock BEFORE touching any nodes
        lock.lock();
        try {
            pred = head;
            curr = pred.next;
            
            // 2. Traverse until we find the right sorted position
            while (curr.key < key) {
                pred = curr;
                curr = curr.next;
            }
            
            // 3. If it already exists, refuse to add a duplicate
            if (key == curr.key) {
                return false; 
            } else {
                // 4. Otherwise, insert it precisely between pred and curr
                Node node = new Node(value);
                node.next = curr;
                pred.next = node;
                return true;
            }
        } finally {
            // 5. CRITICAL: Always release the lock in a finally block!
            // This ensures we never accidentally create a deadlock if an error occurs.
            lock.unlock();
        }
    }

    public boolean remove(int value) {
        Node pred, curr;
        int key = value;
        
        lock.lock();
        try {
            pred = head;
            curr = pred.next;
            
            // Traverse to find the exact node to remove
            while (curr.key < key) {
                pred = curr;
                curr = curr.next;
            }
            
            // If we found it, unlink it by pointing pred directly to curr.next
            if (key == curr.key) {
                pred.next = curr.next; 
                return true;
            } else {
                // Not found in the list
                return false;
            }
        } finally {
            lock.unlock(); // Always release
        }
    }

    public boolean contains(int value) {
        Node pred, curr;
        int key = value;
        
        // Even for reading, we MUST lock! Without it, another thread 
        // could be modifying the list (like removing a node) while we traverse it.
        lock.lock();
        try {
            pred = head;
            curr = pred.next;
            
            while (curr.key < key) {
                pred = curr;
                curr = curr.next;
            }
            
            // Return true if we found a match, false otherwise
            return (key == curr.key);
        } finally {
            lock.unlock();
        }
    }
}
