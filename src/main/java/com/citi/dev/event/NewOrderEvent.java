package com.citi.dev.event;

import com.citi.dev.model.Order;

//input NEW  request with  order to be inserted
public class NewOrderEvent implements InputEvent {

    private final Order order;

    public NewOrderEvent(Order order){
        this.order = order;
    }

    @Override
    public RequestType getRequestType() {
        return RequestType.NEW;
    }

    @Override
    public Order getOrder() {
        return order;
    }
}
