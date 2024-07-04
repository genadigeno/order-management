package order.management.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import order.management.model.OrderOutBox;
import order.management.repository.OrderOutBoxRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderOutBoxService {
    private final OrderOutBoxRepository orderOutBoxRepository;

    public List<OrderOutBox> findAll() {
        log.info("Retrieving data from outbox table...");
        return orderOutBoxRepository.findAll();
    }
}
