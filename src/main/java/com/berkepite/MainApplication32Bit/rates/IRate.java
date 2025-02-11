package com.berkepite.MainApplication32Bit.rates;

import java.time.Instant;

public interface IRate {
    RateEnum getType();

    Double getBid();

    Double getAsk();

    Instant getTimeStamp();

    void setType(RateEnum type);

    void setBid(Double bid);

    void setAsk(Double ask);

    void setTimeStamp(Instant timeStamp);
}
