package com.citi.dev.model;

import com.opencsv.bean.CsvBindByPosition;

import java.io.Serializable;
import java.util.Objects;

//output  BBO = BestBidOffer having max buy and min ask
public class BBO implements OutputResponse, Serializable {

    @CsvBindByPosition(position = 0)
    private String timestamp;
    @CsvBindByPosition(position = 1)
    private double bidPrice;
    @CsvBindByPosition(position = 2)
    private double askPrice;
    @CsvBindByPosition(position = 3)
    private long bidQty;
    @CsvBindByPosition(position = 4)
    private long askQty;
    @CsvBindByPosition(position = 5)
    private String seqNum;

    public  BBO(){}

    public  BBO(String seqNum,double bidPrice,long bidQty, double askPrice, long askQty, String timestamp){
        this.seqNum = seqNum;
        this.bidPrice = bidPrice;
        this.bidQty = bidQty;
        this.askPrice = askPrice;
        this.askQty = askQty;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "BBO{" +
                "timestamp=" + timestamp +
                ", bidPrice=" + bidPrice +
                ", askPrice=" + askPrice +
                ", bidQty=" + bidQty +
                ", askQty=" + askQty +
                ", seqNum=" + seqNum +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BBO bbo = (BBO) o;
        return seqNum.equals(bbo.seqNum) &&
                Double.compare(bbo.bidPrice, bidPrice) == 0 &&
                bidQty == bbo.bidQty &&
                Double.compare(bbo.askPrice, askPrice) == 0 &&
                askQty == bbo.askQty ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(seqNum, bidPrice, bidQty, askPrice, askQty);
    }

    public String getSeqNum() {
        return seqNum;
    }

    public void setSeqNum(String seqNum) {
        this.seqNum = seqNum;
    }

    public double getBidPrice() {
        return bidPrice;
    }

    public void setBidPrice(double bidPrice) {
        this.bidPrice = bidPrice;
    }

    public long getBidQty() {
        return bidQty;
    }

    public void setBidQty(long bidQty) {
        this.bidQty = bidQty;
    }

    public double getAskPrice() {
        return askPrice;
    }

    public void setAskPrice(double askPrice) {
        this.askPrice = askPrice;
    }

    public long getAskQty() {
        return askQty;
    }

    public void setAskQty(long askQty) {
        this.askQty = askQty;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

}
