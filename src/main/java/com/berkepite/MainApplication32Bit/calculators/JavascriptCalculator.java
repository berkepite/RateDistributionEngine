package com.berkepite.MainApplication32Bit.calculators;

import com.berkepite.MainApplication32Bit.rates.CalculatedRate;
import com.berkepite.MainApplication32Bit.rates.RawRate;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class JavascriptCalculator implements IRateCalculator {

    private final String sourcePath;
    private Source source;
    private static final Logger LOGGER = LogManager.getLogger(JavascriptCalculator.class);

    public JavascriptCalculator(String sourcePath) {
        this.sourcePath = sourcePath;
        init();
    }

    public void init() {
        ClassPathResource resource = new ClassPathResource(sourcePath);
        try {
            source = Source.newBuilder("js", resource.getFile()).mimeType("application/javascript+module").build();
        } catch (IOException e) {
            LOGGER.error(e);
            throw new RuntimeException(e);
        }
    }

    public String getSourcePath() {
        return sourcePath;
    }

    @Override
    public RawRate calculateMeansOfRawRates(RawRate incomingRate, Double[] bids, Double[] asks) {
        if (bids.length == 0) {
            return incomingRate;
        }
        RawRate mean = new RawRate();

        mean.setTimestamp(incomingRate.getTimestamp());
        mean.setType(incomingRate.getType());
        mean.setProvider(incomingRate.getProvider());

        try (Context context = Context.newBuilder("js")
                .allowAllAccess(true)
                .option("js.esm-eval-returns-exports", "true")
                .build()) {
            Value module = context.eval(source);

            Value calculateMean = module.getMember("calculateMeansOfRawRates");

            Value result = calculateMean.execute(bids, asks);

            mean.setBid(result.getArrayElement(0).asDouble());
            mean.setAsk(result.getArrayElement(1).asDouble());

            return mean;
        }
    }

    @Override
    public CalculatedRate calculateForType(String type, Double usdmid, Double[] bids, Double[] asks) {
        if (bids.length == 0 || asks.length == 0) {
            return null;
        }
        try (Context context = Context.newBuilder("js")
                .allowAllAccess(true)
                .option("js.esm-eval-returns-exports", "true")
                .build()) {
            Value module = context.eval(source);
            Value calculateForType = module.getMember("calculateForType");

            CalculatedRate calcRate = new CalculatedRate();

            Value result = calculateForType.execute(usdmid, bids, asks);

            calcRate.setType(type);
            calcRate.setTimestamp(Instant.now());
            calcRate.setBid(result.getArrayElement(0).asDouble());
            calcRate.setAsk(result.getArrayElement(1).asDouble());

            return calcRate;
        }
    }

    @Override
    public CalculatedRate calculateForUSD_TRY(Double[] bids, Double[] asks) {
        if (bids.length == 0 || asks.length == 0) {
            return null;
        }
        try (Context context = Context.newBuilder("js")
                .allowAllAccess(true)
                .option("js.esm-eval-returns-exports", "true")
                .build()) {
            Value module = context.eval(source);
            Value calculateForUSD_TRY = module.getMember("calculateForUSD_TRY");

            CalculatedRate calcRate = new CalculatedRate();

            Value result = calculateForUSD_TRY.execute(bids, asks);

            calcRate.setType("USD_TRY");
            calcRate.setTimestamp(Instant.now());
            calcRate.setBid(result.getArrayElement(0).asDouble());
            calcRate.setAsk(result.getArrayElement(1).asDouble());

            return calcRate;
        }
    }

    @Override
    public boolean hasAtLeastOnePercentDiff(Double bid1, Double ask1, Double bid2, Double ask2) {
        try (Context context = Context.newBuilder("js")
                .allowAllAccess(true)
                .option("js.esm-eval-returns-exports", "true")
                .build()) {
            Value module = context.eval(source);
            Value hasAtLeastOnePercentDiff = module.getMember("hasAtLeastOnePercentDiff");

            Value result = hasAtLeastOnePercentDiff.execute(bid1, ask1, bid2, ask2);

            return result.asBoolean();
        }
    }

    @Override
    public Double calculateUSDMID(Double[] bids, Double[] asks) {
        if (bids.length == 0 || asks.length == 0) {
            return null;
        }
        try (Context context = Context.newBuilder("js")
                .allowAllAccess(true)
                .option("js.esm-eval-returns-exports", "true")
                .build()) {
            Value module = context.eval(source);
            Value calculateUSDMID = module.getMember("calculateUSDMID");

            Value result = calculateUSDMID.execute(bids, asks);

            return result.asDouble();
        }
    }

    @Override
    public Double calculateMean(List<RawRate> rawRates) {
        try (Context context = Context.newBuilder("js")
                .allowAllAccess(true)
                .option("js.esm-eval-returns-exports", "true")
                .build()) {
            Value module = context.eval(source);
            Value calculateMeans = module.getMember("calculateMeans");

            List<Double> bids = new ArrayList<>();
            List<Double> asks = new ArrayList<>();
            rawRates.forEach(rate -> {
                bids.add(rate.getBid());
                asks.add(rate.getAsk());
            });

            Value result = calculateMeans.execute(bids.toArray(new Double[0]), asks.toArray(new Double[0]));

            return result.asDouble();
        }
    }
}
