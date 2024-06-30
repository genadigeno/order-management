package order.management.repository;

import order.management.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    /*@Query("select p from Product p where p.id = ?1 and p.quantity > 0")
    Optional<Product> getAvailableProductById(int id);*/
}
