package com.boolck.dev.event;

import com.boolck.dev.model.Order;

//input UPDATE  request with original order and new price & qty to be updated
public class UpdateOrderEvent implements InputEvent {
    private final Order order;
    public double newPrice;
    public long newQty;

    public UpdateOrderEvent(Order existingOrder, double newPrice, long newQty){
        this.order = existingOrder;
        this.newPrice = newPrice;
        this.newQty = newQty;
    }

    @Override
    public RequestType getRequestType() {
        return RequestType.UPDATE;
    }

    @Override
    public Order getOrder() {
        return order;
    }
}
