package com.citi.dev.listener;

import com.citi.dev.calc.OrderBookEngine;
import com.citi.dev.excp.InputReadException;
import com.citi.dev.excp.InvalidOrderException;
import com.citi.dev.excp.OrderProcessingException;

//Source listener interface to pass  request via order book engine
@FunctionalInterface
public interface SourceListener {

    void process(OrderBookEngine orderBookEngine) throws OrderProcessingException, InputReadException, InvalidOrderException;

}
