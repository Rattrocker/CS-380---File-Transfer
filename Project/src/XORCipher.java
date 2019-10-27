
/**
 * Written by Zahy Abou-Diab and Zachary Rank on 11/15/2016
 */
public class XORCipher {

    /**
     * XOR encrypts or decrypts a chunk given a key
     *
     * @param chunk input chunk as a byte Array
     * @param key   input key as a byte Array
     * @return encrypted/decrypted chunk
     */
    public static byte[] xorCipher(byte[] chunk, byte[] key) {
        for (int i = 0; i < chunk.length; i++) {
            chunk[i] ^= key[i % key.length];
        }

        return chunk;
    }

}
