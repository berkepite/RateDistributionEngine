package com.berkepite.MainApplication32Bit.status;

import com.berkepite.MainApplication32Bit.subscribers.ISubscriber;

import java.util.Optional;

public class RateStatus {
    private final String data;
    private final Exception exception;

    private final String method;
    private final String notes;
    private final ISubscriber subscriber;

    private final String endpoint;

    private RateStatus(Builder builder) {
        this.data = builder.data;
        this.exception = builder.exception;
        this.method = builder.method;
        this.notes = builder.notes;
        this.subscriber = builder.subscriber;
        this.endpoint = builder.endpoint;
    }

    public static class Builder {
        private String data;
        private Exception exception;

        private String method;
        private String notes;
        private ISubscriber subscriber;

        private String endpoint;

        public Builder withData(String data) {
            this.data = data;
            return this;
        }

        public Builder withMethod(String method) {
            this.method = method;
            return this;
        }

        public Builder withSubscriber(ISubscriber subscriber) {
            this.subscriber = subscriber;
            return this;
        }

        public Builder withNotes(String notes) {
            this.notes = notes;
            return this;
        }

        public Builder withEndpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Builder withException(Exception exception) {
            this.exception = exception;
            return this;
        }

        public RateStatus build() {
            return new RateStatus(this);
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getData() {
        return data;
    }

    public Optional<Exception> getException() {
        return Optional.ofNullable(exception);
    }

    @Override
    public String toString() {
        return "RateStatus { " +
                "data='" + data + '\'' +
                ", method=" + method +
                ", subscriber=" + subscriber.getConfig().getName() +
                ", endpoint=" + endpoint +
                ", notes=" + (notes != null ? notes : "null") +
                ", exception=" + (exception != null ? exception.getMessage() : "null") +
                " }";
    }
}
