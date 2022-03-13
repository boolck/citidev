package com.citi.dev.excp;

//exception to capture any incorrect order event issues
public class InvalidOrderException extends Exception{

    public InvalidOrderException(String msg){
        super(msg);
    }

    public InvalidOrderException(String msg, Throwable cause){
        super(msg,cause);
    }

}
