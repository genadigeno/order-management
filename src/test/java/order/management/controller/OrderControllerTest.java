package order.management.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import order.management.dto.OrderDto;
import order.management.dto.PageDto;
import order.management.dto.http.OrderRequest;
import order.management.dto.http.OrderResponse;
import order.management.exception.InsufficientQuantityException;
import order.management.exception.OrderNotFoundException;
import order.management.service.OrderService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.github.dockerjava.core.MediaType;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    OrderService orderService;

    /*@MockBean
    JwtUtil jwtUtil;

    @MockBean
    UserDetailsServiceImpl userDetailsService;

    @MockBean
    AuthenticationServiceImpl authenticationService;*/

    private static OrderRequest request;

    @BeforeAll
    public static void init(){
        request = new OrderRequest();
        request.setPrice(BigDecimal.ONE);
        request.setUserId(1);
        request.setProductId(1);
        request.setQuantity(1);
    }

    @Test
//    @WithMockUser(username = "user", password = "123")
    void test_getMyOrders_with_success() throws Exception {
        when(orderService.getMyOrders(anyInt(), anyInt()))
                .thenReturn(PageDto.builder().build());

        mockMvc.perform(get("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON.getMediaType()))
                .andExpect(status().isOk());
    }

    @Test
//    @WithMockUser(username = "user", password = "123")
    void test_getMyOrders_with_failure_1() throws Exception {
        when(orderService.getMyOrders(anyInt(), anyInt()))
                .thenThrow(new OrderNotFoundException(""));

        mockMvc.perform(get("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON.getMediaType()))
                .andExpect(status().isBadRequest());
    }

    @Test
//    @WithMockUser(username = "user", password = "123")
    void test_getMyOrders_with_failure_2() throws Exception {
        when(orderService.getMyOrders(anyInt(), anyInt()))
                .thenThrow(new RuntimeException(""));

        mockMvc.perform(get("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON.getMediaType()))
                .andExpect(status().isInternalServerError());
    }
    //--------------------------------------------------------------------------------------------------------//

    @Test
//    @WithMockUser(username = "user", password = "123")
    void test_getMyOrder_with_success() throws Exception {
        when(orderService.getMyOrder(anyString()))
                .thenReturn(OrderDto.builder().build());

        mockMvc.perform(get("/api/orders/1")
                        .contentType(MediaType.APPLICATION_JSON.getMediaType()))
                .andExpect(status().isOk());
    }

    @Test
//    @WithMockUser(username = "user", password = "123")
    void test_getMyOrder_with_failure() throws Exception {
        when(orderService.getMyOrder(anyString()))
                .thenThrow(new OrderNotFoundException(""));

        mockMvc.perform(get("/api/orders/1")
                        .contentType(MediaType.APPLICATION_JSON.getMediaType()))
                .andExpect(status().isBadRequest());
    }

    //--------------------------------------------------------------------------------------------------------//
    @Test
//    @WithMockUser(username = "user", password = "123")
    void test_createOrder_success() throws Exception {
        when(orderService.createOrder(any(OrderRequest.class)))
                .thenReturn(OrderResponse.builder()
                        .message("order placed")
                        .build());

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON.getMediaType())
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
//    @WithMockUser(username = "user", password = "123")
    void test_createOrder_failure() throws Exception {
        when(orderService.createOrder(any(OrderRequest.class)))
                .thenThrow(new InsufficientQuantityException());

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON.getMediaType())
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    //--------------------------------------------------------------------------------------------------------//
    @Test
//    @WithMockUser(username = "user", password = "123")
    void test_update_success() throws Exception {
        when(orderService.update(anyString(), any(OrderRequest.class)))
                .thenReturn("Ok");

        mockMvc.perform(put("/api/orders/1")
                        .contentType(MediaType.APPLICATION_JSON.getMediaType())
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
//    @WithMockUser(username = "user", password = "123")
    void test_update_failure() throws Exception {
        when(orderService.update(anyString(), any(OrderRequest.class)))
                .thenThrow(new OrderNotFoundException(""));

        mockMvc.perform(put("/api/orders/1")
                        .contentType(MediaType.APPLICATION_JSON.getMediaType())
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    //--------------------------------------------------------------------------------------------------------//
    @Test
//    @WithMockUser(username = "user", password = "123")
    void test_deleteMyOrder_success() throws Exception {
        when(orderService.deleteMyOrder(anyString()))
                .thenReturn("Ok");

        mockMvc.perform(delete("/api/orders/1")
                        .contentType(MediaType.APPLICATION_JSON.getMediaType()))
                .andExpect(status().isOk());
    }

    @Test
//    @WithMockUser(username = "user", password = "123")
    void test_deleteMyOrder_failure() throws Exception {
        when(orderService.deleteMyOrder(anyString()))
                .thenThrow(new OrderNotFoundException(""));

        mockMvc.perform(delete("/api/orders/1")
                        .contentType(MediaType.APPLICATION_JSON.getMediaType()))
                .andExpect(status().isBadRequest());
    }
}
