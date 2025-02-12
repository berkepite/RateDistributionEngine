package com.berkepite.MainApplication32Bit.status;

public class RateStatus {

    private final String data;
    private final Exception exception;

    public RateStatus(final String data, final Exception exception) {
        this.data = data;
        this.exception = exception;
    }


    @Override
    public String toString() {
        return "RateStatus [data=" + data + " | " +
                "exception=" + exception + "]";
    }

}
