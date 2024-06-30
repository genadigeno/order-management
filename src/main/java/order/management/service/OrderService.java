package order.management.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import order.management.dto.PageDto;
import order.management.dto.OrderDto;
import order.management.dto.http.OrderRequest;
import order.management.exception.InsufficientQuantityException;
import order.management.exception.OrderNotFoundException;
import order.management.model.Order;
import order.management.model.Product;
import order.management.repository.OrderRepository;
import order.management.utils.OrderMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final HazelcastInstance hazelcastInstance;
    private final OrderRepository orderRepository;

    private final ProductService productService;
    private final OrderRequestService orderRequestService;

    private IMap<String, Order> getOrderCacheMap(){
        return hazelcastInstance.getMap("orderCache");
    }

    public PageDto getMyOrders(int size, int page) {
        log.info("Get user's orders request for page {} with size {}", page, size);
        Page<OrderDto> orders = orderRepository.findAll(PageRequest.of(page, size)).map(OrderMapper::map);
        return PageDto.builder()
                .data(orders.getContent())
                .total(orders.getTotalElements())
                .build();
    }

    public OrderDto getMyOrder(String id) {
        log.info("Get user's order request for order id {}", id);

        IMap<String, Order> cache = getOrderCacheMap();
        log.info("Retrieving from cache...");
        Order order = cache.get(id);

        if (order == null) {
            log.info("Cache is empty. Retrieving from database...");
            order = orderRepository.findById(UUID.fromString(id))
                    .orElseThrow(() -> new OrderNotFoundException(id));

            cache.put(id, order);
            log.info("Inserted into cache.");
        }

        return OrderMapper.map(order);
    }

    //TODO: test atomicity
    //TODO: explain why isolation level is READ_COMMITTED
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public String createOrder(OrderRequest request) {
        //1st step: find requested product
        log.info("Creating order. Starting 1st step...");
        Product product = productService.getAvailableProduct(request.getProductId());
        if (product.getQuantity() < request.getQuantity()) {
            log.warn("Quantity is not enough; required {}, remaining {}", request.getQuantity(), product.getQuantity());
            throw new InsufficientQuantityException();
        }

        log.info("1st step finished and starting 2nd step...");

        //2nd step: create an order
        Order order = OrderMapper.map(request);
        order.setProduct(product);
        order.setPrice(product.getPrice());//A price could be changed after placing the order
        order.setUserId(defineUser());
        order = orderRepository.save(order);

        getOrderCacheMap().put(order.getId().toString(), order);
        log.info("Inserted into cache");

        log.info("2nd step finished and starting 3rd step...");

        //3rd step:   create an order request job
        orderRequestService.createOrderRequestJob(order);
        log.info("3rd step finished. Order created");

        return "order placed for id: "+order.getId();
    }

    //TODO
    private int defineUser() {
        return 0;
    }

    public String update(String id, OrderRequest request) {
        IMap<String, Order> cache = getOrderCacheMap();
        Order order = cache.get(id);

        if (order == null) {
            log.info("Cache is empty. Retrieving from database...");
            order = orderRepository.findById(UUID.fromString(id))
                    .orElseThrow(() -> new OrderNotFoundException(id));
        }

        OrderMapper.map(request, order);

        if (order.getProduct().getId() != request.getProductId()) {
            log.info("Modifying product with new one...");
            order.setProduct(productService.getAvailableProduct(request.getProductId()));
        }

        order = orderRepository.save(order);
        log.info("User's order with order id {} has been updated", id);

        cache.put(order.getId().toString(), order);
        log.info("Inserted into cache");

        return "order updated for id: "+id;
    }

    public String deleteMyOrder(String id) {
        orderRepository.deleteById(UUID.fromString(id));
        log.info("User's order with order id {} has been deleted", id);

        getOrderCacheMap().delete(id);
        log.info("Removed from cache");

        return "order deleted for id: "+id;
    }
}
