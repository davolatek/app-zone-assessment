package com.appzone.ISO8583FluentService.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinancialRequest {
    private String pan;                      // Field 2: Primary Account Number
    private String processingCode;           // Field 3: Processing Code
    private double amountTransaction;        // Field 4: Amount, Transaction
    private String transmissionDateTime;     // Field 7: Transmission Date and Time
    private String systemTraceAuditNumber;   // Field 11: System Trace Audit Number
    private String localTransactionTime;     // Field 12: Local Transaction Time
    private String localTransactionDate;     // Field 13: Local Transaction Date
    private String expirationDate;           // Field 14: Expiration Date
    private String merchantType;             // Field 18: Merchant Type
    private String posEntryMode;             // Field 22: POS Entry Mode
    private String cardSequenceNumber;       // Field 23: Card Sequence Number
    private String functionCode;             // Field 24: Function Code
    private String acquiringInstitutionCode; // Field 32: Acquiring Institution ID Code
    private String track2Data;               // Field 35: Track 2 Data
    private String retrievalReferenceNumber; // Field 37: Retrieval Reference Number
    private String authorizationIdResponse;  // Field 38: Authorization Identification Response
    private String responseCode;             // Field 39: Response Code
    private String terminalId;               // Field 41: Card Acceptor Terminal Identification
    private String cardAcceptorIdCode;       // Field 42: Card Acceptor Identification Code
    private String cardAcceptorNameLocation; // Field 43: Card Acceptor Name/Location
    private String currencyCode;             // Field 49: Currency Code, Transaction
    private String pinData;                  // Field 52: Personal Identification Number Data
    private String additionalAmounts;        // Field 54: Additional Amounts
    private String iccData;                  // Field 55: ICC Data
    private String originalDataElements;     // Field 90: Original Data Elements
    private String transactionDescription;   // Field 102: Transaction Description
    private String accountIdentification1;   // Field 103: Account Identification 1
    private String reservedPrivate;          // Custom fields (e.g., Field 120, 121)
}
