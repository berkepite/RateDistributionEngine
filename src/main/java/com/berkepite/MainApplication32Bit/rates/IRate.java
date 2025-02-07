package com.berkepite.MainApplication32Bit.rates;

import java.time.Instant;

public interface IRate {
    RateEnum getType();

    void setType(RateEnum type);

    Double getBid();

    Double getAsk();

    Instant getTimeStamp();

    void setBid(Double bid);

    void setAsk(Double ask);

    void setTimeStamp(Instant timeStamp);

    String toString();
}
