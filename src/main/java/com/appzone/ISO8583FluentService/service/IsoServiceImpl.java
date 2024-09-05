package com.appzone.ISO8583FluentService.service;

import com.appzone.ISO8583FluentService.model.FinancialRequest;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

@Service
public class IsoServiceImpl implements IsoService {
    @Override
    public ISOMsg processEchoMessageRequest() throws ISOException {

        String timeStamp = createTransactionTimeStamp();
        String stan = new RandomNumberGenerator().generateRandomInteger(10000, 99999);

        System.out.println("Timestamp::: " + timeStamp);
        ISOMsg isoMsg = new ISOMsg();
        isoMsg.setMTI("0800");
        isoMsg.set("7", timeStamp);
        isoMsg.set("11", stan);
        isoMsg.set("12", createTransactionTimeStamp().substring(4, 10));
        isoMsg.set("13", createTransactionTimeStamp().substring(0, 4));
        isoMsg.set("32", "033");
        isoMsg.set("37", stan + "123456");

        isoMsg.set("70", "301");


        return isoMsg;
    }

    @Override
    public String createTransactionTimeStamp() {
        return new SimpleDateFormat("MMddHHmmss").format(new Date());
    }


    @Override
    public ISOMsg composeKeyExchangeRequest() throws ISOException {
        ISOMsg keyExchangeRequest = new ISOMsg();
        String stan = new RandomNumberGenerator().generateRandomInteger(100000, 999999);
        keyExchangeRequest.setMTI("0800");
        keyExchangeRequest.set("7", createTransactionTimeStamp());
        keyExchangeRequest.set("11", new RandomNumberGenerator().generateRandomInteger(100000, 999999));
        keyExchangeRequest.set("12", createTransactionTimeStamp().substring(4, 10));
        keyExchangeRequest.set("13", createTransactionTimeStamp().substring(0, 4));
        keyExchangeRequest.set("32", "040");
        keyExchangeRequest.set("37", stan + "123456");
        keyExchangeRequest.set("70", "101");
//        keyExchangeRequest.set("100", "082");

        return keyExchangeRequest;
    }

    @Override
    public ISOMsg processFinancialRequest(FinancialRequest financialRequest) throws ISOException {
        ISOMsg isoMsg = new ISOMsg();
        isoMsg.setMTI("0200");
        isoMsg.set(2, financialRequest.getPan());
        isoMsg.set(3, "920000");
        isoMsg.set("4", formulateDE4(financialRequest.getAmountTransaction()));
        isoMsg.set(7, "0905091101");
        isoMsg.set(11, "642795");
        isoMsg.set(32, "4008");
        isoMsg.set(37, "451298");
        isoMsg.set(41, "20351254");
        isoMsg.set(49, "566");
        isoMsg.set(52, new PinBlockUtilities().generatePinBlock(financialRequest.getPinData(), KeyStorage.retrieveKey("decryptedKeK").substring(0, 16), financialRequest.getPan()));
        isoMsg.set(70, "002");
        isoMsg.set(100, "082");

        return isoMsg;
    }


    @Override
    public String processKeyExchangeResponse(ISOMsg keyExchangeResponse) throws Exception {

        String encryptedZpk = keyExchangeResponse.getString(53);
        String zmkPartA = "63E4880A2D502DD8";
        String zmkPartB = "E835C68DD8061BBB";

        if (encryptedZpk != null) {
            String zpkPartA = encryptedZpk.substring(0, 16);

            String zpkPartB = encryptedZpk.substring(16, 32);

            System.out.println("ZPK Part A::: " + zpkPartA);
            System.out.println("ZPK Part B::: " + zpkPartB);

            String xorValue1 = ISOUtil.hexor("A6", zmkPartB.substring(0, 2));

            System.out.println("Xor Value 1:::"+xorValue1);

            String completeVariantedZMK1 =  zmkPartA+zmkPartB.replace("E8", xorValue1);

            // decrypt with 3des encryption algorithm for a varianted zmk part B
            byte[] decryptedValue1 = new PinBlockUtilities().decrypt3DESECB(zpkPartA.getBytes(), completeVariantedZMK1.getBytes());

            String decVal1 = new PinBlockUtilities().byteArrayToHexString(decryptedValue1).toUpperCase();
            System.out.println("Decrypted Value 1::: "+decVal1);

            // do for 5A
            String xorValue2 = ISOUtil.hexor("5A", zmkPartB.substring(0, 2));

            System.out.println("Xor Value 2:::"+xorValue2);

            // decrypt with 3des encryption algorithm for a varianted zmk part B
            String completedVariantedZMK2 = zmkPartA+zmkPartB.replace("E8", xorValue2);
            byte[] decryptedValue2 = new PinBlockUtilities().decrypt3DESECB(zpkPartB.getBytes(), completedVariantedZMK2.getBytes());

            String decVal2 = new PinBlockUtilities().byteArrayToHexString(decryptedValue2).toUpperCase();
            System.out.println("Decrypted Value 2::: "+decVal2);
            String clearZPK =decVal1.toUpperCase().substring(0, 16)+decVal2.toUpperCase().substring(16, 32);
            KeyStorage.storeKey("decryptedKeK", clearZPK);


        }

        return keyExchangeResponse.toString();
    }

    @Override
    public String formulateDE4(double amount) {
        long amountInSmallestUnit = Math.round(amount * 100);

        return String.format("%012d", amountInSmallestUnit);
    }

    private static String encrypt(String plaintext, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding"); // 3DES cipher with ECB mode and PKCS5 padding
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(encryptedBytes); // Convert encrypted bytes to Base64 string
    }

    // Method to decrypt a ciphertext using 3DES
    private static String decrypt(String ciphertext, String variantedZmk) throws Exception {
        Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");

        SecretKey secretKey = new SecretKeySpec(variantedZmk.getBytes(), "DESede");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(ciphertext));
        return new String(decryptedBytes, "UTF-8");
    }



}
