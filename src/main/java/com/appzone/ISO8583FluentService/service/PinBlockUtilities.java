package com.appzone.ISO8583FluentService.service;

import org.bouncycastle.crypto.engines.DESEngine;
import org.bouncycastle.crypto.params.DESedeParameters;
import org.bouncycastle.util.encoders.Hex;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class PinBlockUtilities {

    public byte[] generatePinBlock(String pin, String zpk, String cardPan) {
        String pinString = "0" + pin.length() + pin;
        pinString = padRight(pinString, 16, 'F');
        byte[] pinBlock1 = hexStringToByteArray(pinString);
        String treatedPan = cardPan.substring(cardPan.length() - 13, cardPan.length() - 1);
        treatedPan = padLeft(treatedPan, 16, '0');
        byte[] pinBlock2 = hexStringToByteArray(treatedPan);
        byte[] xor = xorIt(pinBlock1, pinBlock2);
        byte[] pinBlockZpk = new byte[8];
        byte[] zpkBytes = hexStringToByteArray(zpk);
        DESedeParameters keyParam = new DESedeParameters(zpkBytes);
        DESEngine desEngine = new DESEngine();
        desEngine.init(true, keyParam);
        desEngine.processBlock(xor, 0, pinBlockZpk, 0);
        return pinBlockZpk;
    }

    public String byteArrayToHexString(byte[] ba) {
        return Hex.toHexString(ba);
    }

    public byte[] hexStringToByteArray(String hex) {
        return Hex.decode(hex);
    }

    public byte[] decrypt3DESECB(byte[] keyBytes, byte[] dataBytes) {
        try {

            if (keyBytes.length == 16) { // short key ? .. extend to 24 byte key
                byte[] tmpKey = new byte[24];
                System.arraycopy(keyBytes, 0, tmpKey, 0, 16);
                System.arraycopy(keyBytes, 0, tmpKey, 16, 8);
                keyBytes = tmpKey;
            }

            SecretKeySpec newKey = new SecretKeySpec(keyBytes, "DESede");
            Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, newKey);
            return cipher.doFinal(dataBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static byte[] xorIt(byte[] key, byte[] input) {
        byte[] bytes = new byte[input.length];
        for (int i = 0; i < input.length; i++) {
            bytes[i] = (byte) (input[i] ^ key[i % key.length]);
        }
        return bytes;
    }

    public static String padRight(String str, int length, char padChar) {
        while (str.length() < length) {
            str += padChar;
        }
        return str;
    }

    public static String padLeft(String str, int length, char padChar) {
        while (str.length() < length) {
            str = padChar + str;
        }
        return str;
    }


}

