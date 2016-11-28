import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Written by Zachary Rank on 11/17/2016
 */
public class Hash {

    /**
     * Generates an integrity check-sum hash of a given chunk
     * 
     * @param chunk the chunk to be check-summed
     * @return the check-sum hash of the chunk
     */
    public static byte[] generateCheckSum(byte[] chunk) {
        return generateCheckSum(chunk, Constants.CHECKSUM_REPETITIONS);
    }

    private static byte[] generateCheckSum(byte[] chunk, int repetitions) throws ArrayIndexOutOfBoundsException{
        //check for no data
        try {
            byte test = chunk[0];
        }
        catch(ArrayIndexOutOfBoundsException e) {
            System.out.println(e.getStackTrace());
            return new byte[0];
        }

        byte[] hash = new byte[chunk.length * 2];

        for(int i = 0; i < hash.length; i++) {
            if(i < hash.length/2) {
                if(i == hash.length/2 - 1) {
                    hash[i] = (byte) (chunk[i] * chunk[0]);
                }
                else {
                    hash[i] = (byte) (chunk[i] * chunk[i + 1]);
                }
            }
            else {
                int j = i - chunk.length;
                if(i == hash.length - 1) {
                    hash[i] = (byte) (chunk[j] + chunk[chunk.length/2]);
                }
                else {
                    hash[i] = (byte) (chunk[j] + chunk[j + 1]);
                }
            }
        }

        byte[] reducedHash = new byte[chunk.length / 2];

        for(int i = 0; i < reducedHash.length; i++) {
            reducedHash[i] = (byte) ((hash[i] + hash[i + reducedHash.length * 2]) * (hash[i + reducedHash.length]) + hash[i + reducedHash.length * 3]);
        }
        if(reducedHash.length == 0) {
            byte[] alternateHash = new byte[1];
            alternateHash[0] = (byte) (chunk[0] * chunk[0]);
            if(repetitions == 0) {
                return alternateHash;
            }
            else
                return generateCheckSum(alternateHash, repetitions - 1);
        }

        if(repetitions == 0) {
            return reducedHash;
        }
        else
            return generateCheckSum(reducedHash, repetitions - 1);

    }

    /**
     * Generates a hash of a salt and password concatenated together for storage with a ":". 
     * Hashes are generated using SHA-512 and then base64 encoded to remove garbage characters.
     * 
     * @param salt the salt to hash
     * @param password the password to hash
     * @return hash of password and salt
     * @throws NoSuchAlgorithmException 
     */
    public static String generatePasswordHash(String salt, String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        byte[] result = md.digest((salt + password).getBytes());
        return bytesToHex(result);
    }

    final protected static char[] hexArray = "0123456789abcdef".toCharArray();
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
