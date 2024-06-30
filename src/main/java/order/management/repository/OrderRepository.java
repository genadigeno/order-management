package order.management.repository;

import order.management.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    @EntityGraph("order-entity-graph")
    @Override
    Page<Order> findAll(Pageable pageable);

    @EntityGraph("order-entity-graph")
    @Override
    Optional<Order> findById(UUID uuid);
}
