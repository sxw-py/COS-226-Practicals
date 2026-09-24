import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Node 
{
    int value;
    Node next;

    final Lock lock = new ReentrantLock();

    public Node(int value) 
    {
        this.value = value;
    }
}