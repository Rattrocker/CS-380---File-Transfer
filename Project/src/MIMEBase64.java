import java.util.HashMap;

/**
 * Written by Zahy Abou-Diab, Yu-yen La, and Zachary Rank on 11/16/2016
 */
public class MIMEBase64 {

    /**
     * Base64 encoded version of a chunk
     * 
     * @param chunk chunk to encode
     * @return encoded chunk
     */
    public static byte[] b64Encode(byte[] chunk) {
        Base64Alphabet table = new Base64Alphabet();
        String encode = new String(chunk);

        //binary version of String
        String encodeBinary = stringToBinary(encode);

        //generate amount of characters to skip for padding
        int toSkip  = (encode.toCharArray().length * 8)/6;
        if((encode.toCharArray().length * 8) % 6 != 0) toSkip++;

        //turns String into 6-bit chunks separated by a space
        StringBuilder tester = new StringBuilder();
        for(int i = 0; i < encodeBinary.length(); i ++){
            tester.append(encodeBinary.charAt(i));
            if((i+1)%6 == 0){
                tester.append(" "); 
            }
        }

        //separate 6-bit chunks
        String[] splitter = tester.toString().split(" ");

        //convert padding into "=" characters
        for(int i = toSkip; i < splitter.length; i++){
            if(splitter[i].equals("000000")){
                splitter[i] = "1000000";
            }
        }

        //build String
        String output = "";
        for(String i : splitter) {
            output += table.encode.get(Integer.parseInt(i,2));
        }
        return output.getBytes();
    }

    /**
     * Base64 decoded version of chunk
     * 
     * @param chunk chunk to decode
     * @return decoded chunk
     */
    public static byte[] b64Decode(byte[] chunk) {
        String decode = new String(chunk);

        //binary version of String
        String decodeBinary = base64ToBinary(decode);

        //generate amount of characters to keep to discard padding
        int toKeep = decodeBinary.length() / 8;

        //turns String into 8-bit chunks separated by a space
        StringBuilder tester = new StringBuilder();
        for(int i = 0; i < decodeBinary.length(); i ++){
            tester.append(decodeBinary.charAt(i));			
            if((i + 1) % 8 == 0) tester.append(" ");
        }

        //separate 8-bit chunks
        String[] splitter = tester.toString().split(" ");

        //build String
        String output = "";
        for(int i = 0; i < toKeep; i++) {
            output += (char) Integer.parseInt(splitter[i], 2);
        }
        return output.getBytes();
    }

    /**
     * Turns a String encoded in Base64 into its binary value based on the Base64 Alphabet
     * 
     * @param base64 Base64 encoded String
     * @return Bsae64 encoded binary value
     */
    private static String base64ToBinary(String base64) {
        Base64Alphabet table = new Base64Alphabet();
        char[] chars = base64.toCharArray();
        StringBuilder string = new StringBuilder();
        for(Character c : chars) {
            if(!c.equals('=')) {
                int val = table.decode.get(c);
                for (int i = 0; i < 6; i++){
                    string.append((val & 32) == 0 ? 0 : 1);
                    val <<= 1;
                }
            }
        }
        return string.toString();
    }

    /**
     * Converts a String of ASCII characters into its binary representation with padding
     * 
     * @param string ASCII encoded String
     * @return binary representation
     */
    private static String stringToBinary(String string) {
        byte[] bytes = string.getBytes();
        StringBuilder binary = new StringBuilder();
        for (byte b : bytes){
            int val = b;
            for (int i = 0; i < 8; i++){
                binary.append((val & 128) == 0 ? 0 : 1);
                val <<= 1;
            }
        }

        //padding
        if(binary.toString().length() % 6 == 2) {
            binary.append("0000000000000000");
        }
        else if(binary.toString().length() % 6 == 4) {
            binary.append("00000000");
        }

        return binary.toString();
    }
}

class Base64Alphabet {

    HashMap<Integer, Character> encode;
    HashMap<Character, Integer> decode;

    public Base64Alphabet() {
        encode = new HashMap<Integer, Character>();
        decode = new HashMap<Character, Integer>();

        //"A-Z"
        for(int i = 0; i < 26; i++) {
            encode.put(i, (char) (i+65));
            decode.put((char) (i+65), i);
        }

        //"a-z"
        for(int i = 26; i < 52; i++) {
            encode.put(i, (char) (i+71));
            decode.put((char) (i+71), i);
        }

        //"0-9"
        for(int i = 52; i < 62; i++) {
            encode.put(i, (char) (i-4));
            decode.put((char) (i-4), i);
        }

        //"+"
        encode.put(62, (char) 43);
        decode.put((char) 43, 62);

        //"/"
        encode.put(63, (char) 47);
        decode.put((char) 47, 63);

        //"="
        encode.put(64, (char) 61);
        decode.put((char)61, 64);
    }
}