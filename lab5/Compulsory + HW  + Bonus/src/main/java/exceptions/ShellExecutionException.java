package exceptions;

public class ShellExecutionException extends Exception {
    public ShellExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ShellExecutionException(String message) {
        super(message);
    }
}
