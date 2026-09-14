import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class TTASLock implements SpinLock
{

    private final AtomicBoolean locked = new AtomicBoolean(false);

    //count every testAndSet() call so we can compare TAS and TTAS in task 3
    private final AtomicLong tasCount = new AtomicLong(0);

    /* Do not modify this method */
    private boolean testAndSet() 
    {
        return locked.getAndSet(true);
    }

    @Override
    public void lock() 
    {
        while (true) { 
            //First, spin while the lock appears to be held
            while(locked.get()){
                //busy-wait 
            }
            //lock looks free now so now we try the actual atomic swap
            tasCount.incrementAndGet();

            //lock appears free, now try to acquire it
            if (!testAndSet()){
                //successfully acquired the lock
                return;
            }
            //if true, someone else got the lock first (loop back)
        }
        
    }

    @Override
    public void unlock() 
    {
        locked.set(false);
    }
    
    @Override
    public long getTestAndSetCount()
    {
        return tasCount.get();
    }
}