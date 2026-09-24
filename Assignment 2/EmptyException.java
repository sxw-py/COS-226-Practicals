public class EmptyException extends Exception {
    public EmptyException() {
        super("Queue is empty");
    }
}
