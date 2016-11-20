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
        return hash;
    }

    /**
     * Generates a hash of a salt and password concatenated together for storage with a ":". Hashes are generated using SHA-512
     * 
     * @param salt the salt to hash
     * @param password the password to hash
     * @return hash of password and salt
     * @throws NoSuchAlgorithmException 
     */
    public static byte[] generatePasswordHash(byte[] salt, byte[] password) throws NoSuchAlgorithmException {
        String hash = "";

        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(salt);
        hash += new String(md.digest());
        hash += ":";
        md.update(password);
        hash += new String(md.digest());

        return hash.getBytes();
    }

    /**
     * Compares two check-sum values to see if they are the same
     * 
     * @param sum1 first check-sum to compare
     * @param sum2 second check-sum to compare
     * @return true if both check-sum values are the same, false otherwise
     */
    public static boolean compareCheckSums(byte[] sum1, byte[] sum2) {
        boolean same = true;
        for(int i = 0; i < sum1.length; i++) {
            if(sum1[i] != sum2[i]) {
                same = false;
                i = sum1.length;
            }
        }
        return same;
    }
}
