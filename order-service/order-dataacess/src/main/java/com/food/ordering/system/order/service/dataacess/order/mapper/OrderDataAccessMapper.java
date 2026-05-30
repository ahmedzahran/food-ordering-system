package com.food.ordering.system.order.service.dataacess.order.mapper;

import com.food.ordering.system.domain.valueobject.*;
import com.food.ordering.system.order.service.dataacess.order.entity.OrderAddressEntity;
import com.food.ordering.system.order.service.dataacess.order.entity.OrderEntity;
import com.food.ordering.system.order.service.dataacess.order.entity.OrderItemEntity;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.OrderItem;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.valueobject.OrderItemId;
import com.food.ordering.system.order.service.domain.valueobject.StreetAddress;
import com.food.ordering.system.order.service.domain.valueobject.TrackingId;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.food.ordering.system.order.service.domain.entity.Order.FAILURE_MESSAGE_DELIMITER;

@Component
public class OrderDataAccessMapper {


    public OrderEntity orderToOrderEntity(Order order){
        OrderEntity orderEntity = OrderEntity.builder()
                .id(order.getId().getValue())
                .customerId(order.getCustomerId().getValue())
                .restaurantId(order.getRestaurantId().getValue())
                .trackingId(order.getTrackingId().getValue())
                .address(deliveryAddersToAddressEntity(order.getDeliveryAddress()))
                .price(order.getPrice().getAmount())
                .items(orderItemsToOrderItemEntities(order.getItems()))
                .orderStatus(order.getOrderStatus())
                .failureMessages(order.getFailureMessages() != null ?
                        String.join(FAILURE_MESSAGE_DELIMITER,order.getFailureMessages()) : "")
                .build();
        orderEntity.getAddress().setOrder(orderEntity);
        orderEntity.getItems().forEach(orderItemEntity -> {
            orderItemEntity.setOrder(orderEntity);
        });

        return orderEntity;
    }

    public Order orderEntityToOrder(OrderEntity orderEntity){
        return Order.builder()
                .orderId(new OrderId(orderEntity.getId()))
                .customerId(new CustomerId(orderEntity.getCustomerId()))
                .restaurantId(new RestaurantId(orderEntity.getRestaurantId()))
                .trackingId(new TrackingId(orderEntity.getTrackingId()))
                .deliveryAddress(addressEntityToDeliveryAddress(orderEntity.getAddress()))
                .price(new Money(orderEntity.getPrice()))
                .items(orderItemEntitiesToOrderItems(orderEntity.getItems()))
                .orderStatus(orderEntity.getOrderStatus())
                .failureMessages(orderEntity.getFailureMessages().isEmpty() ? new ArrayList<>() :
                        new ArrayList<>(Arrays.asList(orderEntity.getFailureMessages().split(FAILURE_MESSAGE_DELIMITER))))
                .build();
    }

    private List<OrderItem> orderItemEntitiesToOrderItems(List<OrderItemEntity> items) {
        return items
                .stream()
                .map(orderItemEntity -> {
                    return OrderItem.builder()
                            .orderItemId(new OrderItemId(orderItemEntity.getId()))
                            .product(new Product(new ProductId(orderItemEntity.getProductId())))
                            .price(new Money(orderItemEntity.getPrice()))
                            .quantity(orderItemEntity.getQuantity())
                            .subTotal(new Money(orderItemEntity.getSubTotal()))
                            .build();
                }).toList();
    }

    private StreetAddress addressEntityToDeliveryAddress(OrderAddressEntity address) {
        return new StreetAddress(address.getId(),address.getStreet(),address.getPostalCode(), address.getCity());
    }

    private List<OrderItemEntity> orderItemsToOrderItemEntities(List<OrderItem> items) {
        return items
                .stream()
                .map(item -> {
                    return OrderItemEntity.builder()
                            .id(item.getId().getValue())
                            .productId(item.getProduct().getId().getValue())
                            .price(item.getPrice().getAmount())
                            .quantity(item.getQuantity())
                            .subTotal(item.getSubTotal().getAmount())
                            .build();
                }).toList();
    }

    private OrderAddressEntity deliveryAddersToAddressEntity(StreetAddress deliveryAddress) {
        return OrderAddressEntity
                .builder()
                .id(deliveryAddress.getId())
                .city(deliveryAddress.getCity())
                .postalCode(deliveryAddress.getPostalCode())
                .street(deliveryAddress.getStreet())
                .build();
    }
}
