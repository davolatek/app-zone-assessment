package com.appzone.ISO8583FluentService.service;

import com.appzone.ISO8583FluentService.model.FinancialRequest;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
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


        if (encryptedZpk != null) {
            decryptZpk(encryptedZpk);

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


    public void decryptZpk(String encryptedZpk) {
        String zpk16A = encryptedZpk.substring(0, 16);
        String zpk16B = encryptedZpk.substring(16, 32);

        String zmk16A = "63E4880A2D502DD8";
        String zmk16B = "E835C68DD8061BBB";
        String variantZmKA = zmk16B.substring(0, 2);
        String partA = zmk16B.substring(2, 16);

        String variantZmkBOne = ISOUtil.hexor("A6", variantZmKA) + partA;
        String newZmk = zmk16A + variantZmkBOne;
        String clearZpkA = new PinBlockUtilities().byteArrayToHexString(new PinBlockUtilities().decrypt3DESECB(new PinBlockUtilities().hexStringToByteArray(newZmk), new PinBlockUtilities().hexStringToByteArray(zpk16A)));


        String variantZmkBTwo = ISOUtil.hexor("5A", variantZmKA) + partA;
        String newZmk2 = zmk16A + variantZmkBTwo;
        String clearZpkB = new PinBlockUtilities().byteArrayToHexString(new PinBlockUtilities().decrypt3DESECB(new PinBlockUtilities().hexStringToByteArray(newZmk2), new PinBlockUtilities().hexStringToByteArray(zpk16B)));

        String clearZpk = clearZpkA.substring(0, 16) + clearZpkB.substring(0, 16);
        System.out.print("Clear ZPK Stored::: " + clearZpk.toUpperCase());
        KeyStorage.storeKey("decryptedKeK", clearZpk.toUpperCase());
    }

}
