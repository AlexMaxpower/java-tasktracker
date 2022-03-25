package tracker.exceptions;

public class ManagerSaveException extends Error {
    public ManagerSaveException(final String message) {
        System.out.println(message);
    }
}
