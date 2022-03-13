package com.citi.dev.calc;

import com.citi.dev.event.*;
import com.citi.dev.excp.InvalidOrderException;
import com.citi.dev.excp.OrderProcessingException;
import com.citi.dev.model.BBO;
import com.citi.dev.model.Order;
import static com.citi.dev.model.Order.Side;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class TestOrderBookEngine {

    OrderBookEngine engine = new OrderBookEngine();

    @Test(expected = InvalidOrderException.class)
    public void testInvalidTickOrder() throws OrderProcessingException, InvalidOrderException{
        Order.OrderBuilder orderBuilder = new Order.OrderBuilder();
        Order buy1 = orderBuilder.seqNum("1").id("b1").side(Side.BUY).price(0.00001).qty(100).timestamp("1000").build();
        engine.processRequest(Stream.of(new NewOrderEvent(buy1)));
    }

    @Test(expected = InvalidOrderException.class)
    public void testInvalidLotOrder() throws OrderProcessingException, InvalidOrderException{
        Order.OrderBuilder orderBuilder = new Order.OrderBuilder();
        Order buy1 = orderBuilder.seqNum("1").id("b1").side(Side.BUY).price(1.0).qty(2).timestamp("1000").build();
        engine.processRequest(Stream.of(new NewOrderEvent(buy1)));
    }

    @Test
    public void testNewOrder() throws OrderProcessingException, InvalidOrderException {
        Order.OrderBuilder orderBuilder = new Order.OrderBuilder();
        Order buy1 = orderBuilder.seqNum("1").id("b1").side(Side.BUY).price(10).qty(100).timestamp("1000").build();
        Order buy2 = orderBuilder.seqNum("2").id("b2").side(Side.BUY).price(9).qty(100).timestamp("2000").build();
        Order sel = orderBuilder.seqNum("3").id("s1").side(Side.SELL).price(11).qty(100).timestamp("3000").build();
        Order sell2 = orderBuilder.seqNum("4").id("s1").side(Side.SELL).price(12).qty(100).timestamp("4000").build();
        List<InputEvent> newOrderRequests = Arrays.asList(new NewOrderEvent(buy1), new NewOrderEvent(buy2), new NewOrderEvent(sel), new NewOrderEvent(sell2));
        engine.processRequest(newOrderRequests.stream());
        BBO bbo = engine.getLatestBBO();
        BBO expectedBBO = new BBO("3",10,100,11,100,"3000");
        assertEquals(expectedBBO,bbo);
    }

    @Test
    public void testCancelOrder() throws OrderProcessingException, InvalidOrderException {
        Order.OrderBuilder orderBuilder = new Order.OrderBuilder();
        Order buy1 = orderBuilder.seqNum("1").id("b1").side(Side.BUY).price(10).qty(150).timestamp("1000").build();
        Order buy2 = orderBuilder.seqNum("2").id("b2").side(Side.BUY).price(9).qty(100).timestamp("2000").build();
        Order sel = orderBuilder.seqNum("3").id("s1").side(Side.SELL).price(11).qty(100).timestamp("3000").build();
        List<InputEvent> newOrderRequests = Arrays.asList(new NewOrderEvent(buy1), new NewOrderEvent(buy2), new NewOrderEvent(sel));
        engine.processRequest(newOrderRequests.stream());
        List<InputEvent> cancelRequest = Collections.singletonList(new CancelOrderEvent(buy1));
        engine.processRequest(cancelRequest.stream());
        BBO bbo = engine.getLatestBBO();
        BBO expectedBBO = new BBO("1",9,100,11,100,"1000");
        assertEquals(expectedBBO,bbo);
    }

    @Test
    public void testUpdateOrder() throws OrderProcessingException, InvalidOrderException {
        Order.OrderBuilder orderBuilder = new Order.OrderBuilder();
        Order buy1 = orderBuilder.seqNum("1").id("b1").side(Side.BUY).price(10).qty(100).timestamp("1000").build();
        Order buy2 = orderBuilder.seqNum("2").id("b2").side(Side.BUY).price(9).qty(100).timestamp("2000").build();
        Order sel = orderBuilder.seqNum("3").id("s1").side(Side.SELL).price(11).qty(100).timestamp("3000").build();
        engine.processRequest(Stream.of(new NewOrderEvent(buy1), new NewOrderEvent(buy2), new NewOrderEvent(sel)));
        Order updateOrder = new Order.OrderBuilder().id(buy1.getOrderId()).seqNum("4").side(Side.BUY).price(10).qty(100).timestamp("4000").build();
        engine.processRequest(Stream.of(new UpdateOrderEvent(updateOrder,10.5,100)));
        BBO bbo = engine.getLatestBBO();
        BBO expectedBBO = new BBO("4",10.5,100,11,100,"4000");
        assertEquals(expectedBBO,bbo);
    }

    @Test
    public void testNoMatchingBBO() throws OrderProcessingException, InvalidOrderException{
        Order.OrderBuilder orderBuilder = new Order.OrderBuilder();
        Order buy1 = orderBuilder.seqNum("1").id("b1").side(Side.BUY).price(10).qty(100).timestamp("1000").build();
        engine.processRequest(Stream.of(new NewOrderEvent(buy1)));
        BBO bbo = engine.getLatestBBO();
        assertNull(bbo);
    }

}
