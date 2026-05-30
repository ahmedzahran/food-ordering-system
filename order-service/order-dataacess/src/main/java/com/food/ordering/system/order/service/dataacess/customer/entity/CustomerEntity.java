package com.food.ordering.system.order.service.dataacess.customer.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "order_customer_m_view",schema = "customer")
@Entity
public class CustomerEntity {

    @Id
    private UUID id;
}
