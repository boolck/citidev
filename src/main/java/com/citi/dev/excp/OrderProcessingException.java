package com.citi.dev.excp;

//exception to capture any internal order processing issues from order book engine
public class OrderProcessingException extends Exception {

    public OrderProcessingException(String msg){
        super(msg);
    }
}
