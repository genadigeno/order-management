package order.management.repository;

import order.management.model.OrderRequestJob;
import order.management.model.RequestStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRequestRepository extends JpaRepository<OrderRequestJob, Integer> {

    @EntityGraph("order_requests")
    @Query("select orj from OrderRequestJob orj where orj.status = :status order by orj.created asc")
    List<OrderRequestJob> findAllOrderByCreated(@Param("status") RequestStatus status);

    @Query("select r from OrderRequestJob r where r.order.id = ?1")
    OrderRequestJob findByOrderId(UUID id);
}
