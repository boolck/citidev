package com.citi.dev.listener;

import com.citi.dev.calc.OrderBookEngine;
import com.citi.dev.event.InputEvent;
import com.citi.dev.excp.InputReadException;
import com.citi.dev.excp.InvalidOrderException;
import com.citi.dev.excp.OrderProcessingException;
import com.citi.dev.model.OrderBook;
import com.citi.dev.util.OrderBookRequestFileUtil;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/*
    1. utility to support processing orders from  input csv file
    2. converts to unified & abstract event request model
    3. sends to OrderBookEngine for processing for each batch
 */
public class BufferedCSVListener implements SourceListener{
    //name of csv file having incoming data
    private final String file;
    private final int batchSize;
    private final int limit;

    public BufferedCSVListener(String inputRequestFile) {
        this(inputRequestFile,1,Integer.MAX_VALUE);
    }

    public BufferedCSVListener(String inputRequestFile,int batchSize, int limit) {
        this.file = inputRequestFile;
        this.batchSize = batchSize;
        this.limit = limit;
    }

    //parses the csv and calls orderbook engine to process the requests
    @Override
    public void process(OrderBookEngine orderBookEngine) throws InputReadException, OrderProcessingException, InvalidOrderException {
        processAsBufferReader(orderBookEngine,batchSize,limit);
    }

    private void processAsBufferReader(OrderBookEngine orderBookEngine,int batchSize,int limit) throws OrderProcessingException, InputReadException, InvalidOrderException {
        try {
            try (Scanner scanner = new Scanner(Paths.get(file))) {
                List<String> linesInBatch = new ArrayList<>(batchSize);
                List<String> totalLinesRead = new LinkedList<>();
                boolean skipHeader=false;
                while (scanner.hasNextLine()) {
                    //ignore header
                    if(!skipHeader){
                        scanner.nextLine();
                        skipHeader=true;
                    }

                    //keep populating the batch until batchSize
                     if (linesInBatch.size() < batchSize) {
                        String thisLine = scanner.nextLine();
                        linesInBatch.add(thisLine);
                        totalLinesRead.add(thisLine);
                    }
                    //else process this batch
                    else {
                        processBatch(linesInBatch, orderBookEngine);
                        linesInBatch.clear();
                    }
                    //if lines read exceeds limit, flush the batch
                    if(totalLinesRead.size()>=limit){
                        processBatch(linesInBatch, orderBookEngine);
                        linesInBatch.clear();
                        return;
                    }
                }
                //any residual stream left to be processed
                if(!linesInBatch.isEmpty()){
                    processBatch(linesInBatch, orderBookEngine);
                    linesInBatch.clear();
                }
            }
        } catch (IOException e) {
            throw new InputReadException(e.getMessage(),e.getCause());
        }
    }

    /*
    this method converts each  csv line to correct  request (new/update/cancel)
    then passes to order processing engine for BBO processing
     */

    private void processBatch(List<String> lines, OrderBookEngine orderBookEngine) throws InvalidOrderException, OrderProcessingException {
        if(lines.isEmpty()){
            return;
        }

        List<InputEvent> lastButOneRequest =
                IntStream.range(0, lines.size() - 1)
                        .mapToObj(i -> OrderBookRequestFileUtil.parseOrderBookRow(lines.get(i))).
                        map(OrderBook::getInputRequest)
                        .sorted(Comparator.comparing(r -> r.getOrder().getSeqNumAsInt()))
                        .collect(Collectors.toCollection(ArrayList::new));

        orderBookEngine.processRequest(lastButOneRequest.stream());

    }

}
