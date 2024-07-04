package order.management.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders_outbox_table", schema = "gvggroup")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
public class OrderOutBox {
    @Id
    @GeneratedValue
    private int id;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @CreatedDate
    private LocalDateTime created;
}
