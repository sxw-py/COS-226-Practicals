public class BakeryLock implements Lock {

    private final int n;
    private final VolatileBoolean[] flag;
    private final VolatileInt[] label;

    public BakeryLock(int n) {
        this.n = n;
        flag = new VolatileBoolean[n];
        label = new VolatileInt[n];
        for (int i = 0; i < n; i++) {
            flag[i] = new VolatileBoolean(false);
            label[i] = new VolatileInt(0);
        }
    }

    @Override
    public void lock(int threadId) {
        flag[threadId].value = true; // Enter bakery

        int maxLabel = 0;
        for (int k = 0; k < n; k++) {
            if (label[k].value > maxLabel) {
                maxLabel = label[k].value; // look for current highest label
            }
        }
        label[threadId].value = maxLabel + 1; // two threads could've possibly gotten the same max label if they were
                                              // checking concurrently

        for (int k = 0; k < n; k++) {
            if (k != threadId) {
                while (flag[k].value &&
                        (label[k].value < label[threadId].value ||
                                (label[k].value == label[threadId].value && k < threadId))) {
                    // spin wait
                }
            }
        }
    }

    @Override
    public void unlock(int threadId) {
        flag[threadId].value = false; // leave the bakery (let go of lock)
    }
}