package com.citi.dev.util;

import com.citi.dev.model.OrderBook;
import com.citi.dev.excp.InputReadException;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.List;

public  class OrderBookRequestFileUtil {

    public static class OrderBookAnalytics{
        private final String fileName;
        private final List<OrderBook> orderBookList;

        public OrderBookAnalytics(String fileName,List<OrderBook> orderBookList){
            this.fileName = fileName;
            this.orderBookList = orderBookList;
        }
        public List<OrderBook> getOrderBookList(){
            return orderBookList;
        }
    }

    public static OrderBookAnalytics parseRequestFile(String inputRequestFile) throws InputReadException {
        Path filePath = Path.of(inputRequestFile);
        try {
            BufferedReader Reader = new BufferedReader(
                    new FileReader(filePath.toFile().getAbsolutePath()));
            Reader.readLine();
            CsvToBean<OrderBook> csvReader = new CsvToBeanBuilder<OrderBook>(Reader)
                    .withType(OrderBook.class)
                    .withSeparator(',')
                    .withIgnoreLeadingWhiteSpace(true)
                    .withIgnoreEmptyLine(true)
                    .build();
            List<OrderBook> allrequests = csvReader.parse();

            return new OrderBookAnalytics(inputRequestFile,allrequests);

        } catch (IOException e) {
            throw new InputReadException("Error loading file "+inputRequestFile,e);
        }
    }

    public static int compareSeqNum(String thisSeqNum, String otherSeqNum){
        BigInteger thisSeqNumAsInt = new BigDecimal(thisSeqNum).toBigIntegerExact();
        BigInteger otherSeqNumAsInt = new BigDecimal(otherSeqNum).toBigIntegerExact();
        return thisSeqNumAsInt.subtract(otherSeqNumAsInt).intValue();
    }

    public static OrderBook parseOrderBookRow(String line) {
        String[] parts = line.split(",");
        OrderBook orderBook = new OrderBook();
        orderBook.setSeqNum(parts[0]);
        if(!parts[1].isEmpty()){
            orderBook.setAddOrderId(parts[1]);
            orderBook.setAddSide(parts[2]);
            orderBook.setAddPrice(Double.parseDouble(parts[3]));
            orderBook.setAddQty(Long.parseLong(parts[4]));
        }
        else if(!parts[5].isEmpty()){
            orderBook.setUpdateOrderId(parts[5]);
            orderBook.setUpdateSide(parts[6]);
            orderBook.setUpdatePrice(Double.parseDouble(parts[7]));
            orderBook.setUpdateQty(Long.parseLong(parts[8]));
        }
        else if(!parts[9].isEmpty()){
            orderBook.setDeleteOrderId(parts[9]);
            orderBook.setDeleteSide(parts[10]);
        }
        orderBook.setTime(parts[11]);
        return orderBook;
    }

}
