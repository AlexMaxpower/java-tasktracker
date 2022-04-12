package tracker.exceptions;

public class AddTaskException extends RuntimeException {
    public AddTaskException(final String message) {
        super(message);
        System.out.println(message);
    }
}

