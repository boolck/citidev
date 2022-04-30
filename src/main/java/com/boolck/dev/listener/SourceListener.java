package com.boolck.dev.listener;

import com.boolck.dev.excp.InvalidOrderException;
import com.boolck.dev.excp.OrderProcessingException;
import com.boolck.dev.calc.OrderBookEngine;
import com.boolck.dev.excp.InputReadException;

//Source listener interface to pass  request via order book engine
@FunctionalInterface
public interface SourceListener {

    void process(OrderBookEngine orderBookEngine) throws OrderProcessingException, InputReadException, InvalidOrderException;

}
