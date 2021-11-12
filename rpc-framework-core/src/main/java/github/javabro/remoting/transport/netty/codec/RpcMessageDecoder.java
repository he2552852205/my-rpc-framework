package github.javabro.remoting.transport.netty.codec;

import github.javabro.remoting.constants.RpcConstant;
import github.javabro.remoting.dto.RpcMessage;
import github.javabro.remoting.dto.RpcRequest;
import github.javabro.remoting.dto.RpcResponse;
import github.javabro.serialize.Serializer;
import github.javabro.serialize.kryo.KryoSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

import static github.javabro.remoting.transport.netty.codec.RpcMessageDecoder.checkMagicNumber;

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
@Slf4j
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder {

    private static final Serializer kryoSerializer = new KryoSerializer();

    /**
     * 5 代表长度字段要越过5个字节开始
     * 4 代表长度字段为4个字节
     * -9 因为fullLength是整个数据包信息，所以从当前字节回到第一个字节开始读
     */
    public RpcMessageDecoder() {
        this(RpcConstant.MAX_FRAME_LENGTH, 5, 4, -9, 0);
    }

    public RpcMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
                             int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decode = super.decode(ctx, in);
        if (decode instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf) decode;
            if (buf.readableBytes() >= RpcConstant.TOTAL_LENGTH) {
                try {
                    return decodeFrame(buf);
                } catch (Exception e) {
                    log.error("Decode frame error!", e);
                    throw e;
                } finally {
                    buf.release();
                }
            }
        }
        return decode;
    }

    private Object decodeFrame(ByteBuf byteBuf) {

        //检查魔数
        checkMagicNumber(byteBuf);
        //检查版本
        checkVersion(byteBuf);

        int fullLength = byteBuf.readInt();
        byte messageType = byteBuf.readByte();
        byte codec = byteBuf.readByte();
        byte compress = byteBuf.readByte();
        int requestId = byteBuf.readInt();
        RpcMessage rpcMessage = RpcMessage.builder()
                .messageType(messageType).codec(codec)
                .compress(compress).request(requestId).build();

        int bodyLen = fullLength - byteBuf.readableBytes();
        if (bodyLen > 0) {
            byte[] body = new byte[bodyLen];
            byteBuf.readBytes(body);
            //TODO: uncompress

            //TODO: 此处序列化方法暂时使用Kryo，后期会扩展
            if (messageType == RpcConstant.REQUEST_TYPE) {
                RpcRequest rpcRequest = kryoSerializer.deserialize(body, RpcRequest.class);
                rpcMessage.setData(rpcRequest);
            } else {
                RpcResponse rpcResponse = kryoSerializer.deserialize(body, RpcResponse.class);
                rpcMessage.setData(rpcResponse);
            }
        }
        return rpcMessage;
    }

    private static void checkVersion(ByteBuf byteBuf) {
        byte version = byteBuf.readByte();
        if (version != RpcConstant.VERSION) {
            throw new RuntimeException("Version is not matched :" + version);
        }
    }

    private static void checkMagicNumber(ByteBuf in) {
        int len = RpcConstant.MAGIC_NUMBER.length;
        byte[] tmp = new byte[len];
        in.readBytes(tmp);
        for (int i = 0; i < len; i++) {
            if (tmp[i] != RpcConstant.MAGIC_NUMBER[i]) {
                throw new IllegalArgumentException("Unknown magic code: " + Arrays.toString(tmp));
            }
        }
    }


}
