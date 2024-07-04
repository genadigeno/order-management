package order.management.service;

import order.management.dto.OrderDto;
import order.management.dto.PageDto;
import order.management.dto.http.OrderRequest;
import order.management.dto.http.OrderResponse;
import order.management.exception.InsufficientQuantityException;
import order.management.exception.OrderNotFoundException;
import order.management.model.*;
import order.management.repository.OrderOutBoxRepository;
import order.management.repository.OrderRequestRepository;
import order.management.repository.OrderRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    OrderService orderService;

    @Mock
    OrderRepository orderRepository;
    @Mock
    CacheService cacheService;
    @Mock
    ProductService productService;
    @Mock
    NotificationService notificationService;
    @Mock
    OrderOutBoxRepository orderOutBoxRepository;
    @Mock
    OrderRequestRepository orderRequestRepository;

    int page = 1, size = 10;
    static String uuid = "550e8400-e29b-41d4-a716-446655440000";
    static Order order;
    static Product product;
    static OrderRequest orderRequest;
    static OrderRequest insufficientOrderRequest;

    @BeforeAll
    public static void setup(){
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
    void test_getMyOrders_success() {
        List<Order> orderList = List.of(order);
        Page<Order> orderPage = new PageImpl<>(orderList, PageRequest.of(page, size), orderList.size());

        when(orderRepository.findAll(any(PageRequest.class)))
                .thenReturn(orderPage);

        PageDto result = orderService.getMyOrders(page, size);

        assertEquals(orderPage.getContent().size(), result.getData().size());
        assertEquals(orderPage.getTotalElements(), result.getTotal());

        verify(orderRepository, times(1))
                .findAll(any(PageRequest.class));
    }

    //--------------------------------------------------------------------------------------------------------//
    @Test
    @DisplayName("test getMyOrder() with success when cache is not empty")
    void test_getMyOrder_success_1() {
        when(cacheService.getFromCache(anyString()))
                .thenReturn(order);

        OrderDto orderDto = orderService.getMyOrder(uuid);

        assertEquals(orderDto.getUuid(), order.getId().toString());

        verify(orderRepository, times(0))
                .findById(any(UUID.class));
    }

    @Test
    @DisplayName("test getMyOrder() with success when cache is empty")
    void test_getMyOrder_success_2() {
        when(cacheService.getFromCache(anyString()))
                .thenReturn(null);
        when(orderRepository.findById(any(UUID.class)))
                .thenReturn(Optional.ofNullable(order));

//        OrderDto orderDto = orderService.getMyOrder(uuid);
        OrderDto orderDto = assertDoesNotThrow(() -> orderService.getMyOrder(uuid));

        assertEquals(orderDto.getUuid(), order.getId().toString());
        verify(orderRepository, times(1))
                .findById(any(UUID.class));
        verify(cacheService, times(1))
                .putIntoCache(anyString(), any(Order.class));
    }

    @Test
    @DisplayName("test getMyOrder() with failure orderRepository throws an exception")
    void test_getMyOrder_failure() {
        when(cacheService.getFromCache(anyString()))
                .thenReturn(null);
        when(orderRepository.findById(any(UUID.class)))
                .thenThrow(new OrderNotFoundException(""));

        assertThrows(OrderNotFoundException.class, () -> orderService.getMyOrder(uuid));

        verify(cacheService, times(0))
                .putIntoCache(anyString(), any(Order.class));
    }

    //--------------------------------------------------------------------------------------------------------//
    @Test
    @DisplayName("test createOrder() with success")
    void test_createOrder_success() {
        OrderRequestJob job = new OrderRequestJob();
        job.setStatus(RequestStatus.REQUESTED);
        job.setInfo("Pending for approval");
        job.setOrder(order);

        OrderOutBox orderOutBox = new OrderOutBox();
        orderOutBox.setOrder(order);

        when(productService.getAvailableProduct(anyInt()))
                .thenReturn(product);
        when(orderRepository.save(any(Order.class)))
                .thenReturn(order);
        when(orderRequestRepository.save(any(OrderRequestJob.class)))
                .thenReturn(job);
        when(orderOutBoxRepository.save(any(OrderOutBox.class)))
                .thenReturn(orderOutBox);
        doNothing()
                .when(notificationService).sendOrderCreationEvent(any(Order.class));

        OrderResponse result = assertDoesNotThrow(() -> orderService.createOrder(orderRequest));

        assertEquals(result.getOrderId(), String.valueOf(order.getId()));//result returns text with order id.
        verify(productService, times(1)).getAvailableProduct(anyInt());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderRequestRepository, times(1)).save(any(OrderRequestJob.class));
        verify(orderOutBoxRepository, times(1)).save(any(OrderOutBox.class));
        verify(notificationService, times(1)).sendOrderCreationEvent(any(Order.class));
    }

    @Test
    @DisplayName("test create Order with failure when requested quantity is more than available one")
    void test_createOrder_failure() {
        when(productService.getAvailableProduct(anyInt()))
                .thenReturn(product);

        assertThrows(InsufficientQuantityException.class, () -> orderService.createOrder(insufficientOrderRequest));

        verify(productService, times(1)).getAvailableProduct(anyInt());
        verify(orderRepository, times(0)).save(any(Order.class));
        verify(orderRequestRepository, times(0)).save(any(OrderRequestJob.class));
        verify(orderOutBoxRepository, times(0)).save(any(OrderOutBox.class));
        verify(notificationService, times(0)).sendOrderCreationEvent(any(Order.class));
    }

    //--------------------------------------------------------------------------------------------------------//
    @Test
    @DisplayName("test update successfully with sufficient quantity request " +
            "when cache is NOT EMPTY and product is unchanged")
    void test_update_success_1() {
        when(cacheService.getFromCache(anyString()))
                .thenReturn(order);
        when(orderRepository.save(any(Order.class)))
                .thenReturn(order);
        doNothing()
                .when(cacheService).putIntoCache(anyString(), any(Order.class));

        String result = assertDoesNotThrow(() -> orderService.update(uuid, orderRequest));

        assertTrue(result.contains(String.valueOf(order.getId())));//result returns text with order id.
        verify(cacheService, times(1)).getFromCache(anyString());
        verify(orderRepository, times(0)).findById(any(UUID.class));
        verify(productService, times(0)).getAvailableProduct(anyInt());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(cacheService, times(1)).putIntoCache(anyString(), any(Order.class));
    }
    @Test
    @DisplayName("test update successfully with sufficient quantity request " +
            "when cache is EMPTY and product is unchanged")
    void test_update_success_2() {
        when(cacheService.getFromCache(anyString()))
                .thenReturn(null);

        when(orderRepository.save(any(Order.class)))
                .thenReturn(order);
        when(orderRepository.findById(any(UUID.class)))
                .thenReturn(Optional.ofNullable(order));
        doNothing()
                .when(cacheService).putIntoCache(anyString(), any(Order.class));

        String result = assertDoesNotThrow(() -> orderService.update(uuid, orderRequest));

        assertTrue(result.contains(String.valueOf(order.getId())));//result returns text with order id.
        verify(cacheService, times(1)).getFromCache(anyString());
        verify(orderRepository, times(1)).findById(any(UUID.class));
        verify(productService, times(0)).getAvailableProduct(anyInt());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(cacheService, times(1)).putIntoCache(anyString(), any(Order.class));
    }
    @Test
    @DisplayName("test update successfully with sufficient quantity request " +
            "when cache is empty and product is CHANGED")
    void test_update_success_3() {
        //change product manually
        product.setId(product.getId()+1);
        order.setProduct(product);

        when(cacheService.getFromCache(anyString()))
                .thenReturn(null);

        when(orderRepository.save(any(Order.class)))
                .thenReturn(order);
        when(orderRepository.findById(any(UUID.class)))
                .thenReturn(Optional.ofNullable(order));
        when(productService.getAvailableProduct(anyInt()))
                .thenReturn(product);
        doNothing()
                .when(cacheService).putIntoCache(anyString(), any(Order.class));

        String result = assertDoesNotThrow(() -> orderService.update(uuid, orderRequest));

        assertTrue(result.contains(String.valueOf(order.getId())));//result returns text with order id.
        verify(cacheService, times(1)).getFromCache(anyString());
        verify(orderRepository, times(1)).findById(any(UUID.class));
        verify(productService, times(1)).getAvailableProduct(anyInt());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(cacheService, times(1)).putIntoCache(anyString(), any(Order.class));
    }

    @Test
    @DisplayName("test update with failure when insufficient quantity request")
    void test_update_failure() {
        when(cacheService.getFromCache(anyString()))
                .thenReturn(null);
        when(orderRepository.findById(any(UUID.class)))
                .thenThrow(new OrderNotFoundException(""));

        assertThrows(OrderNotFoundException.class, () -> orderService.update(uuid, orderRequest));

        verify(cacheService, times(1)).getFromCache(anyString());
        verify(orderRepository, times(1)).findById(any(UUID.class));
        verify(productService, times(0)).getAvailableProduct(anyInt());
        verify(orderRepository, times(0)).save(any(Order.class));
        verify(cacheService, times(0)).putIntoCache(anyString(), any(Order.class));
    }

    //--------------------------------------------------------------------------------------------------------//
    @Test
    void test_deleteMyOrder_success() {
        doNothing().when(orderRepository).deleteById(any(UUID.class));
        doNothing().when(orderOutBoxRepository).deleteByOrder(any(UUID.class));
        doNothing().when(cacheService).removeFromCache(anyString());

        assertDoesNotThrow(() -> orderService.deleteMyOrder(uuid));

        verify(orderRepository, times(1)).deleteById(any(UUID.class));
        verify(orderOutBoxRepository, times(1)).deleteByOrder(any(UUID.class));
        verify(cacheService, times(1)).removeFromCache(anyString());
    }

    @Test
    void test_deleteMyOrder_failure() {
        doThrow(OrderNotFoundException.class).when(orderRepository).deleteById(any(UUID.class));

        assertThrows(OrderNotFoundException.class, () -> orderService.deleteMyOrder(uuid));

        verify(orderRepository, times(1)).deleteById(any(UUID.class));
        verify(orderOutBoxRepository, times(0)).deleteByOrder(any(UUID.class));
        verify(cacheService, times(0)).removeFromCache(anyString());
    }
}