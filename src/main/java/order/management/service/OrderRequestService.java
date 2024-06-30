package order.management.service;

import com.hazelcast.core.HazelcastInstance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import order.management.model.Order;
import order.management.model.OrderRequestJob;
import order.management.model.RequestStatus;
import order.management.repository.OrderRequestRepository;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderRequestService {
    private final OrderRequestRepository orderRequestRepository;

    public void createOrderRequestJob(Order order) {
        OrderRequestJob job = new OrderRequestJob();
        job.setStatus(RequestStatus.REQUESTED);
        job.setInfo("Pending for approval");
        job.setOrder(order);
        orderRequestRepository.save(job);
        log.info("Order request job created with default status");
    }
}
