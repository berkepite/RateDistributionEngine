package com.berkepite.RateDistributionEngine.common.status;

import com.berkepite.RateDistributionEngine.common.subscribers.ISubscriber;

import java.net.Socket;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public class ConnectionStatus {
    private final Exception exception;
    private final HttpResponse<?> httpResponse;
    private final HttpRequest httpRequest;
    private final Socket socket;
    private final String url;

    private final String method;
    private final String notes;
    private final ISubscriber subscriber;

    private ConnectionStatus(Builder builder) {
        this.exception = builder.exception;
        this.httpResponse = builder.httpResponse;
        this.httpRequest = builder.httpRequest;
        this.socket = builder.socket;
        this.url = builder.url;

        this.subscriber = builder.subscriber;
        this.notes = builder.notes;
        this.method = builder.method;
    }

    public static class Builder {
        private Exception exception;
        private HttpResponse<?> httpResponse;
        private HttpRequest httpRequest;
        private Socket socket;
        private String url;

        private String method;
        private ISubscriber subscriber;
        private String notes;

        public Builder withException(Exception exception) {
            this.exception = exception;
            return this;
        }

        public Builder withHttpResponse(HttpResponse<?> response, HttpRequest request) {
            this.httpResponse = response;
            this.httpRequest = request;
            this.url = request.uri().toString();
            return this;
        }

        public Builder withHttpRequestError(Exception e, HttpRequest request) {
            this.exception = e;
            this.httpRequest = request;
            this.url = request.uri().toString();
            return this;
        }

        public Builder withSocket(Socket socket, String url) {
            this.socket = socket;
            this.url = url;
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

        public ConnectionStatus build() {
            return new ConnectionStatus(this);
        }
    }

    @Override
    public String toString() {
        return "ConnectionStatus {" +
                "url='" + url + '\'' +
                ", httpRequest=" + httpRequest +
                ", httpResponse=" + httpResponse +
                ", socket=" + (socket != null ? socket.getInetAddress().getHostAddress() + ":" + socket.getPort() : "null") +
                ", method=" + method +
                ", subscriber=" + subscriber.getConfig().getName() +
                ", notes=" + (notes != null ? notes : "null") +
                ", exception=" + (exception != null ? exception.getMessage() : "null") +
                '}';
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    // Getters
    public Optional<HttpResponse<?>> getHttpResponse() {
        return Optional.ofNullable(httpResponse);
    }

    public Optional<HttpRequest> getHttpRequest() {
        return Optional.ofNullable(httpRequest);
    }

    public String getUrl() {
        return url;
    }

    public ISubscriber getSubscriber() {
        return subscriber;
    }

    public String getNotes() {
        return notes;
    }

    public String getMethod() {
        return method;
    }

    public Socket getSocket() {
        return socket;
    }

    public Optional<Exception> getException() {
        return Optional.ofNullable(exception);
    }
}
