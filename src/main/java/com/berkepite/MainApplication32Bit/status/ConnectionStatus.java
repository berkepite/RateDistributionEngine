package com.berkepite.MainApplication32Bit.status;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ConnectionStatus {

    protected ConnectionStatusEnum status;
    protected Exception exception;
    protected HttpResponse<?> httpResponse;
    protected final HttpRequest httpRequest;
    protected final String url;

    public ConnectionStatus(Exception e, HttpRequest req) {
        httpRequest = req;
        status = ConnectionStatusEnum.valueOf(e.getClass().getSimpleName());
        this.url = req.uri().toString();
        this.exception = e;
    }

    public ConnectionStatus(HttpResponse<?> response, HttpRequest req) {
        httpRequest = req;
        httpResponse = response;
        this.url = req.uri().toString();
        switch (response.statusCode()) {
            case 200:
                status = ConnectionStatusEnum.OK;
                break;
            case 404:
                status = ConnectionStatusEnum.Resource_Not_Found;
                break;
            case 401:
                status = ConnectionStatusEnum.Unauthorized;
                break;
            case 500:
                status = ConnectionStatusEnum.Internal_Server_Error;
                break;
        }
    }

    @Override
    public String toString() {
        return "Connection [status=" + status + "," +
                " HttpRequest=" + httpRequest.toString() + "," +
                " HttpResponse=" + httpResponse.toString() + "," +
                " URL=" + url + "," +
                " Exception=" + exception.toString() + "," + "]";
    }

    public HttpResponse<?> getHttpResponse() {
        return httpResponse;
    }

    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    public String getUrl() {
        return url;
    }

    public ConnectionStatusEnum getStatus() {
        return status;
    }

    public Exception getException() {
        return exception;
    }
}
