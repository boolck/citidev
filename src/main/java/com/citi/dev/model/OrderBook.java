package com.citi.dev.model;

import com.citi.dev.event.*;

import java.io.Serializable;
import java.util.Objects;

//input OrderBook, representing a row in input_orders.csv
public class OrderBook implements Serializable {
    private String seqNum;
    private String addOrderId;
    private String addSide;
    private double addPrice;
    private long addQty;
    private String updateOrderId;
    private String updateSide;
    private double updatePrice;
    private long updateQty;
    private String deleteOrderId;
    private String deleteSide;
    private String time;

    public String getSeqNum() {
        return seqNum;
    }

    public void setSeqNum(String seqNum) {
        this.seqNum = seqNum;
    }

    public void setAddOrderId(String addOrderId) {
        this.addOrderId = addOrderId;
    }

    public void setAddSide(String addSide) {
        this.addSide = addSide;
    }

    public void setAddPrice(double addPrice) {
        this.addPrice = addPrice;
    }

    public void setAddQty(long addQty) {
        this.addQty = addQty;
    }

    public void setUpdateOrderId(String updateOrderId) {
        this.updateOrderId = updateOrderId;
    }

    public void setUpdateSide(String updateSide) {
        this.updateSide = updateSide;
    }

    public void setUpdatePrice(double updatePrice) {
        this.updatePrice = updatePrice;
    }

    public void setUpdateQty(long updateQty) {
        this.updateQty = updateQty;
    }

    public void setDeleteOrderId(String deleteOrderId) {
        this.deleteOrderId = deleteOrderId;
    }

    public void setDeleteSide(String deleteSide) {
        this.deleteSide = deleteSide;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public InputEvent getInputRequest() {
        InputEvent request = null;
        if(addOrderId != null){
            Order.OrderBuilder orderBuilder = new Order.OrderBuilder();
            Order order = orderBuilder.id(this.addOrderId)
                    .side(Order.Side.valueOf(addSide))
                    .price(addPrice)
                    .qty(addQty)
                    .seqNum(seqNum)
                    .timestamp(time).build();
            request =  new NewOrderEvent(order);
        }
        else if(updateOrderId != null){
            Order.OrderBuilder orderBuilder = new Order.OrderBuilder();
            Order order = orderBuilder.id(this.updateOrderId)
                    .side(Order.Side.valueOf(updateSide))
                    .price(updateQty)
                    .qty(updateQty)
                    .seqNum(seqNum)
                    .timestamp(time).build();
            request = new UpdateOrderEvent(order, updatePrice, updateQty);
        }
        else if(deleteOrderId != null){
            Order.OrderBuilder orderBuilder = new Order.OrderBuilder();
            Order order = orderBuilder.id(this.deleteOrderId)
                    .side(Order.Side.valueOf(deleteSide))
                    .seqNum(seqNum)
                    .timestamp(time).build();
            request = new CancelOrderEvent(order);
        }
        Objects.requireNonNull(request);
        return request;
    }

}
