import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;


public class TASLock implements SpinLock
{

    private final AtomicBoolean locked = new AtomicBoolean(false);

    //counts every testAndSet() call so we can compare TAS vs TTSA in task 3
    private final AtomicLong tasCount = new AtomicLong(0);

    /* Do not modify this method */
    private boolean testAndSet() 
    {
        return locked.getAndSet(true);
    }

    @Override
    public void lock() 
    {
        tasCount.incrementAndGet();
        while(testAndSet()){
            tasCount.incrementAndGet();
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