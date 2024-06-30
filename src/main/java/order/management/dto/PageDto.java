package order.management.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Collection;

@Data
@Builder
public class PageDto {
    private Collection<?> data;
    private long total;
}
