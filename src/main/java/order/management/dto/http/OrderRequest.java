package order.management.dto.http;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderRequest {
    @Positive(message = "userId must be greater than 0")
    private int userId;
    @Positive(message = "productId must be greater than 0")
    private int productId;
    @Positive(message = "quantity must be greater than 0")
    private int quantity;
    @PositiveOrZero(message = "price must not be negative")
    private BigDecimal price;
}
