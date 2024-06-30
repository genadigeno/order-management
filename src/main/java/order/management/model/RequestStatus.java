package order.management.model;

public enum RequestStatus {
    REQUESTED(1), COMPLETED(2), REJECTED(3), FAILED(4), EXPIRED(5);

    RequestStatus(int id) {
    }
}
