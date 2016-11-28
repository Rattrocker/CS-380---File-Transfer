/**
 * Created by cthill on 11/16/16.
 */
public class Constants {
    public static final int CHUNK_SIZE = 4096;
    // amount of times to repeat check sum generation; higher numbers create shorter hashes
    public static final int CHECKSUM_REPETITIONS = 30;
    public static final int MAX_CHUNK_RETRY = 10;
    // maximum number of auth attempts
    public static final int MAX_AUTH_ATTEMPTS = 3;

    // packet headers
    public static final byte PH_CHUNK_DATA = 0x0;
    public static final byte PH_AUTH = 0x1;
    public static final byte PH_DISCONNECT = 0x2;
    public static final byte PH_START_TRANSMIT = 0x3;
    public static final byte PH_CHUNK_ERROR = 0x4;
    public static final byte PH_CHUNK_OK = 0x5;
    public static final byte PH_PROTO_ERROR = 0x6;

}
