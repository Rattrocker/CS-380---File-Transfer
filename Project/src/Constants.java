/**
 * Created by cthill on 11/16/16.
 */
public class Constants {
    public static final int CHUNK_SIZE = 4096;

    //packet headers
    public static final byte PH_CHUNK_DATA = 0x0;

    //amount of times to repeat check sum generation
    public static final int CHECK_SUM_REPETITIONS = 3;
}
