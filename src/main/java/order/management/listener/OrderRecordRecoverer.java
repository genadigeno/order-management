package order.management.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;

@Slf4j
@RequiredArgsConstructor
public class OrderRecordRecoverer implements ConsumerRecordRecoverer {
    private final String dlt;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void accept(ConsumerRecord<?, ?> data, Exception exception) {
        log.warn("Error: {}, value - {}, key - {}", exception.getMessage(), data.value(), data.key(), exception);
        kafkaTemplate.send(dlt, String.valueOf(data.key()), String.valueOf(data.value()));
    }
}
