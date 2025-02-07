package com.berkepite.MainApplication32Bit.status;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/*
 * WIP
 */
public class RateStatus extends ConnectionStatus {

    public RateStatus(HttpResponse<?> response, HttpRequest req) {
        super(response, req);
    }

    public RateStatus(Exception e, HttpRequest req) {
        super(e, req);
    }

    @Override
    public String toString() {
        return "RateStatus [status=" + status + "," +
                " HttpRequest=" + httpRequest.toString() + "," +
                " HttpResponse=" + httpResponse.toString() + "," +
                " URL=" + url + "," +
                " Exception=" + exception.toString() + "," + "]";
    }

}
