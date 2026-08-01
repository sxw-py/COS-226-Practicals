public class PetersonsLock {
    /*
    This is a lock implementation using Peterson's lock, it satisfies mutual exclusion
    reference: page 24-27
    peterson lock is starvation and deadlock free
    */

    //thread-local index, 0 or 1
    private volatile boolean[] flag = new boolean[2];
    private volatile int victim;
    public void lock(int myID){
        int i = myID;
        int j=1-i;
        flag[i] = true; //says I'm interested;
        victim = i; // says you go first

        while(flag[j] && victim == i){ //wait until other thread isnt interest or my turn 

        };
    }
    public void unlock(int myID){
        int i = myID;
        flag[i] = false; // says I'm not interested
    }
    
}
