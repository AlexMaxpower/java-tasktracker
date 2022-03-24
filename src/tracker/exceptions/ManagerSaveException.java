package tracker.exceptions;

import java.io.IOException;

public class ManagerSaveException extends IOException {
    public ManagerSaveException(final String message) {
        System.out.println(message);
    }
}
