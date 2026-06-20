package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.order.service.domain.dto.create.CreateOrderCommand;
import com.food.ordering.system.order.service.domain.dto.create.CreateOrderResponse;
import com.food.ordering.system.order.service.domain.event.OrderCreatedEvent;
import com.food.ordering.system.order.service.domain.mapper.OrderDataMapper;
import com.food.ordering.system.order.service.domain.outbox.scheduler.payment.PaymentOutboxHelper;
import com.food.ordering.system.outbox.OutboxStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
public class OrderCreateCommandHandler {


    private final OrderDataMapper orderDataMapper;
    private final OrderCreateHelper orderCreateHelper;
    private final OrderSagaHelper orderSagaHelper;
    private final PaymentOutboxHelper paymentOutboxHelper;

    public OrderCreateCommandHandler(OrderDataMapper orderDataMapper, OrderCreateHelper orderCreateHelper, OrderSagaHelper orderSagaHelper, PaymentOutboxHelper paymentOutboxHelper){
        this.orderDataMapper = orderDataMapper;
        this.orderCreateHelper = orderCreateHelper;
        this.orderSagaHelper = orderSagaHelper;
        this.paymentOutboxHelper = paymentOutboxHelper;
    }

    @Transactional
    public CreateOrderResponse createOrder(CreateOrderCommand createOrderCommand){
        OrderCreatedEvent orderCreatedEvent = orderCreateHelper.persistOrder(createOrderCommand);
        log.info("Order with Id is created {}", orderCreatedEvent.getOrder().getId().getValue());
        CreateOrderResponse createOrderResponse =  orderDataMapper.orderToCreateOrderResponse(orderCreatedEvent.getOrder(),"order created successfully");

        paymentOutboxHelper.savePaymentOutboxMessage(orderDataMapper
                    .orderCreatedEventToOrderPaymentEventPayload(orderCreatedEvent),
                    orderCreatedEvent.getOrder().getOrderStatus(),
                    orderSagaHelper.orderStatusToSagaStatus(orderCreatedEvent.getOrder().getOrderStatus()),
                    OutboxStatus.STARTED,
                    UUID.randomUUID());

        log.info("Returning CreateOrderResponse with order id : {} " , orderCreatedEvent.getOrder().getId());
        return createOrderResponse;
    }


}
