package order.management;

import order.management.dto.http.OrderRequest;
import order.management.dto.http.OrderResponse;
import order.management.model.Order;
import order.management.model.OrderRequestJob;
import order.management.model.Product;
import order.management.model.Status;
import order.management.repository.OrderRepository;
import order.management.repository.OrderRequestRepository;
import order.management.repository.ProductRepository;
import order.management.service.OrderService;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource("classpath*:application.properties")
@EmbeddedKafka(partitions = 1, topics = "test.orders.topic",
		brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
class OrderApplicationTests extends AbstractIntegrationTest {

	@Autowired
	OrderService orderService;
	@Autowired
	private ProductRepository productRepository;
	@Autowired
	private OrderRepository orderRepository;
	@Autowired
	private OrderRequestRepository orderRequestRepository;

	static String uuid = "550e8400-e29b-41d4-a716-446655440000";
	static Order order;
	static Product product;
	static OrderRequest orderRequest;
	static OrderRequest insufficientOrderRequest;

	@BeforeAll
	static void setup(){
		product = new Product();
		product.setId(1);
		product.setQuantity(5);
		product.setPrice(BigDecimal.ONE);
		product.setName("Iphone 14 Pro");

		order = new Order();
		order.setId(UUID.fromString(uuid));
		order.setStatus(Status.PENDING);
		order.setPrice(BigDecimal.ONE);
		order.setProduct(product);
		order.setUserId(1);

		orderRequest = new OrderRequest();
		orderRequest.setQuantity(5);
		orderRequest.setPrice(BigDecimal.ONE);
		orderRequest.setProductId(product.getId());
		orderRequest.setUserId(1);

		insufficientOrderRequest = new OrderRequest();
		insufficientOrderRequest.setQuantity(6);
		insufficientOrderRequest.setPrice(BigDecimal.ONE);
		insufficientOrderRequest.setProductId(product.getId());
		insufficientOrderRequest.setUserId(1);
	}

	@Test
	@Transactional
	void test_order_create() {
		// Setup test data
		Product product = new Product();
		product.setId(1);
		product.setName("Test Product");
		product.setQuantity(10);
		product.setPrice(BigDecimal.valueOf(100.0));
		productRepository.save(product); // Assuming save method exists in ProductService

		OrderRequest request = new OrderRequest();
		request.setProductId(product.getId());
		request.setQuantity(5);

		// Call the method to test
		OrderResponse result = orderService.createOrder(request);

		// Verify results
		assertTrue(result.getMessage().contains("order placed"));

		Order order = orderRepository.findById(UUID.fromString(result.getOrderId()))
				.orElse(null);

		assertThat(order).isNotNull();
		assertThat(order.getProduct().getId()).isEqualTo(product.getId());
		assertThat(order.getQuantity()).isEqualTo(request.getQuantity());

		kafkaTemplate.send("test.orders.topic", order.getId().toString(), order.toString());

		OrderRequestJob job = orderRequestRepository.findByOrderId(order.getId());
		assertThat(job).isNotNull();

		// Verify event sending
		Consumer<String, String> consumer = configureConsumer();
		ConsumerRecord<String, String> record =
				KafkaTestUtils.getSingleRecord(consumer, "test.orders.topic");
		assertEquals(result.getOrderId(), record.key());//key is order id
	}

	@Autowired
	EmbeddedKafkaBroker embeddedKafkaBroker;

	@Autowired
	KafkaTemplate<String, Object> kafkaTemplate;

	private Consumer<String, String> configureConsumer() {
		Map<String, Object> consumerProps =
				KafkaTestUtils.consumerProps("testGroup", "true", embeddedKafkaBroker);
		consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		Consumer<String, String> consumer =
				new DefaultKafkaConsumerFactory<String, String>(consumerProps).createConsumer();
		consumer.subscribe(Collections.singleton("test.orders.topic"));
		return consumer;
	}
}
