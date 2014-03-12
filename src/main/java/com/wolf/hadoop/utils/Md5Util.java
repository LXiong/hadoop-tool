package com.wolf.hadoop.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author aladdin
 */
public class Md5Util {

    /**
     *
     * @param str
     * @return
     */
    public static String encryptByMd5(String str) {
        String result = "";
        try {
            MessageDigest algorithm = MessageDigest.getInstance("MD5");
            algorithm.reset();
            byte[] messageDigest = algorithm.digest(str.getBytes());
            result = byteToHexString(messageDigest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private static String byteToHexString(byte[] textByte) {
        StringBuilder hexString = new StringBuilder(32);
        int byteValue;
        for (byte bt : textByte) {
            byteValue = 0xFF & bt;
            if (byteValue < 16) {
                hexString.append("0").append(Integer.toHexString(byteValue));
            } else {
                hexString.append(Integer.toHexString(byteValue));
            }
        }
        return hexString.toString();
    }
}
