import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class FineList 
{

    private final Node head;
    private final Node tail;

    public FineList() 
    {
        head = new Node(Integer.MIN_VALUE);
        tail = new Node(Integer.MAX_VALUE);

        head.next = tail;
    }

    public boolean add(int value) 
    {
        head.lock.lock();
        Node pred = head;
        try{
            Node curr = pred.next;
            curr.lock.lock();;
            try{
                //Traverse using hand-over-hand locking to find insertion point
                while (curr.value < value){
                    pred.lock.unlock(); //release previous lock
                    pred = curr;
                    curr = curr.next;
                    curr.lock.lock(); //lock next node before proceeding
                }

                if (curr.value == value){
                    return false; //duplicate value found
                }

                //insetion step: create new node and update pointers
                Node node = new Node(value);
                node.next = curr;
                pred.next = node;
                return true;
                } finally{
                    curr.lock.unlock(); //release current lock
                }

            }finally {
                pred.lock.unlock(); //release previous lock
            }
        }

    

    public boolean remove(int value) 
    {
        head.lock.lock();
        Node pred = head;
        try{
            Node curr = pred.next;
            curr.lock.lock();
            try{
                //Traverse using hand-over-hand locking to find node to remove
                while (curr.value < value){
                    pred.lock.unlock(); //release previous lock
                    pred = curr;
                    curr = curr.next;
                    curr.lock.lock(); //lock next node before proceeding
                }

                if (curr.value == value){
                    //unlink the node to remove it from the list
                    pred.next = curr.next;
                    return true; //node successfully removed
                }
                return false; //node not found in the list
            } finally {
                curr.lock.unlock(); //release current lock
            }
        } finally {
            pred.lock.unlock(); //release previous lock
        }
    }

    public boolean contains(int value) 
    {
        head.lock.lock();
        Node pred = head;
        try{
            Node curr = pred.next;
            curr.lock.lock();
            try{
                //hand-over-hand traversal 
                while (curr.value < value){
                    pred.lock.unlock(); //release previous lock
                    pred = curr;
                    curr = curr.next;
                    curr.lock.lock(); //lock next node before proceeding
                }

                return (curr.value == value); //return true if found, false otherwise
            } finally {
                curr.lock.unlock(); //release current lock
            }
        } finally {
            pred.lock.unlock(); //release previous lock
        }
    }
}
