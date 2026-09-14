public class FilterLock implements Lock 
{

    private final int n;
    private final VolatileInt[] level;
    private final VolatileInt[] victim;
    //followed the textbook implementation
    public FilterLock(int n) 
    {
        //n threads, so we need n-1 levels for them to climb through
        this.n = n;
        level = new VolatileInt[n];
        victim = new VolatileInt[n];

        //everyone starts at level 0 (not trying to get in yet)
        for (int i = 0; i < n; i++) 
        {
            level[i] = new VolatileInt(0);
        }

        //victim[l] just needs some starting value, doesn't really matter what
        for (int l = 0; l < n; l++) 
        {
            victim[l] = new VolatileInt(0);
        }
    }

    @Override
    public void lock(int threadId) 
    {
        //thread has to climb from level 1 up to level n-1 the higher the level, the closer to actually getting the lock
        for (int l = 1; l < n; l++) 
        {
            level[threadId].value = l;
            victim[l].value = threadId;

            //wait while there's some other thread at this level or higher AND this thread is still the victim (i.e. everyone else has moved on or bailed)
            boolean exists;
            do 
            {
                exists = false;
                for (int k = 0; k < n; k++) 
                {
                    if (k != threadId && level[k].value >= l) 
                    {
                        exists = true;
                        break;
                    }
                }
            } 
            while (exists && victim[l].value == threadId);
        }
        //once we're out of the loop, threadId is at level n-1 and safe to enter
    }

    @Override
    public void unlock(int threadId) 
    {
        //just drop back down to level 0, releasing the lock
        level[threadId].value = 0;
    }
}