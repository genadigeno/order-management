package order.management.exception;

public class OrderNotFoundException extends RuntimeException{
    public OrderNotFoundException(String id) {
        super("User with id ["+id+"] not found");
    }
}
