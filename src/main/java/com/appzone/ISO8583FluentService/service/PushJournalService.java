package com.appzone.ISO8583FluentService.service;

import java.io.IOException;
import java.net.http.HttpResponse;

public interface PushJournalService {

    HttpResponse<String> sendPushJournalRequest() throws IOException, InterruptedException;
}
