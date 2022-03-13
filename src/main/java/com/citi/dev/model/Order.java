package com.citi.dev.model;

import com.citi.dev.util.OrderBookRequestFileUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

//input Order which is main processing component of every Request
public class Order {

    public enum Side {
        BUY,
        SELL
    }

    public static final double THRESHOLD_TICK_SIZE = 0.001;
    public static final double THRESHOLD_LOT_SIZE = 100;

    private final String seqNum;
    private final String orderId;
    private final Side side;
    //TODO replace double with BigDecimal
    private double price;
    private long qty;
    private final String timestamp;
    private final BigInteger seqNumAsInt;

    private Order(OrderBuilder builder) {
        this.seqNum = builder.seqNum;
        this.orderId = builder.orderId;
        this.side = builder.side;
        this.price = builder.price;
        this.qty = builder.qty;
        this.timestamp = builder.timestamp;
        seqNumAsInt = new BigDecimal(seqNum).toBigIntegerExact();
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public long getQty() {
        return qty;
    }

    public void setQty(long qty) {
        this.qty = qty;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getSeqNum() {
        return seqNum;
    }

    public BigInteger getSeqNumAsInt(){
        return seqNumAsInt;
    }

    public String getOrderId(){return this.orderId; }

    public Side getSide(){return this.side; }


    @Override
    public int hashCode() {
        return Objects.hash(this.orderId, this.side);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return orderId.equals(order.orderId) &&
                side == order.side ;
    }

    @Override
    public String toString() {
        return "Order{" +
                "seqNum=" + seqNum +
                ", orderId='" + orderId + '\'' +
                ", side=" + side +
                ", price=" + price +
                ", qty=" + qty +
                ", timestamp=" + timestamp +
                '}';
    }

    public boolean isBuy() {
        return Side.BUY==this.side;

    }

    public int compareSeqNum(Order orderInProcess) {
        return this.compareSeqNum(orderInProcess.getSeqNum());
    }

    public int compareSeqNum(String otherSeqNum) {
        return OrderBookRequestFileUtil.compareSeqNum(this.seqNum,otherSeqNum);
    }

    public static class OrderBuilder {
        private String seqNum;
        private String orderId;
        private Side side;
        private double price;
        private long qty;
        private String timestamp;

        public OrderBuilder seqNum(String seqNum) {
            this.seqNum = seqNum;
            return this;
        }

        public OrderBuilder id(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public OrderBuilder side(Side side) {
            this.side = side;
            return this;
        }

        public OrderBuilder price(double price) {
            this.price = price;
            return this;
        }

        public OrderBuilder qty(long qty) {
            this.qty = qty;
            return this;
        }

        public OrderBuilder timestamp(String timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Order build() {
            validateOrder();
            return new Order(this);
        }

        private void validateOrder() {
            Objects.requireNonNull(this.orderId);
            Objects.requireNonNull(this.side);
        }
    }


}
