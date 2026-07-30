public class LockOne {
    private volatile boolean[] flag = new boolean[2];

    public void lock(int myID) {
        int other = 1 - myID;
        flag[myID] = true;

        while (flag[other]) {
            // wait for other thread to finish
        }
    }

    public void unlock(int myID) {
        flag[myID] = false;
    }
}
