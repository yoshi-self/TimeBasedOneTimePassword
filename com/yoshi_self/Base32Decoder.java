package com.yoshi_self;

import java.util.BitSet;
import java.util.List;
import java.util.Arrays;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


public class Base32Decoder {
    // NOTE: Base32 encodes 5 bytes to 8 bytes
    public static final int ENCODE_UNIT = 5;
    public static final int DECODE_UNIT = 8;
    // byte values of ABCDEFGHIJKLMNOPQRSTUVWXYZ234567
    public static final List<Byte> TABLE = Arrays.asList(new Byte[]{
       65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80,
       81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 50, 51, 52, 53, 54, 55
    });
    public static final byte PADDING_BYTE = 61; // byte value of =
    protected byte[] source = new byte[0]; // encoded bytes array
    protected int pos = 0; // index of decoded in source

    public Base32Decoder() {
    }

    public Base32Decoder(byte[] source) {
        this.source = source; 
    }

    public Base32Decoder(String source) {
        this.source = source.getBytes(StandardCharsets.US_ASCII); 
        //this.source = source.getBytes(); 
    }

    public void setSource(byte[] source) {
        this.source = source;
        this.pos = 0;
    }

    /**
     * Decode this.source to String with specified charset
     *
     * @param Charset charset of original string
     * @return String decoded String
     */
    public String decodeToString(Charset charset) {
        if(this.source.length == 0) {
            return "";
        }

        int bufferSize = this.source.length / this.DECODE_UNIT * this.ENCODE_UNIT;
        ByteBuffer resultBuffer = ByteBuffer.allocate(bufferSize);

        // decode
        for(this.pos = 0; this.pos < this.source.length; this.pos += this.DECODE_UNIT) {
            byte[] unit = decodeUnit();
            resultBuffer.put(unit);
        }

        String result = new String(resultBuffer.array(), charset);

        // find last index not null and return until it
        int lastIdx = result.length() - 1;
        for(int i = lastIdx; i >= 1; --i) {
            if(result.charAt(i) == '\000') {
                lastIdx = i - 1;
            }
            else {
                break;
            }
        }
        return result.substring(0, lastIdx + 1);
    }

    /**
     * Decode this.source to bytes[] of specified length
     *
     * @param int byte length of original bytes
     * @return byte[] decoded byte[] of specified length
     */
    public byte[] decodeToBytes(int length) {
        if(this.source.length == 0) {
            return new byte[]{};
        }

        int bufferSize = this.source.length / this.DECODE_UNIT * this.ENCODE_UNIT;
        ByteBuffer resultBuffer = ByteBuffer.allocate(bufferSize);

        // decode
        for(this.pos = 0; this.pos < this.source.length; this.pos += this.DECODE_UNIT) {
            byte[] unit = decodeUnit();
            resultBuffer.put(unit);
        }

        return Arrays.copyOfRange(resultBuffer.array(), 0, length);
    }

    /**
     * Decode 8 bytes to 5 bytes
     *
     * @return byte[] decoded 5 bytes
     */
    protected byte[] decodeUnit() {
        int bitSize =  this.DECODE_UNIT * this.ENCODE_UNIT;
        BitSet bitSet = new BitSet(bitSize);
        
        for(int i = this.pos, bitPos = 0; i < this.pos + this.DECODE_UNIT; ++i, bitPos += this.ENCODE_UNIT) {
            byte b = this.source[i];
            if(b == this.PADDING_BYTE) {
                continue;
            }
            int n = TABLE.indexOf(b);
            // set 5 bits from n
            for(int j = 0; j < this.ENCODE_UNIT; ++j) {
                if( ( (n >>> (this.ENCODE_UNIT - 1 - j)) & 1 ) != 0) {
                    bitSet.set(bitPos + j);
                }
            }
        }

        // make result byte array
        // NOTE: initial value of byte is 0
        byte[] unitBytes = new byte[this.ENCODE_UNIT];
        // set bits of byte array same as bitSet
        for(int i = 0; i < bitSize; ++i) {
            int bit = bitSet.get(i) ? 1 : 0;
            unitBytes[i/this.DECODE_UNIT] |= (bit << (this.DECODE_UNIT -1 - i % this.DECODE_UNIT));
        }

        return unitBytes;
    }
}
