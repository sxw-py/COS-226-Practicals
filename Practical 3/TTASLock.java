import java.util.concurrent.atomic.AtomicBoolean;

public class TTASLock implements SpinLock
{

    private final AtomicBoolean locked = new AtomicBoolean(false);

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
    
}