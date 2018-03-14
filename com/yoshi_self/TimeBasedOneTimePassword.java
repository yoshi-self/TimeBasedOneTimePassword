package com.yoshi_self;

import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.yoshi_self.Base32Decoder;

public class TimeBasedOneTimePassword {
    // Parameters are same as Google Authenticator
    public static final int TOKEN_LENGTH = 6;
    public static final long TIME_STEP = 30;
    public static final long TIME_START = 0;
    public static final int SECRET_LENGTH = 10; // bytes

    private String secret;

    private TimeBasedOneTimePassword() {
    }

    public TimeBasedOneTimePassword(String secret) {
        this.secret = secret;
    }

    public String calcOneTimePassword()
        throws NoSuchAlgorithmException, InvalidKeyException
    {
        // base32 decode secret
        byte[] secretBytes = decodeBase32(this.secret);

        // Java has no unsigned logn but bytes are same as unsigned until 9223372036854775807
        long now = System.currentTimeMillis() / 1000L;
        long counter = (now - TIME_START) / TIME_STEP;

        // convert long to bytes[]
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(counter);
        byte[] data = buffer.array();

        // HmacSha1
        byte[] hashed = calcHmacSha1(data, secretBytes);

        // truncate
        String result = truncate(hashed);
        return result;
    }

    public byte[] calcHmacSha1(byte[] data, byte[] key)
        throws NoSuchAlgorithmException, InvalidKeyException
    {
        SecretKeySpec secretKey = new SecretKeySpec(key, "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(secretKey);
        byte[] bytes = mac.doFinal(data);
        return bytes;
    }

    /**
     * @param bytes byte[] array of 20 bytes
     */
    public String truncate(byte[] bytes) {
        byte last = bytes[bytes.length - 1];
        // use 4 bits
        int offset = last & 0xF;
        long dt = 0;
        // make 4 bytes using from [offset] to [offset + 3]
        for(int i = 0; i < 4; ++i) {
            dt <<= 8;
            dt |= (bytes[offset + i] & 0xFF);
        }
        // use 31 bits
        dt &= 0x7FFFFFFF;

        dt %= Math.pow(10, TOKEN_LENGTH);
        String result = String.format("%06d", dt);
        return result;
    }

    public byte[] decodeBase32(String source) {
        Base32Decoder decoder = new Base32Decoder(source);
        byte[] r =  decoder.decodeToBytes(this.SECRET_LENGTH);
        return r;
    }
}
