package com.boolck.dev.excp;

//exception to capture any file reader issues
public class InputReadException extends Exception{

    public InputReadException(String msg,Throwable cause){
        super(msg,cause);
    }

}
