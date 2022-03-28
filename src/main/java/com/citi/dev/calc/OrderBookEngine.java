package com.citi.dev.calc;

import com.citi.dev.event.*;
import com.citi.dev.excp.InvalidOrderException;
import com.citi.dev.excp.OrderProcessingException;
import com.citi.dev.model.BBO;
import com.citi.dev.model.Order;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;

import static java.util.AbstractMap.*;

//main calculator class for order request processing
public class OrderBookEngine {

    //bid(buy) & offer(ask) priority queues, sorted by price accordingly
    private final Queue<Order> bidsQueue = new PriorityQueue<>(Comparator.comparing(Order::getPrice).reversed().thenComparing(Order::getSeqNumAsInt));
    private final Queue<Order> offerQueue = new PriorityQueue<>(Comparator.comparing(Order::getPrice).thenComparing(Order::getSeqNumAsInt));

    //orderIdMap to fetch existing orders in O(1) complexity
    private final Map<AbstractMap.Entry<String, Order.Side>,Order> orderIdMap = new HashMap<>();

    //maintains list of BBO (best bid offer)
    private final List<BBO> bbo  = new ArrayList<>();

    //heads for bestBid and bestOffer from the queue
    private Order bestBid, bestOffer;


    public OrderBookEngine(){
    }


    //processes the micro batch of incoming requests.
    public void processRequest(Stream<InputEvent> requestStream) throws InvalidOrderException,OrderProcessingException {
        Iterator<InputEvent> iterator = requestStream.iterator();
        while (iterator.hasNext()) {
            InputEvent request = iterator.next();
            checkTickAndLotSize(request);
            processThisAtomicRequest(request);
        }
    }

    private void checkTickAndLotSize(InputEvent request) throws InvalidOrderException{
        Order order = request.getOrder();
        Objects.requireNonNull(order);
        if(order.getQty()%Order.THRESHOLD_LOT_SIZE!=0){
            String log = String.format("Lot Size of order %s should be more than threshold %f",order,Order.THRESHOLD_TICK_SIZE);
            throw new InvalidOrderException(log);
        }
        BigDecimal remainder = BigDecimal.valueOf(order.getPrice()).remainder(BigDecimal.valueOf(Order.THRESHOLD_TICK_SIZE));
        if(remainder.compareTo(BigDecimal.ZERO)>0.00000001){
            String log = String.format("Tick Size of order %s should be more than threshold %f",order,Order.THRESHOLD_TICK_SIZE);
            throw new InvalidOrderException(log);
        }
    }

    //processes single order  request
    private void processThisAtomicRequest(InputEvent request) throws OrderProcessingException {
        InputEvent.RequestType requestType = request.getRequestType();

        switch (requestType) {
            case NEW: {
                processNewOrder((NewOrderEvent) request);
                break;
            }
            case UPDATE: {
                processUpdateOrder((UpdateOrderEvent) request);
                break;
            }
            case CANCEL: {
                processCancelOrder((CancelOrderEvent)request);
                break;
            }
            default:
                throw new OrderProcessingException("Invalid requestType received " + requestType);
        }
    }

    //processes new order request
    private void processNewOrder(NewOrderEvent newOrderEvent) {
        Order newOrder = newOrderEvent.getOrder();
        Queue<Order> orderQ = newOrder.isBuy() ? bidsQueue : offerQueue;
        SimpleEntry<String, Order.Side> key = new SimpleEntry<>(newOrder.getOrderId(), newOrder.getSide());
        if(!orderIdMap.containsKey(key)) {
            orderQ.add(newOrder);
            orderIdMap.put(key,newOrder);
            updateBBO(newOrder);
        }
    }

    private void processUpdateOrder(UpdateOrderEvent updateOrderEvent)  {
        Order updateOrder = updateOrderEvent.getOrder();
        Queue<Order> orderQ = updateOrder.isBuy() ? bidsQueue : offerQueue;
        SimpleEntry<String, Order.Side> key = new SimpleEntry<>(updateOrder.getOrderId(), updateOrder.getSide());
        Order existingOrder = orderIdMap.get(key);
        if(existingOrder!=null) {
            //existing order should exist before update
            boolean isRemoved = orderQ.remove(updateOrder);
            if (isRemoved) {
                updateOrder.setQty(updateOrderEvent.newQty);
                updateOrder.setPrice(updateOrderEvent.newPrice);
                orderQ.add(updateOrder);
                updateBBO(updateOrder);
            }
        }
    }


    private void processCancelOrder(CancelOrderEvent cancelOrderRequest)  {
        Order cancelOrder = cancelOrderRequest.getOrder();
        Queue<Order> orderQ = cancelOrder.isBuy() ? bidsQueue : offerQueue;
        SimpleEntry<String, Order.Side> key = new SimpleEntry<>(cancelOrder.getOrderId(), cancelOrder.getSide());
        Order existingOrder = orderIdMap.get(key);
        if(existingOrder!=null) {
            //existing order should exist before delete
            boolean isRemoved = orderQ.remove(cancelOrder);
            if (isRemoved) {
                cancelOrder.setPrice(existingOrder.getPrice());
                cancelOrder.setQty(existingOrder.getQty());
                orderIdMap.remove(key);
                updateBBO(cancelOrder);
            }
        }
    }


    //updates BBO is eligible update to ask or offer is available
    private void updateBBO(Order orderInProcess)  {
        if (bidsQueue.isEmpty() || offerQueue.isEmpty()) {
            return;
        }

        Order bidHead = bidsQueue.peek();
        Order offerHead = offerQueue.peek();
        if (shouldUpdateBBO(bidHead, offerHead) || doesQtyChangedAtHead(orderInProcess) ) {
            //gets nearest orders ordered by sequence number
            this.bestBid = getValidOrder(bidsQueue,orderInProcess) ;
            this.bestOffer = getValidOrder(offerQueue,orderInProcess) ;
            if(bestBid != null  && bestOffer != null){
                long cumulativeBidQty = this.getCumulativeQty(bestBid,bidsQueue);
                long cumulativeOfferQty = this.getCumulativeQty(bestOffer,offerQueue);
                BBO latestBBO = new BBO(
                        orderInProcess.getSeqNum(),
                        bestBid.getPrice(),
                        cumulativeBidQty,
                        bestOffer.getPrice(),
                        cumulativeOfferQty,
                        orderInProcess.getTimestamp());

                bbo.add(latestBBO);
            }
        }
    }

    //compares if new order has changed qty for existing BBO to trigger a new BBO update
    private boolean doesQtyChangedAtHead(Order orderInProcess) {
        BBO latestBBO = getLatestBBO();
        if(orderInProcess.isBuy() && Double.compare(orderInProcess.getPrice(),latestBBO.getBidPrice())==0 && orderInProcess.getQty()!=latestBBO.getBidQty()){
            return true;
        }
        return !orderInProcess.isBuy() && Double.compare(orderInProcess.getPrice(),latestBBO.getAskPrice())==0 && orderInProcess.getQty()!=latestBBO.getAskQty();
    }

    private Order getValidOrder(Queue<Order> ordersQueue,Order orderInProcess){
        LinkedList<Order> polledOrders = new LinkedList<>();
        while(!ordersQueue.isEmpty()
                && ordersQueue.peek()!=null
                && ordersQueue.peek().compareSeqNum(orderInProcess)>0
            ){
            polledOrders.add(ordersQueue.poll());
        }
        Order validInSequenceOrder  = !ordersQueue.isEmpty() ? ordersQueue.peek() : polledOrders.getFirst();
        ordersQueue.addAll(polledOrders);
        return validInSequenceOrder;
    }

    //checks if head of queue is not same as best bid/ask thus triggering a new BBO update
    private boolean shouldUpdateBBO(Order bidHead, Order offerHead) {
        return !bidHead.equals(bestBid)
                || bidHead.getQty() != bestBid.getQty()
                || Double.compare(bidHead.getPrice(), bestBid.getPrice()) != 0
                || !offerHead.equals(bestOffer)
                || offerHead.getQty() != bestOffer.getQty()
                || Double.compare(offerHead.getPrice(), bestOffer.getPrice()) != 0;
    }

    //gets the cumulative quantity from all nodes with same best price
    private long getCumulativeQty(Order bestOrder, Queue<Order> orderQueue) {
        List<Order> polledOrders = new LinkedList<>();
        while(!orderQueue.isEmpty() && orderQueue.peek()!=null && orderQueue.peek().getPrice()==bestOrder.getPrice()){
            polledOrders.add(orderQueue.poll());
        }
        orderQueue.addAll(polledOrders);
        return polledOrders.isEmpty() && !orderQueue.isEmpty() ? orderQueue.peek().getQty() : polledOrders.stream().mapToLong(Order::getQty).sum();
    }

    public List<BBO> getBBOList() {
        return bbo;
    }

    public BBO getLatestBBO() {
        return bbo.isEmpty() ? null : bbo.get(bbo.size()-1);
    }
}
