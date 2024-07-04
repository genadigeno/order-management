package order.management.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import order.management.model.Order;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventListener {

    //concurrency = 10 task per instance
    @KafkaListener(
            concurrency = "10",
            topics = "${kafka.orders.topic}",
            groupId = "${spring.application.name}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen(ConsumerRecord<String, Order> message, Acknowledgment ack){
        log.info("Message: key = {}, value = {}", message.key(), message.value());
        ack.acknowledge();
    }
}
