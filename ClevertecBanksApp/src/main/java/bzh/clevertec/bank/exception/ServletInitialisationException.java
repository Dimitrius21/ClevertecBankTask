package bzh.clevertec.bank.exception;

public class ServletInitialisationException extends RuntimeException {

    public ServletInitialisationException() {
    }

    public ServletInitialisationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServletInitialisationException(String message) {
        super(message);
    }
}
