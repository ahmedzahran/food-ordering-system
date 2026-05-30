package com.food.ordering.system.kafka.producer.service;

import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.kafka.support.SendResult;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public interface KafkaProducer<
        K extends Serializable,
        V extends SpecificRecordBase> {

    CompletableFuture<SendResult<K, V>> send(
            String topicName,
            K key,
            V message,
            BiConsumer<SendResult<K, V>, Throwable> callback
    );
}
