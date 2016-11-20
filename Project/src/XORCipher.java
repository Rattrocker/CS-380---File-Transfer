
/**
 * Written by Zahy Abou-Diab and Zachary Rank on 11/15/2016
 */
public class XORCipher {

    /**
     * XOR encrypts or decrypts a chunk given a key
     * 
     * @param chunk input chunk as a byte Array
     * @param key input key as a byte Array
     * @return encrypted/decrypted chunk
     */
    public static byte[] xorCipher(byte[] chunk, byte[] key) {
        String stringChunk = new String(chunk);
        String stringKey = new String(key);

        //pads key with itself to accomodate chunk length
        String keyTemp = stringKey;
        while(keyTemp.length() < stringChunk.length()) {
            keyTemp += key;
        }

        //chunk and key as byte Arrays
        byte[] chunkBytes = stringChunk.getBytes();
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
