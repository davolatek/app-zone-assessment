package com.appzone.ISO8583FluentService.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class PushJournalImpl implements PushJournalService{
    @Override
    public HttpResponse<String> sendPushJournalRequest() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://52.234.156.59:31000/pushjournal/api/push-journal"))
                .header("accept", "application/json")
                .header("x-api-key", "zsLive_8748261147813940309")
                .header("content-type", "application/json")
                .method("POST", HttpRequest.BodyPublishers.ofString("{\"Rrn\":\"000000489938\",\"Stan\":\"129725\",\"AcquirerBank\":\"100025\",\"Amount\":1000,\"AccountNumber\":\"12345643234\",\"Pan\":\"555940**8222\",\"TransactionStatus\":\"APPROVED\",\"CurrencyCode\":\"566\",\"Comment\":\"THE TRANSACTION WAS SUCCESSFULLY COMPLETED\",\"TransactionDate\":\"25/08/2024\",\"TransactionTime\":\"23:04\",\"Error\":\"\",\"TerminalId\":\"20351254\"}"))
                .build();


        HttpResponse<String> response
                = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        return response;
    }
}
