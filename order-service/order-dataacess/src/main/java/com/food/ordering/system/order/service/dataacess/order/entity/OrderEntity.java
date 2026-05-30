package com.food.ordering.system.order.service.dataacess.order.entity;

import com.food.ordering.system.domain.valueobject.OrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
@Entity
public class OrderEntity {

    @Id
    private UUID id;

    private UUID customerId;
    private UUID restaurantId;
    private UUID trackingId;
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    private String failureMessages;

    @OneToOne(mappedBy = "order",cascade = CascadeType.ALL)
    private OrderAddressEntity address;

    @OneToMany(mappedBy = "order",cascade = CascadeType.ALL)
    private List<OrderItemEntity> items;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderEntity that = (OrderEntity) o;
        return  id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
