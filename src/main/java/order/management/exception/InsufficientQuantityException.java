package order.management.exception;

public class InsufficientQuantityException extends RuntimeException {
    public InsufficientQuantityException() {
        super("Quantity is not sufficient");
    }
}
