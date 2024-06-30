package order.management.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class OrderDto {
    private String uuid;
    private int userId;
    private int quantity;
    private BigDecimal price;
    private String status;
    private String product;
    private LocalDateTime orderedAt;
}
