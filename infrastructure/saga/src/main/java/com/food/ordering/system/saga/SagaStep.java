package com.food.ordering.system.saga;

import com.food.ordering.system.domain.event.DomainEvent;

public interface SagaStep<T> {

    void proccess(T data);
    void rollback(T data);

}
