package com.food.ordering.system.order.service.messaging.listener.kafka;

import com.food.ordering.system.kafka.consumer.KafkaConsumer;
import com.food.ordering.system.kafka.order.avro.model.OrderApprovalStatus;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalResponseModel;
import com.food.ordering.system.order.service.domain.ports.input.message.listener.restaurant.RestaurantApprovalResponseMessageListener;
import com.food.ordering.system.order.service.messaging.mapper.OrderMessagingDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.food.ordering.system.order.service.domain.entity.Order.FAILURE_MESSAGE_DELIMITER;

@Component
@Slf4j
public class RestaurantApprovalResponseKafkaListener implements KafkaConsumer<RestaurantApprovalResponseModel> {

    private final RestaurantApprovalResponseMessageListener restaurantApprovalResponseMessageListener;
    private final OrderMessagingDataMapper orderMessagingDataMapper;

    public RestaurantApprovalResponseKafkaListener(RestaurantApprovalResponseMessageListener restaurantApprovalResponseMessageListener, OrderMessagingDataMapper orderMessagingDataMapper) {
        this.restaurantApprovalResponseMessageListener = restaurantApprovalResponseMessageListener;
        this.orderMessagingDataMapper = orderMessagingDataMapper;
    }


    @Override
    @KafkaListener(id =  "${kafka-consumer-config.restaurant-approval-consumer-group-id}",topics = "${order-service.restauran-approval-response-topic-name}")
    public void receive(@Payload List<RestaurantApprovalResponseModel> messages,
                        @Header(KafkaHeaders.RECEIVED_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION)  List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets
    ) {

        log.info("{} number of restaurant approval responses received with keys : {} , and partitions : {} and offset : {}",
                messages.size(),
                keys.toString(),
                partitions.toString(),
                offsets.toString());

        messages.forEach(restaurantApprovalResponseModel -> {
            if (OrderApprovalStatus.APPROVED == restaurantApprovalResponseModel.getOrderApprovalStatus()){
                log.info("Processing Approved order for order id : {} ",restaurantApprovalResponseModel.getOrderId());
                restaurantApprovalResponseMessageListener.orderApproved(orderMessagingDataMapper.approvalResponseAvroModelToRestaurantApprovalResponse(restaurantApprovalResponseModel));
            } else if (OrderApprovalStatus.REJECTED == restaurantApprovalResponseModel.getOrderApprovalStatus()) {
                log.info("Processing Rejected order for order id : {} with failure messages : {}"
                        ,restaurantApprovalResponseModel.getOrderId(),String.join(FAILURE_MESSAGE_DELIMITER,restaurantApprovalResponseModel.getFailureMessages()));
                restaurantApprovalResponseMessageListener.orderRejected(orderMessagingDataMapper
                        .approvalResponseAvroModelToRestaurantApprovalResponse(restaurantApprovalResponseModel));

            }
        });
    }
}
