/*Represents a single unit of work (job) processed by the system.
Each job has a unique identifier to track its production and consumption. */
public class Job {
    private final int id;

    public Job(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}