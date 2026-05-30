package com.food.ordering.system.kafka.producer.service.impl;

import com.food.ordering.system.kafka.producer.service.KafkaProducer;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

@Component
@Slf4j
public class KafkaProducerImpl<
        K extends Serializable,
        V extends SpecificRecordBase>
        implements KafkaProducer<K, V> {

    private final KafkaTemplate<K, V> kafkaTemplate;

    public KafkaProducerImpl(KafkaTemplate<K, V> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public CompletableFuture<SendResult<K, V>> send(
            String topicName,
            K key,
            V message,
            BiConsumer<SendResult<K, V>, Throwable> callback
    ) {

        log.info("Sending message={} to topic={}", message, topicName);
        CompletableFuture<SendResult<K, V>> future = kafkaTemplate.send(topicName, key, message);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error(
                        "Error sending message. key={}, message={}",
                        key,
                        message,
                        ex
                );
            }

            if (callback != null) {
                callback.accept(result, ex);
            }
        });
        return future;
    }

    @PreDestroy
    public void close(){
        if (kafkaTemplate != null){
            log.info("Closing Kafka producer");
            kafkaTemplate.destroy();
        }
    }
}