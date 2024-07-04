package order.management.service;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import order.management.dto.PageDto;
import order.management.dto.OrderDto;
import order.management.dto.http.OrderRequest;
import order.management.dto.http.OrderResponse;
import order.management.exception.InsufficientQuantityException;
import order.management.exception.OrderNotFoundException;
import order.management.model.*;
import order.management.repository.OrderOutBoxRepository;
import order.management.repository.OrderRepository;
import order.management.repository.OrderRequestRepository;
import order.management.utils.OrderMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderOutBoxRepository orderOutBoxRepository;
    private final OrderRequestRepository orderRequestRepository;

    private final ProductService productService;
    private final NotificationService notificationService;
    private final CacheService cacheService;



    public PageDto getMyOrders(int size, int page) {
        log.info("Get user's orders request for page {} with size {}", page, size);
        Page<Order> orderPage = orderRepository.findAll(PageRequest.of(page, size));
        Page<OrderDto> orders = orderPage.map(OrderMapper::map);

        return PageDto.builder()
                .data(orders.getContent())
                .total(orders.getTotalElements())
                .build();
    }

    public OrderDto getMyOrder(String id) {
        log.info("Get user's order request for order id {}", id);

        log.info("Retrieving from cache...");
        Order order = cacheService.getFromCache(id);

        if (order == null) {
            log.info("Cache is empty. Retrieving from database...");
            order = orderRepository.findById(UUID.fromString(id))
                    .orElseThrow(() -> new OrderNotFoundException(id));

            cacheService.putIntoCache(id, order);
            log.info("Inserted into cache.");
        }

        return OrderMapper.map(order);
    }

    //TODO: test atomicity
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
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

        log.info("2nd step finished and starting 3rd step...");

        //3rd step:   create an order request job
        createOrderRequestJob(order);
        log.info("3rd step finished. Order created");

        //insert into outbox table
        saveToOutBoxTable(order);

        //different transaction scope
        sendEvent(order);

        return OrderResponse.builder()
                .message("order placed")
                .orderId(order.getId().toString())
                .build();
    }

    private void createOrderRequestJob(Order order) {
        OrderRequestJob job = new OrderRequestJob();
        job.setStatus(RequestStatus.REQUESTED);
        job.setInfo("Pending for approval");
        job.setOrder(order);
        orderRequestRepository.save(job);
        log.info("Order request job created with default status");
    }

    private void saveToOutBoxTable(Order order) {
        log.info("Inserting into outbox table...");
        OrderOutBox orderOutBox = new OrderOutBox();
        orderOutBox.setOrder(order);
        orderOutBoxRepository.save(orderOutBox);
    }

    private void sendEvent(Order order){
        log.info("Sending order creation event...");
        try {
            notificationService.sendOrderCreationEvent(order);
        } catch (Exception e) {
            log.error("notification error", e);
        }
    }

    //TODO
    private int defineUser() {
        return 0;
    }

    public String update(String id, OrderRequest request) {
        Order order = cacheService.getFromCache(id);

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

        cacheService.putIntoCache(order.getId().toString(), order);
        log.info("Inserted into cache");

        return "order updated for id: "+id;
    }

    @Transactional
    public String deleteMyOrder(String id) {
        UUID orderId = UUID.fromString(id);
        orderRepository.deleteById(orderId);
        log.info("User's order with order id {} has been deleted", id);

        log.info("removing from outbox table...");
        orderOutBoxRepository.deleteByOrder(orderId);

        cacheService.removeFromCache(id);
        log.info("Removed from cache");

        return "order deleted for id: "+id;
    }

    public void saveOrder(Order order) {
        orderRepository.save(order);
    }
}
