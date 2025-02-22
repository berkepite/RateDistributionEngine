package com.berkepite.MainApplication32Bit.calculators;

import com.berkepite.MainApplication32Bit.rates.RawRate;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JavascriptCalculator implements IRateCalculator {

    private final String sourcePath;
    private Source source;
    private static final Logger LOGGER = LogManager.getLogger(JavascriptCalculator.class);

    public JavascriptCalculator(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    @PostConstruct
    public void init() throws IOException {
        ClassPathResource resource = new ClassPathResource(sourcePath);
        source = Source.newBuilder("js", resource.getFile()).mimeType("application/javascript+module").build();
    }

    public String getSourcePath() {
        return sourcePath;
    }

    @Override
    public RawRate calculateMean(RawRate incomingRate, List<RawRate> otherPlatformRates) {
        RawRate mean = new RawRate();

        mean.setTimestamp(incomingRate.getTimestamp());
        mean.setType(incomingRate.getType());
        mean.setProvider(incomingRate.getProvider());

        List<Double> bids = new ArrayList<>();
        bids.add(incomingRate.getBid());
        otherPlatformRates.forEach(platformRate -> bids.add(platformRate.getBid()));

        List<Double> asks = new ArrayList<>();
        asks.add(incomingRate.getAsk());
        otherPlatformRates.forEach(platformRate -> asks.add(platformRate.getAsk()));

        try (Context context = Context.newBuilder("js")
                .allowAllAccess(true)
                .option("js.esm-eval-returns-exports", "true")
                .build()) {
            Value module = context.eval(source);

            Value calculateMean = module.getMember("calculateMean");

            Value result = calculateMean.execute(bids.toArray(new Double[0]), asks.toArray(new Double[0]));

            mean.setBid(result.getArrayElement(0).asDouble());
            mean.setAsk(result.getArrayElement(1).asDouble());

            return mean;
        }
    }

    @Override
    public boolean hasAtLeastOnePercentDiff(RawRate rate1, RawRate rate2) {
        try (Context context = Context.newBuilder("js")
                .allowAllAccess(true)
                .option("js.esm-eval-returns-exports", "true")
                .build()) {
            Value module = context.eval(source);
            Value hasAtLeastOnePercentDiff = module.getMember("hasAtLeastOnePercentDiff");

            Double rate1_bid = rate1.getBid();
            Double rate2_bid = rate2.getBid();
            Double rate1_ask = rate1.getAsk();
            Double rate2_ask = rate2.getAsk();

            Value result = hasAtLeastOnePercentDiff.execute(rate1_bid, rate1_ask, rate2_bid, rate2_ask);

            return result.asBoolean();
        }
    }

    @Override
    public Double calculateUSDMID(List<RawRate> ratesOfTypeUSDTRY) {
        try (Context context = Context.newBuilder("js")
                .allowAllAccess(true)
                .option("js.esm-eval-returns-exports", "true")
                .build()) {
            Value module = context.eval(source);
            Value calculateUSDMID = module.getMember("calculateUSDMID");

            List<Double> bids = new ArrayList<>();
            List<Double> asks = new ArrayList<>();
            ratesOfTypeUSDTRY.forEach(rate -> {
                bids.add(rate.getBid());
                asks.add(rate.getAsk());
            });

            Value result = calculateUSDMID.execute(bids.toArray(new Double[0]), asks.toArray(new Double[0]));

            return result.asDouble();
        }
    }
}
