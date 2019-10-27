import java.util.HashMap;

/**
 * Written by Zahy Abou-Diab, Y-Uyen La, and Zachary Rank on 11/16/2016
 */
public class Base64 {
    protected static Base64Alphabet table = new Base64Alphabet();

    /**
     * Base64 encoded version of a chunk
     *
     * @param chunk chunk to encode
     * @return encoded string
     */
    public static String b64Encode(byte[] chunk) {
        // use string builder for performance
        StringBuilder out = new StringBuilder();

        // encode 3 bytes at a time. Do all but last group
        for (int i = 0; i < chunk.length - chunk.length % 3; i += 3) {
            // encode the 3 bytes as 4 base64 characters
            byte b1 = chunk[i];
            byte b2 = chunk[i + 1];
            byte b3 = chunk[i + 2];

            // take upper 6 bits of b1
            int c1 = (b1 & 0b11111100) >> 2;
            // take lower 2 bits of b1 and upper 4 bits of b2
            int c2 = ((b1 & 0b00000011) << 4) | ((b2 & 0b11110000) >> 4);
            // take lower 4 bits of b2 and upper 2 bits of b3
            int c3 = ((b2 & 0b00001111) << 2) | ((b3 & 0b11000000) >> 6);
            // take lower 6 bits of b3
            int c4 = b3 & 0b00111111;

            // lookup and append the characters
            out.append(table.encode.get(c1));
            out.append(table.encode.get(c2));
            out.append(table.encode.get(c3));
            out.append(table.encode.get(c4));
        }

        // handle last group
        int lastGroupLen = chunk.length % 3;
        if (lastGroupLen == 1) {
            // read last byte
            byte b = chunk[chunk.length - 1];
            // take upper 6 bits of last byte
            int c1 = (b & 0b11111100) >> 2;
            // take lower 2 bits of last byte
            int c2 = (b & 0b00000011) << 4;

            // lookup and append the characters
            out.append(table.encode.get(c1));
            out.append(table.encode.get(c2));
            out.append("==");
        } else if (lastGroupLen == 2) {
            // read last two bytes
            byte b1 = chunk[chunk.length - 2];
            byte b2 = chunk[chunk.length - 1];

            // take upper 6 bits of b1
            int c1 = (b1 & 0b11111100) >> 2;
            // take lower 2 bits of b1 and upper 4 bits of b2
            int c2 = ((b1 & 0b00000011) << 4) | ((b2 & 0b11110000) >> 4);
            // take lower 4 bits of b2
            int c3 = ((b2 & 0b00001111) << 2);

            // lookup and append the characters
            out.append(table.encode.get(c1));
            out.append(table.encode.get(c2));
            out.append(table.encode.get(c3));
            out.append("=");
        }

        return out.toString();
    }

    /**
     * Base64 decoded version of chunk
     *
     * @param base64 string to decode
     * @return decoded chunk
     */
    public static byte[] b64Decode(String base64) {
        // calculate padding bytes
        int paddingBytes = 0;
        if (base64.endsWith("==")) {
            paddingBytes = 2;
        } else if (base64.endsWith("=")) {
            paddingBytes = 1;
        }

        // allocate array
        byte[] out = new byte[(base64.length() / 4) * 3 - paddingBytes];

        // setup variables
        int writeAt = 0;
        int dataSize = base64.length();
        if (paddingBytes > 0) {
            dataSize -= 4;
        }

        // decode 4 characters at a time (3 bytes)
        for (int i = 0; i < dataSize; i += 4) {
            int c1 = table.decode.get(base64.charAt(i));
            int c2 = table.decode.get(base64.charAt(i + 1));
            int c3 = table.decode.get(base64.charAt(i + 2));
            int c4 = table.decode.get(base64.charAt(i + 3));

            // build b1 using c1 and two bits from c2
            byte b1 = (byte) (((c1 & 0b00111111) << 2) | ((c2 & 0b00110000) >> 4));
            // build b2 using lower 4 bits from c2 and 4 bits from c3
            byte b2 = (byte) (((c2 & 0b00001111) << 4) | ((c3 & 0b00111100) >> 2));
            // build b3 using lower 2 bits from c3 and 6 bits from c4
            byte b3 = (byte) (((c3 & 0b00000011) << 6) | ((c4 & 0b00111111)));

            // write to output buffer
            out[writeAt++] = b1;
            out[writeAt++] = b2;
            out[writeAt++] = b3;
        }

        // handle last group
        if (paddingBytes == 1) {
            int len = base64.length();
            int c1 = table.decode.get(base64.charAt(len - 4));
            int c2 = table.decode.get(base64.charAt(len - 3));
            int c3 = table.decode.get(base64.charAt(len - 2));

            // build b1 using c1 and two bits from c2
            byte b1 = (byte) (((c1 & 0b00111111) << 2) | ((c2 & 0b00110000) >> 4));
            // build b2 using lower 4 bits from c2 and 4 bits from c3
            byte b2 = (byte) (((c2 & 0b00001111) << 4) | ((c3 & 0b00111100) >> 2));

            // write to output buffer
            out[writeAt++] = b1;
            out[writeAt++] = b2;
        } else if (paddingBytes == 2) {
            int len = base64.length();
            int c1 = table.decode.get(base64.charAt(len - 4));
            int c2 = table.decode.get(base64.charAt(len - 3));

            // build b1 using c1 and two bits from c2
            byte b1 = (byte) (((c1 & 0b00111111) << 2) | ((c2 & 0b00110000) >> 4));

            // write to output buffer
            out[writeAt++] = b1;
        }

        return out;
    }
}

class Base64Alphabet {
    public HashMap<Integer, Character> encode;
    public HashMap<Character, Integer> decode;

    public Base64Alphabet() {
        encode = new HashMap<>();
        decode = new HashMap<>();

        //"A-Z"
        for (int i = 0; i < 26; i++) {
            encode.put(i, (char) (i + 65));
            decode.put((char) (i + 65), i);
        }

        //"a-z"
        for (int i = 26; i < 52; i++) {
            encode.put(i, (char) (i + 71));
            decode.put((char) (i + 71), i);
        }

        //"0-9"
        for (int i = 52; i < 62; i++) {
            encode.put(i, (char) (i - 4));
            decode.put((char) (i - 4), i);
        }

        //"+"
        encode.put(62, (char) 43);
        decode.put((char) 43, 62);

        //"/"
        encode.put(63, (char) 47);
        decode.put((char) 47, 63);

        //"="
        encode.put(64, (char) 61);
        decode.put((char) 61, 64);
    }
}
