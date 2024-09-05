package com.appzone.ISO8583FluentService.service;

import com.appzone.ISO8583FluentService.model.FinancialRequest;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;

public interface IsoService {

    ISOMsg processEchoMessageRequest() throws ISOException;
    String createTransactionTimeStamp();

    ISOMsg composeKeyExchangeRequest() throws ISOException;

    ISOMsg processFinancialRequest(FinancialRequest financialRequest) throws ISOException;

    String processKeyExchangeResponse(ISOMsg keyExchangeResponse) throws Exception;
    String formulateDE4(double amount);
}
