package order.management.repository;

import order.management.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    @Query("select p.quantity from Product p where p.id = ?1")
    int findQuantityById(int id);

    @Modifying
    @Query("update Product p set p.quantity = ((select pp.quantity from Product pp where pp.id=?1) - ?2) " +
           "where p.id = ?1")
    void decreaseQuantity(int id, int requiredQuantity);
}
