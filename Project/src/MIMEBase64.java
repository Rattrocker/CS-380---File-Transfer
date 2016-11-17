import java.io.IOException;
import java.util.HashMap;

public class MIMEBase64 {

	public static Base64Alphabet table = new Base64Alphabet();

	public static void main(String[] args) throws IOException {

		String test = "A\tB\nX X";
		System.out.println("String: " + test);
		System.out.println("Binary: " + stringToBinary(test)+ "\n");

		System.out.println("ENCODING");
		String testString = b64Encode(test);		
		System.out.println("Encoded String: " + testString);
		System.out.println("Binary: " + stringToBinary(testString) + "\n");

		String xorKey = "ASDSFDGSDFGWE#@$@#%#RGFE#4342423fr23";
		System.out.println("ASCII ARMOR");
		testString = new String(ASCIIArmor.xorCipher(testString, xorKey));
		System.out.println("Ciphered String: " + testString);
		testString = new String(ASCIIArmor.xorCipher(testString, xorKey));
		System.out.println("DeCiphered String: " + testString + "\n");

		System.out.println("DECODING");
		testString = b64Decode(testString);
		System.out.println("Decoded String: " + testString);
		System.out.println("Binary: " + stringToBinary(testString) + "\n");
	}

	/**
	 * Base64 encoded version of String
	 * 
	 * @param encoded String to encode
	 * @return encoded String
	 */
	public static String b64Encode(String encode) {
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
		return output;
	}

	/**
	 * Base64 decoded version of String
	 * 
	 * @param decode String to decode
	 * @return String
	 */
	public static String b64Decode(String decode) {
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
		return output;
	}

	/**
	 * Turns a String encoded in Base64 into its binary value based on the Base64 Alphabet
	 * 
	 * @param base64 BAse64 encoded String
	 * @return Bsae64 encoded binary value
	 */
	public static String base64ToBinary(String base64) {
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
	 * Converts a String of ASCII characters into its binary representation
	 * 
	 * @param string ASCII encoded String
	 * @return binary representation
	 */
	public static String stringToBinary(String string) {
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