public class Counter 
{

    private int value = 0;

    public void increment() 
    {
        int current = value;
        value = current + 1;
    }

    public int getValue() {
        return value;
    }
}