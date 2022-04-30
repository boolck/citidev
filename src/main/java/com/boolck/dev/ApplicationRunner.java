package com.boolck.dev;

import com.boolck.dev.calc.OrderBookEngine;
import com.boolck.dev.excp.InputReadException;
import com.boolck.dev.excp.InvalidOrderException;
import com.boolck.dev.excp.OrderProcessingException;
import com.boolck.dev.listener.BufferedCSVListener;
import com.boolck.dev.listener.SourceListener;
import com.boolck.dev.model.BBO;
import com.boolck.dev.util.OrderBookRequestFileUtil;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/*
main entry class to run the program, it takes input from input request file csv
in order to process each request in streaming, it uses BufferedCSVListener with batch size as unity
 */
public class ApplicationRunner {

    private static final Logger LOGGER = Logger.getLogger("ApplicationRunner.class");

    public static void main(String... args) throws InputReadException, OrderProcessingException, InvalidOrderException {
        if (args == null || args.length < 1) {
            LOGGER.warning("No Input  request files provided");
            return;
        }

        String inputOrderBookFilePath = Objects.requireNonNull(args)[0];
        Path inputFilePath = Paths.get(inputOrderBookFilePath);
        if (!inputFilePath.toFile().canRead() ) {
            LOGGER.severe("file paths for " + inputOrderBookFilePath + " is not correct, exiting ");
            return;
        }

        OrderBookRequestFileUtil.OrderBookAnalytics orderBookAnalytics =
                OrderBookRequestFileUtil.parseRequestFile(inputFilePath.toFile().getAbsolutePath());
        OrderBookEngine engine = new OrderBookEngine();

        SourceListener streamingListener = new BufferedCSVListener(
                inputFilePath.toFile().getAbsolutePath());

        streamingListener.process(engine);

        List<BBO> bboList = engine.getBBOList();
        bboList.forEach(x -> LOGGER.info(x.toString()));

    }
}
