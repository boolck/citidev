package com.citi.dev.calc;

import com.citi.dev.excp.InputReadException;
import com.citi.dev.excp.InvalidOrderException;
import com.citi.dev.excp.OrderProcessingException;
import com.citi.dev.listener.BufferedCSVListener;
import com.citi.dev.listener.SourceListener;
import com.citi.dev.model.BBO;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.Assert.fail;

public class TestBufferedCSVListener {


    @Test
    public void testL3RequestProcessAsStream() throws OrderProcessingException, InputReadException, IOException, InvalidOrderException {
        Path filePath = Paths.get("src","test","resources");
        String inputFile = "input_orders.csv";
        SourceListener listener = new BufferedCSVListener(
                filePath.resolve(inputFile).toFile().getAbsolutePath());
        OrderBookEngine engine = new OrderBookEngine();
        listener.process(engine);
        compareWithExpectedOutput(filePath,engine.getBBOList());
    }

    private void compareWithExpectedOutput(Path filePath, List<BBO> actualBBO) throws IOException {

        String l1ExpectedOutputFile = "expected_l1_data.csv";
        BufferedReader l1Reader = new BufferedReader(new FileReader(filePath.resolve(l1ExpectedOutputFile).toFile()));
        l1Reader.readLine();
        CsvToBean<BBO> csvReader = new CsvToBeanBuilder<BBO>(l1Reader)
                .withType(BBO.class)
                .withSeparator(',')
                .withIgnoreLeadingWhiteSpace(true)
                .withIgnoreEmptyLine(true)
                .build();
        List<BBO> expectedBBO = csvReader.parse();

        Map<BBO,BBO> mismatched = new HashMap<>();
        int min = Math.min(expectedBBO.size(),actualBBO.size());
        IntStream.range(0, min).forEach(i -> {
            if (!actualBBO.get(i).equals(expectedBBO.get(i))) {
                mismatched.put(actualBBO.get(i),expectedBBO.get(i));
            }
        });
        if(!mismatched.isEmpty()){
            fail("Found "+mismatched.size()+" mismatches in expected vs actual BBO "+mismatched);
        }
    }

}
