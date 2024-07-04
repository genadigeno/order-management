package order.management.dto.http;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderResponse {
    private String message;
    private String orderId;
}
