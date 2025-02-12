package com.berkepite.MainApplication32Bit.status;

import java.net.Socket;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ConnectionStatus {
    protected Exception exception;
    protected HttpResponse<?> httpResponse;
    protected HttpRequest httpRequest;
    protected Socket socket;
    protected String url;

    public ConnectionStatus(Exception e, HttpRequest req) {
        httpRequest = req;
        this.url = req.uri().toString();
        this.exception = e;
    }

    public ConnectionStatus(Exception e, Socket socket, String url) {
        this.url = url;
        this.exception = e;
        if (socket != null) {
            this.socket = socket;
        }
    }

    public ConnectionStatus(Socket socket, String response) {
        this.socket = socket;
        this.url = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
    }

    public ConnectionStatus(HttpResponse<?> response, HttpRequest req) {
        httpRequest = req;
        httpResponse = response;
        this.url = req.uri().toString();
    }

    @Override
    public String toString() {
        return "Connection [HttpRequest=" + httpRequest + "," +
                " HttpResponse=" + httpResponse + "," +
                " Socket=" + socket + "," +
                " URL=" + url + "," +
                " Exception=" + exception + "]";
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

    public Exception getException() {
        return exception;
    }
}
