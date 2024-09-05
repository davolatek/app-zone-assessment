package com.appzone.ISO8583FluentService.controller;

import com.appzone.ISO8583FluentService.model.FinancialRequest;
import com.appzone.ISO8583FluentService.service.IsoService;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.channel.PostChannel;
import org.jpos.iso.packager.GenericPackager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/iso8583")
public class FluentServiceController {

    @Autowired
    private IsoService isoService;
    private ISOChannel channel;
    private GenericPackager packager;

    private final String HOST = "52.234.156.59";
    private final int PORT = 12000;

    public FluentServiceController() throws ISOException, IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("field.xml");
        this.packager = new GenericPackager(is);

        this.channel = new PostChannel(HOST, PORT, this.packager);
        this.channel.connect();
    }

    @PostMapping("/echo-message")
    public String sendEchoMessage(){
        try{



            ISOMsg echoRequest = isoService.processEchoMessageRequest();

            if(echoRequest != null){
                echoRequest.setPackager(this.packager);


                byte[] isoMessageByte = echoRequest.pack();
                System.out.println("Bitmap::: "+ ISOUtil.hexString(isoMessageByte));
//                System.out.println("Hex dump::: "+ISOUtil.hexDump(echoRequ));
                for (int i = 1; i <= echoRequest.getMaxField(); i++) {
                    if (echoRequest.hasField(i)) {
                        System.out.println("Field " + i + ": " + echoRequest.getString(i));
                    }
                }


                channel.send(echoRequest);

                // receive channel response
                ISOMsg echoResponse =  channel.receive();

                System.out.println("Echo message received::: "+echoResponse);


                for (int i = 1; i <= echoResponse.getMaxField(); i++) {
                    if (echoResponse.hasField(i)) {
                        System.out.println("Field " + i + ": " + echoResponse.getString(i));
                    }
                }

                return echoResponse.toString();
            }

            return "error processing ISO request";
        }catch(Exception e){
            e.printStackTrace();;
            return "Error processing request::: "+e.getMessage();
        }
    }

    @PostMapping("/key-exchange")
    public String sendKeyExchange() throws Exception {


        ISOMsg keyExchangeRequest = isoService.composeKeyExchangeRequest();
        System.out.println("Key Exchange Request::: ");
        for(int i=1; i<=keyExchangeRequest.getMaxField();i++){
            if(keyExchangeRequest.hasField(i)){
                System.out.println("Field "+i+": "+keyExchangeRequest.getString(i));
            }
        }

        if(keyExchangeRequest !=null){
            channel.send(keyExchangeRequest);

            ISOMsg keyExchangeResponse = channel.receive();

            if(keyExchangeResponse !=null){
                //process the key exchange response
                System.out.println("Key Exchange Response Received");
                for(int i=1; i<=keyExchangeResponse.getMaxField();i++){
                    if(keyExchangeResponse.hasField(i)){
                        System.out.println("Field "+i+": "+keyExchangeResponse.getString(i));
                    }
                }
            }

            isoService.processKeyExchangeResponse(keyExchangeResponse);

            return keyExchangeResponse.toString();
        }

        return "error processing request";
    }

    @PostMapping("financial-request")
    public String sendFinancialRequest(@RequestBody FinancialRequest financialRequest) throws ISOException, IOException {
        ISOMsg composedFinancialRequest = isoService.processFinancialRequest(financialRequest);

        if(composedFinancialRequest !=null){
            System.out.println("Financial Request Generated");
            for(int i= 1; i<=composedFinancialRequest.getMaxField();i++){
                if(composedFinancialRequest.hasField(i)){

                    System.out.println("Field "+i+" "+composedFinancialRequest.getString(i));
                }
            }

            channel.send(composedFinancialRequest);
            ISOMsg receivedMessage = channel.receive();



            if(receivedMessage != null){
                System.out.println("Financial Response Received");
                for(int i= 1; i<=receivedMessage.getMaxField();i++){
                    if(receivedMessage.hasField(i)){

                        System.out.println("Field "+i+" "+receivedMessage.getString(i));
                    }
                }

                return receivedMessage.toString();
            }
        }

        return "error... failed to process";
    }
}
