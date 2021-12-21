package com.github.eros.common.lang;

import com.github.eros.common.exception.ErosError;
import com.github.eros.common.exception.ErosException;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/21 15:30
 */
public class MD5 {
    private MessageDigest messageDigest;
    private ReentrantLock opLock = new ReentrantLock();

    private MD5(){
        try {
            messageDigest = MessageDigest.getInstance("md5");
        } catch (Exception e) {
            throw new ErosException(ErosError.SYSTEM_ERROR, "getMessageDigestInstance error...", e);
        }
    }

    private static char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    private static Map<Character, Integer> rDigits = new HashMap<>(16);
    static {
        for (int i = 0; i < digits.length; ++i) {
            rDigits.put(digits[i], i);
        }
    }

    private static class MD5Holder {
        private static final MD5 INSTANCE = new MD5();
    }

    public static MD5 getInstance() {
        return MD5Holder.INSTANCE;
    }

    public String getMD5(String content) {
        return bytes2string(hash(content));
    }


    public String getMD5(byte[] content) {
        return bytes2string(hash(content));
    }


    public byte[] hash(String str) {
        opLock.lock();
        try {
            byte[] bt = messageDigest.digest(str.getBytes(Charset.defaultCharset()));
            if (null == bt || bt.length != 16) {
                throw new IllegalArgumentException("md5 need");
            }
            return bt;
        } finally {
            opLock.unlock();
        }
    }

    public byte[] hash(byte[] data) {
        opLock.lock();
        try {
            byte[] bt = messageDigest.digest(data);
            if (null == bt || bt.length != 16) {
                throw new IllegalArgumentException("md5 need");
            }
            return bt;
        }
        finally {
            opLock.unlock();
        }
    }

    public String bytes2string(byte[] bt) {
        int l = bt.length;

        char[] out = new char[l << 1];

        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = digits[(0xF0 & bt[i]) >>> 4];
            out[j++] = digits[0x0F & bt[i]];
        }

        return new String(out);
    }

    public byte[] string2bytes(String str) {
        if (null == str) {
            throw new NullPointerException("str is null");
        }
        if (str.length() != 32) {
            throw new IllegalArgumentException("md5 need");
        }
        byte[] data = new byte[16];
        char[] chs = str.toCharArray();
        for (int i = 0; i < 16; ++i) {
            int h = rDigits.get(chs[i * 2]);
            int l = rDigits.get(chs[i * 2 + 1]);
            data[i] = (byte) ((h & 0x0F) << 4 | (l & 0x0F));
        }
        return data;
    }


}
