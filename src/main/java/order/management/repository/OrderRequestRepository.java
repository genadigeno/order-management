package order.management.repository;

import order.management.model.OrderRequestJob;
import order.management.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRequestRepository extends JpaRepository<OrderRequestJob, Integer> {
    List<OrderRequestJob> getAllByStatus(RequestStatus status);
}
