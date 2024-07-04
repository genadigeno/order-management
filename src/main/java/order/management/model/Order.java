package order.management.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders", schema = "gvggroup")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter
@NamedEntityGraph(name="order-entity-graph", attributeNodes=@NamedAttributeNode("product"))
public class Order {
    @Id
    @UuidGenerator
    private UUID id;

    private int userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private int quantity;

    @Enumerated(EnumType.STRING)
    private Status status;

    @OneToMany(mappedBy="order", cascade=CascadeType.ALL, orphanRemoval=true)
//    @JsonBackReference
    private List<OrderRequestJob> orderRequests;

    private BigDecimal price;

    @CreatedDate
    private LocalDateTime created;

    @LastModifiedDate
    private LocalDateTime modified;
}
