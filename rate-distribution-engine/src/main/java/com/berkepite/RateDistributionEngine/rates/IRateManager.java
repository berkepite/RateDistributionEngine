package com.berkepite.RateDistributionEngine.rates;

import com.berkepite.RateDistributionEngine.common.rates.RawRate;

public interface IRateManager {

    void manageIncomingRawRate(RawRate rawRate);
}
