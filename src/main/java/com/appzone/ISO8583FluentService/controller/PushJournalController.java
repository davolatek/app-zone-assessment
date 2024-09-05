package com.appzone.ISO8583FluentService.controller;

import com.appzone.ISO8583FluentService.service.PushJournalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.http.HttpResponse;

@RestController
@RequestMapping("/push-journal")
@Slf4j
public class PushJournalController {

    @Autowired
    private PushJournalService pushJournalService;

    @PostMapping("/simulate-journal")
    public ResponseEntity<?> simulateJournal(){

        try{
            HttpResponse<String> response
                    = pushJournalService.sendPushJournalRequest();

            log.info("Response Received::: "+response.body());
            return ResponseEntity
                    .ok(response);
        }catch(Exception e){
            log.error("An error occurred "+e.getMessage());
            return ResponseEntity
                    .internalServerError()
                    .body("An error occurred");
        }
    }

}
