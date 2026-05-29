package com.food.ordering.system.order.service.domain.mapper;

import com.food.ordering.system.domain.valueobject.CustomerId;
import com.food.ordering.system.domain.valueobject.Money;
import com.food.ordering.system.domain.valueobject.ProductId;
import com.food.ordering.system.domain.valueobject.RestaurantId;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.dto.create.OrderAddress;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.OrderItem;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.entity.Restaurant;
import com.food.ordering.system.order.service.domain.valueobject.StreetAddress;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class OrderDataMapper {


    public CreateOrderResponse orderToCreateOrderResponse(Order order){
        CreateOrderResponse.builder()
                .orderStatus(order.getOrderStatus())
                .orderTrackingId(order.getTrackingId().getValue())
                .build();
    }
    public Restaurant createOrderCommandToRestaurant(CreateOrderCommand createOrderCommand){
        return Restaurant.builder()
                .restaurantId(new RestaurantId(createOrderCommand.getRestaurantId()))
                .products(createOrderCommand.getItems().stream().map(orderItem -> {
                    return new Product(new ProductId(orderItem.getProductId()));
                }).collect(Collectors.toList()))
                .build();
    }

    public Order createOrderComaandToOrder(CreateOrderCommand createOrderCommand){
        return Order
                .builder()
                .customerId(new CustomerId(createOrderCommand.getCustomerId()))
                .restaurantId(new RestaurantId(createOrderCommand.getRestaurantId()))
                .deliveryAddress(orderAddressToStreetAddress(createOrderCommand.getAddress()))
                .price(new Money(createOrderCommand.getPrice()))
                .items(orderItemsToOrderItemsEntity(createOrderCommand.getItems()))
                .build();
    }

    private List<OrderItem> orderItemsToOrderItemsEntity(List<com.food.ordering.system.order.service.domain.dto.create.OrderItem> orderItems) {
        return orderItems
                .stream()
                .map(orderItem -> {
                    return OrderItem.builder()
                            .product(new Product(new ProductId(orderItem.getProductId())))
                            .price(new Money(orderItem.getPrice()))
                            .quantity(orderItem.getQuantity())
                            .subTotal(new Money(orderItem.getSubtotal()))
                            .build();
                }).toList();
    }

    private StreetAddress orderAddressToStreetAddress(OrderAddress orderAddress) {

        return new StreetAddress(UUID.randomUUID(),orderAddress.getStreet(),orderAddress.getPostalCode(),orderAddress.getCity());
    }
}
