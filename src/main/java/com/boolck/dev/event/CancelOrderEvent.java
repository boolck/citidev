package com.boolck.dev.event;

import com.boolck.dev.model.Order;

//input CANCEL  request with  order to be cancelled
public class CancelOrderEvent implements InputEvent {
    private final Order order;
    public CancelOrderEvent(Order cancelledOrder){
        this.order = cancelledOrder;
    }

    @Override
    public RequestType getRequestType() {
        return RequestType.CANCEL;
    }

    @Override
    public Order getOrder() {
        return order;
    }


}
