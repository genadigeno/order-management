package order.management.service;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import order.management.model.Order;
import order.management.model.OrderOutBox;
import order.management.repository.OrderOutBoxRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final OrderOutBoxRepository orderOutBoxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.orders.topic}")
    private String ordersTopic;

    @Value("${kafka.orders.success.topic}")
    private String successfulOrdersTopic;

    @Value("${kafka.orders.failed.topic}")
    private String failedOrdersTopic;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    //sent from order-service
    public void sendOrderCreationEvent(Order order) {
        executorService.submit(() -> {
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(ordersTopic, order.getId().toString(), order);

            future.thenAccept(result -> {
                log.info("Event sent. Deleting from outbox table...");
                OrderOutBox orderOutBox = orderOutBoxRepository.findByOrder(order);
                log.info("UserOutBox - {}", orderOutBox);
                orderOutBoxRepository.delete(orderOutBox);
            });

            future.exceptionally(ex -> {
                log.error("Event did not send", ex);
                return null;
            });
        });
    }

    //sent from task scheduler
    public void sendOrderCreationEvent(final OrderOutBox orderOutBox) {
        executorService.submit(() -> {
            Order order = orderOutBox.getOrder();
            log.info("Sending event for orderOutBox - {}, order - {}", orderOutBox.getId(), order.getId());
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(ordersTopic, order.getId().toString(), order);

            future.thenAccept(result -> {
                log.info("Event sent. Deleting from outbox table... OrderOutBox - {}", orderOutBox);
                orderOutBoxRepository.delete(orderOutBox);
            });

            future.exceptionally(ex -> {
                log.error("Event did not send", ex);
                return null;
            });
        });
    }

    public void sendOrderFailedEvent(Order order) {
        executorService.submit(() -> {
            log.info("Sending to order failed topic...");
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(failedOrdersTopic, order.getId().toString(), order);

            future.thenAccept(result -> log.info("Event sent."));

            future.exceptionally(ex -> {
                log.error("Event did not send", ex);
                return null;
            });
        });
    }

    public void sendOrderSucceedEvent(Order order) {//TODO: TEST IT
        executorService.submit(() -> {
            log.info("Sending to order success topic...");
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(successfulOrdersTopic, order.getId().toString(), order);

            future.thenAccept(result -> log.info("Order Succeed Event sent."));

            future.exceptionally(ex -> {
                log.error("Event did not send", ex);
                return null;
            });
        });
    }

    @PreDestroy
    public void close(){
        executorService.shutdown();
    }
}
