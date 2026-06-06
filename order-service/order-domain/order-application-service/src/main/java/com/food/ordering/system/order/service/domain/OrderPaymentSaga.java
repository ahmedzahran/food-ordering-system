package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.domain.event.EmptyEvent;
import com.food.ordering.system.domain.valueobject.OrderId;
import com.food.ordering.system.order.service.domain.dto.message.PaymentResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import com.food.ordering.system.order.service.domain.exception.OrderNotFoundException;
import com.food.ordering.system.order.service.domain.ports.output.message.publisher.restaurantApproval.OrderPaidRestaurantRequestMessagePublisher;
import com.food.ordering.system.order.service.domain.ports.output.repository.OrderRepository;
import com.food.ordering.system.saga.SagaStep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
@Slf4j
public class OrderPaymentSaga implements SagaStep<PaymentResponse, OrderPaidEvent, EmptyEvent> {

    private final OrderDomainService orderDomainService;
    private final OrderSagaHelper orderSagaHelper;
    private final OrderPaidRestaurantRequestMessagePublisher orderPaidRestaurantRequestMessagePublisher;

    public OrderPaymentSaga(OrderDomainService orderDomainService,
                            OrderSagaHelper orderSagaHelper,
                            OrderPaidRestaurantRequestMessagePublisher orderPaidRestaurantRequestMessagePublisher) {
        this.orderDomainService = orderDomainService;
        this.orderSagaHelper = orderSagaHelper;
        this.orderPaidRestaurantRequestMessagePublisher = orderPaidRestaurantRequestMessagePublisher;
    }

    @Override
    @Transactional
    public OrderPaidEvent proccess(PaymentResponse paymentResponse) {
        log.info("Completing Payment for order with id: {} ", paymentResponse.getOrderId());
        Order order = orderSagaHelper.findOrder(paymentResponse.getOrderId());
        OrderPaidEvent orderPaidEvent = orderDomainService.payOrder(order,orderPaidRestaurantRequestMessagePublisher);
        orderSagaHelper.saveOrder(order);

        log.info("Order with Id : {} is Paid ", paymentResponse.getOrderId() );

        return orderPaidEvent;
    }



    @Override
    @Transactional
    public EmptyEvent rollback(PaymentResponse paymentResponse) {
        log.info("Cancelling order with id : {} " , paymentResponse.getOrderId());
        Order order = orderSagaHelper.findOrder(paymentResponse.getOrderId());
        orderDomainService.cancelOrder(order,paymentResponse.getFailureMessages());
        orderSagaHelper.saveOrder(order);
        log.info("Order with Id : {} is Cancelled ", paymentResponse.getOrderId() );

        return EmptyEvent.INSTANCE;
    }

}
