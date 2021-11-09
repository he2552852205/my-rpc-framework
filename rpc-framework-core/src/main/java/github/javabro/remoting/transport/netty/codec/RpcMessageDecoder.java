package github.javabro.remoting.transport.netty.codec;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * Created with IntelliJ IDEA.
 *
 *  <p>
 *  * custom protocol decoder
 *  * <p>
 *  * <pre>
 *  *   0     1     2     3     4        5     6     7     8         9          10      11     12  13  14   15 16
 *  *   +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+----- --+-----+-----+-------+
 *  *   |   magic   code        |version | full length         | messageType| codec|compress|    RequestId       |
 *  *   +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 *  *   |                                                                                                       |
 *  *   |                                         body                                                          |
 *  *   |                                                                                                       |
 *  *   |                                        ... ...                                                        |
 *  *   +-------------------------------------------------------------------------------------------------------+
 *  * 4B  magic code（魔法数）   1B version（版本）   4B full length（消息长度）    1B messageType（消息类型）
 *  * 1B compress（压缩类型） 1B codec（序列化类型）    4B  requestId（请求的Id）
 *  * body（object类型数据）
 *  * </pre>
 *
 * @Author: HeZhongPan
 * @Date: 2021/11/09/8:19
 * @Description: 解码
 */
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder {




    public RpcMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
                             int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }
}
