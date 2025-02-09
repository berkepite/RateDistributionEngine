package com.berkepite.MainApplication32Bit.status;

import java.net.Socket;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ConnectionStatus {

    protected ConnectionStatusEnum status;
    protected Exception exception;
    protected HttpResponse<?> httpResponse;
    protected HttpRequest httpRequest;
    protected Socket socket;
    protected final String url;

    public ConnectionStatus(Exception e, HttpRequest req) {
        httpRequest = req;
        status = ConnectionStatusEnum.valueOf(e.getClass().getSimpleName());
        this.url = req.uri().toString();
        this.exception = e;
    }

    public ConnectionStatus(Exception e, Socket socket) {
        this.socket = socket;
        status = ConnectionStatusEnum.valueOf(e.getClass().getSimpleName());
        this.url = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
        this.exception = e;
    }

    public ConnectionStatus(Socket socket, String response) {
        this.socket = socket;
        this.url = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
        if (response.startsWith("AUTH: Please")) {
            status = ConnectionStatusEnum.AUTHPROMPT;
            return;
        }
        status = ConnectionStatusEnum.valueOf(response.replace(" ", ""));
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
                " HttpRequest=" + httpRequest + "," +
                " HttpResponse=" + httpResponse + "," +
                " Socket=" + socket + "," +
                " URL=" + url + "," +
                " Exception=" + exception + "," + "]";
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
