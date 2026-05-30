package com.food.ordering.system.order.service.messaging.publisher.kafka;

import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.function.BiConsumer;

@Component
@Slf4j
public class OrderKafkaMessageHelper {

    public <T> BiConsumer<SendResult<String, T>, Throwable> getKafkaCallback(
            String topicName,
            String orderId,
            String messageType
    ) {

        return (result, ex) -> {

            if (ex != null) {

                log.error(
                        "Error while sending {} message to topic: {} for order id: {} error: {}",
                        messageType,
                        topicName,
                        orderId,
                        ex.getMessage(),
                        ex
                );

            } else {

                log.info(
                        "{} message sent successfully. topic: {}, partition: {}, offset: {}, orderId: {}",
                        messageType,
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        orderId
                );
            }
        };
    }

//    public BiConsumer<
//            SendResult<String, PaymentRequestAvroModel>,
//            Throwable> getKafkaCallback(
//            String paymentResponseTopicName,
//            PaymentRequestAvroModel paymentRequestAvroModel
//    ) {
//
//        return (result, ex) -> {
//
//            if (ex != null) {
//
//                log.error(
//                        "Error while sending payment request message to topic: {} " +
//                                "for order id: {} error: {}",
//                        paymentResponseTopicName,
//                        paymentRequestAvroModel.getOrderId(),
//                        ex.getMessage(),
//                        ex
//                );
//
//            } else {
//
//                log.info(
//                        "Payment request message sent successfully. " +
//                                "topic: {}, partition: {}, offset: {}, orderId: {}",
//                        result.getRecordMetadata().topic(),
//                        result.getRecordMetadata().partition(),
//                        result.getRecordMetadata().offset(),
//                        paymentRequestAvroModel.getOrderId()
//                );
//            }
//        };
//    }
}
