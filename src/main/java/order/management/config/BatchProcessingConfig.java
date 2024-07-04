package order.management.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import order.management.model.OrderOutBox;
import order.management.model.OrderRequestJob;
import order.management.service.NotificationService;
import order.management.service.OrderOutBoxService;
import order.management.service.OrderRequestService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

@Slf4j
@EnableScheduling
@Configuration
@RequiredArgsConstructor
public class BatchProcessingConfig {
    private final OrderOutBoxService  orderOutBoxService;
    private final NotificationService notificationService;
    private final OrderRequestService orderRequestService;

    @Scheduled(cron = "0 * * * * *")
    public void extractDataFromOutboxTable(){
        List<OrderOutBox> users = orderOutBoxService.findAll();
        log.info("total outbox records - {}", users.size());
        users.forEach(notificationService::sendOrderCreationEvent);
    }

    @Scheduled(cron = "0 */2 * * * *")
    public void processOrderRequests(){
        List<OrderRequestJob> orderRequests = orderRequestService.getAllRequests();
        log.info("Total order requests - {}", orderRequests.size());
        //Some business logic that process order requests...
        orderRequests.forEach(orderRequestService::process);
    }
}
