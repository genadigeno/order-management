package order.management.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import order.management.model.Product;
import order.management.repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductService {
    private final HazelcastInstance hazelcastInstance;
    private final ProductRepository productRepository;

    public Product getAvailableProduct(int productId) {
        log.info("Searching product with id {}", productId);
        IMap<Integer, Product> productCacheMap = getProductCacheMap();
        Product product = productCacheMap.get(productId);

        if (product == null) {
            log.info("Cache is empty. Retrieving from database...");
            product = productRepository.findById(productId)
                    .orElseThrow(() -> new EntityNotFoundException("Product not found"));

            productCacheMap.put(productId, product);
            log.info("Inserted into cache.");
        }
        return product;
    }

    private IMap<Integer, Product> getProductCacheMap(){
        return hazelcastInstance.getMap("productCache");
    }

    public int getQuantity(int productId) {
        log.info("fetching product[{}] quantity", productId);
        return productRepository.findQuantityById(productId);
    }

    public void decreaseQuantity(int productId, int requiredQuantity) {
        productRepository.decreaseQuantity(productId, requiredQuantity);
    }
}
