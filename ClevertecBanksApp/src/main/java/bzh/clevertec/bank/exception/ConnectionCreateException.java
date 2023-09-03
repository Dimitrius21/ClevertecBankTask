package bzh.clevertec.bank.exception;

public class ConnectionCreateException extends RuntimeException{

    public ConnectionCreateException() {
    }

    public ConnectionCreateException(String message, Throwable cause) {
        super(message, cause);
    }
}
