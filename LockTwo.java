public class LockTwo {
    private volatile int victim;

    public void lock(int myID){
        victim = myID;              //volunteers as victim
        while (victim == myID) {
            //busy-wait (repeatedly check condition)
        }

    }

    public void unlock(){
        //No state needs resetting for Lock two. 
        // The 'victim' flag is naturally overwritten the next time either thread calls lock()
    }
}