public interface Lock 
{
    void lock(int threadId);
    void unlock(int threadId);
}