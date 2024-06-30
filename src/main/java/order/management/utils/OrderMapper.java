package order.management.utils;


import order.management.dto.OrderDto;
import order.management.dto.http.OrderRequest;
import order.management.model.Order;
import order.management.model.Status;

import java.util.UUID;

public final class OrderMapper {
    public static OrderDto map(Order order){
        return OrderDto.builder()
                .uuid(order.getId().toString())
                .userId(order.getUserId())
                .product(order.getProduct().getName())
                .quantity(order.getQuantity())
                .orderedAt(order.getCreated())
                .price(order.getPrice())
                .status(order.getStatus().name())
                .build();
    }

    public static Order map(OrderRequest request) {
        Order order = new Order();
        order.setQuantity(request.getQuantity());
        order.setStatus(Status.PENDING);
        return order;
    }

    /*public static Order map(OrderRequest request) {
        Order user = new Order();
        return user;
    }*/

    public static void map(OrderRequest request, Order current) {
        current.setPrice(request.getPrice());
        current.setQuantity(request.getQuantity());
        current.setUserId(request.getUserId());
    }

}
