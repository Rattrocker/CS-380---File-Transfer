
public class ASCIIArmor {

	/**
	 * XOR encrypts or decrypts a chunk given a key
	 * 
	 * @param chunk
	 * @param key
	 * @return encrypted/decrypted chunk
	 */
	public static byte[] xorCipher(String chunk, String key) {
		//pads key with itself to accomodate chunk length
		String keyTemp = key;
		while(keyTemp.length() < chunk.length()) {
			keyTemp += key;
		}
		
		//chunk and key as byte Arrays
		byte[] chunkBytes = chunk.getBytes();
		byte[] keyBytes = keyTemp.getBytes();

		//XOR encypted/decrypted chunk
		byte[] encryptedBytes = new byte[chunkBytes.length];
		
		//encrypt/decrypt
		for(int i = 0; i < chunkBytes.length; i++) {
			encryptedBytes[i] = (byte) (chunkBytes[i] ^ keyBytes[i]);
		}
		return encryptedBytes;
	}
	
}
