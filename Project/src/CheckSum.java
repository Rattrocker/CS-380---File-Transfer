
/**
 * Written by Zachary Rank on 11/17/2016
 */
public class CheckSum {

    /**
     * Generates the check-sum value of a given chunk
     * 
     * @param chunk the chunk to be check-summed
     * @return the check-sum of the chunk
     */
    public static byte[] generateCheckSum(byte[] chunk) {
        //TODO: everything
        
        return new byte[32];
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
