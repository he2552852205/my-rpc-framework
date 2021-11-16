package github.javabro.remoting.constants;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: HeZhongPan
 * @Date: 2021/11/09/14:22
 * @Description: 常数类
 */
public class RpcConstant {

    public static final byte[] MAGIC_NUMBER = new byte[]{'c', 'a', 'f', 'e'};
    public static final byte VERSION = 1;
    public static final byte REQUEST_TYPE = 1;
    public static final byte RESPONSE_TYPE = 2;
    //ping
    public static final byte HEARTBEAT_REQUEST_TYPE = 3;
    //pong
    public static final byte HEARTBEAT_RESPONSE_TYPE = 4;

    public static final String PING = "ping";
    public static final String PONG = "pong";

    public static final int HEAD_LENGTH = 16;
    public static final int TOTAL_LENGTH = 16;



    public static final int MAX_FRAME_LENGTH = 8 * 1024 * 1024;

}
