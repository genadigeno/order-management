package order.management.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import order.management.model.*;
import order.management.repository.OrderRequestRepository;
import order.management.utils.VoidConsumer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Consumer;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderRequestService {
    private final ProductService productService;
    private final OrderService orderService;
    private final NotificationService notificationService;
    private final OrderRequestRepository orderRequestRepository;

    public List<OrderRequestJob> getAllRequests() {
        return orderRequestRepository.findAllOrderByCreated(RequestStatus.REQUESTED);
    }

    @Transactional
    public void process(OrderRequestJob orderRequest) {
        int productId = orderRequest.getOrder().getProduct().getId();
        int requiredQuantity = orderRequest.getOrder().getQuantity();

        //check if enough
        if (isEnough(productId, requiredQuantity)){
            orderRequest.setStatus(RequestStatus.COMPLETED);
            orderRequest.setInfo("Order request approved.");
            orderRequestRepository.save(orderRequest);

            Order order = orderRequest.getOrder();
            order.setStatus(Status.APPROVED);
            orderService.saveOrder(order);

            log.info("Decreasing with required quantity {} for product[{}]", requiredQuantity, productId);
            productService.decreaseQuantity(productId, requiredQuantity);

            //different transaction scope
            log.info("Sending successful order process event...");
            notificationService.sendOrderSucceedEvent(order);
        }
        else {
            orderRequest.setStatus(RequestStatus.REJECTED);
            orderRequest.setInfo("Order request rejected due to insufficient quantity");
            orderRequestRepository.save(orderRequest);

            Order order = orderRequest.getOrder();
            order.setStatus(Status.REJECTED);
            orderService.saveOrder(order);

            //different transaction scope
            log.warn("Sending failed order process event...");
            notificationService.sendOrderFailedEvent(order);
        }
    }

    private boolean isEnough(int productId, int requestedQuantity){
        log.info("Inspecting available product quantity");
        int availableQuantity = productService.getQuantity(productId);
        log.info("Available quantity is {}, requested - {}", availableQuantity, requestedQuantity);
        return requestedQuantity <= availableQuantity;
    }
}
