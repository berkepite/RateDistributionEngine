package com.berkepite.MainApplication32Bit.calculators;

import com.berkepite.MainApplication32Bit.rates.CalculatedRate;
import com.berkepite.MainApplication32Bit.rates.RateFactory;
import com.berkepite.MainApplication32Bit.rates.RawRate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class PythonCalculator implements IRateCalculator {
    private final String sourcePath;
    private final RateFactory rateFactory;
    private static final Logger LOGGER = LogManager.getLogger(PythonCalculator.class);
    private Source source;

    public PythonCalculator(String sourcePath, RateFactory rateFactory) {
        this.sourcePath = sourcePath;
        this.rateFactory = rateFactory;
        init();
    }

    public void init() {
        try {
            ClassPathResource resource = new ClassPathResource(sourcePath);
            Reader stream = new InputStreamReader(resource.getInputStream());

            source = Source.newBuilder("python", stream, "pymod.py").build();
        } catch (IOException e) {
            LOGGER.error(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public RawRate calculateMeansOfRawRates(RawRate incomingRate, Double[] bids, Double[] asks) {
        if (bids.length == 0) {
            return incomingRate;
        }

        try (Context context = Context.newBuilder("python")
                .allowAllAccess(true)
                .build()) {
            Value module = context.eval(source);

            Value calculateMean = module.getMember("calculate_means_of_raw_rates");

            Value result = calculateMean.execute(bids, asks);

            return rateFactory.createRawRate(incomingRate.getType(),
                    incomingRate.getProvider(),
                    result.getArrayElement(0).asDouble(),
                    result.getArrayElement(1).asDouble(),
                    incomingRate.getTimestamp());
        }
    }

    @Override
    public CalculatedRate calculateForType(String type, Double usdmid, Double[] bids, Double[] asks) {
        if (bids.length == 0 || asks.length == 0) {
            return null;
        }
        try (Context context = Context.newBuilder("python")
                .allowAllAccess(true)
                .build()) {
            Value module = context.eval(source);

            Value calculateForType = module.getMember("calculate_for_type");
            Value result = calculateForType.execute(usdmid, bids, asks);

            return rateFactory.createCalcRate(type,
                    result.getArrayElement(0).asDouble(),
                    result.getArrayElement(1).asDouble(),
                    Instant.now());
        }
    }

    @Override
    public CalculatedRate calculateForUSD_TRY(Double[] bids, Double[] asks) {
        if (bids.length == 0 || asks.length == 0) {
            return null;
        }
        try (Context context = Context.newBuilder("python")
                .allowAllAccess(true)
                .build()) {
            Value module = context.eval(source);

            Value calculateForUSD_TRY = module.getMember("calculate_for_usd_try");
            Value result = calculateForUSD_TRY.execute(bids, asks);

            return rateFactory.createCalcRate("USD_TRY",
                    result.getArrayElement(0).asDouble(),
                    result.getArrayElement(1).asDouble(),
                    Instant.now());
        }
    }

    @Override
    public boolean hasAtLeastOnePercentDiff(Double bid1, Double ask1, Double bid2, Double ask2) {
        try (Context context = Context.newBuilder("python")
                .allowAllAccess(true)
                .build()) {
            Value module = context.eval(source);
            Value hasAtLeastOnePercentDiff = module.getMember("has_at_least_one_percent_diff");

            Value result = hasAtLeastOnePercentDiff.execute(bid1, ask1, bid2, ask2);

            return result.asBoolean();
        }
    }

    @Override
    public Double calculateUSDMID(Double[] bids, Double[] asks) {
        if (bids.length == 0 || asks.length == 0) {
            return null;
        }
        try (Context context = Context.newBuilder("python")
                .allowAllAccess(true)
                .build()) {
            Value module = context.eval(source);
            Value calculateUSDMID = module.getMember("calculate_usdmid");

            Value result = calculateUSDMID.execute(bids, asks);

            return result.asDouble();
        }
    }

    @Override
    public Double calculateMean(List<RawRate> rawRates) {
        try (Context context = Context.newBuilder("python")
                .allowAllAccess(true)
                .build()) {
            Value module = context.eval(source);
            Value calculateMeans = module.getMember("calculate_means");

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
