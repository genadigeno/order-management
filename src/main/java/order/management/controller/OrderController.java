package order.management.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import order.management.dto.OrderDto;
import order.management.dto.PageDto;
import order.management.dto.http.OrderRequest;
import order.management.service.OrderService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
//TODO: Add JWT Token description
public class OrderController {
    private final OrderService orderService;

    @Tag(name = "Get the orders", description = "GET orders endpoint")
    @GetMapping
    public PageDto orders(@RequestParam(defaultValue="10") int size, @RequestParam(defaultValue="0") int page){
        return orderService.getMyOrders(size, page);
    }

    @Tag(name = "Get a order", description = "GET orders endpoint")
    @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json",
                    schema = @Schema(implementation = OrderDto.class)) })
    @ApiResponse(responseCode = "404", description = "Order not found", content = @Content)
    @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
    @GetMapping("/{id}")
    public OrderDto order(@PathVariable String id){
        return orderService.getMyOrder(id);
    }

    @Tag(name = "Create a order", description = "Create order endpoint")
    @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json",
            schema = @Schema(implementation = String.class)) })
    @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = "",
            content = @Content(schema = @Schema(anyOf = OrderRequest.class)))
    @PostMapping
    public String create(@Validated @RequestBody OrderRequest request){
        return orderService.createOrder(request);
    }

    @Tag(name = "Update a order", description = "Update order endpoint")
    @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json",
            schema = @Schema(implementation = String.class)) })
    @ApiResponse(responseCode = "404", description = "Order not found", content = @Content)
    @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
    @PutMapping("/{id}")
    public String update(@PathVariable String id, @Validated @RequestBody OrderRequest request){
        return orderService.update(id, request);
    }

    @Tag(name = "Delete a order", description = "Delete orderorder endpoint")
    @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json",
            schema = @Schema(implementation = String.class)) })
    @ApiResponse(responseCode = "404", description = "Order not found", content = @Content)
    @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
    @DeleteMapping("/{id}")
    public String delete(@PathVariable String id){
        return orderService.deleteMyOrder(id);
    }
}
