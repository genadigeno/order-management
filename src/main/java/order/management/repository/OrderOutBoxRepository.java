package order.management.repository;

import order.management.model.Order;
import order.management.model.OrderOutBox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderOutBoxRepository extends JpaRepository<OrderOutBox, UUID> {
    @Modifying
    @Query("delete from OrderOutBox ob where ob.order.id = ?1")
    void deleteByOrder(UUID orderId);

    OrderOutBox findByOrder(Order order);
}
