package github.javabro.remoting.transport.netty.codec;

import github.javabro.remoting.constant.RpcConstant;
import github.javabro.remoting.dto.RpcMessage;
import github.javabro.serialize.Serializer;
import github.javabro.serialize.kryo.KryoSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
 * @Date: 2021/11/09/8:18
 * @Description: 编码
 */
@Slf4j
public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> {

    private static final AtomicInteger atomicInteger = new AtomicInteger(0);
    private static final Serializer kryoSerializer = new KryoSerializer();

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage msg, ByteBuf out) throws Exception {

        try {
            //设置魔数
            out.writeBytes(RpcConstant.MAGIC_NUMBER);
            //设置版本
            out.writeByte(RpcConstant.VERSION);
            //越过full length最后再设置
            out.writerIndex(out.writerIndex() + 4);
            //消息类型
            out.writeByte(msg.getMessageType());
            //序列化类型
            out.writeByte(msg.getCodec());
            //压缩类型
            out.writeByte(msg.getCompress());
            //请求id
            out.writeByte(atomicInteger.getAndIncrement());

            int fullLength = RpcConstant.HEAD_LENGTH;
            //body
            byte[] body = kryoSerializer.serialize(msg.getData());
            fullLength += body.length;
            //TODO: compress

            if (body != null) {
                out.writeBytes(body);
            }
            //填充fullLength
            int index = out.writerIndex();
            out.writeByte(index - fullLength + RpcConstant.MAGIC_NUMBER.length + 1);
            out.writeByte(fullLength);
            out.writerIndex(index);
        } catch (Exception e) {
            log.error("message encode fail");
        }
    }
}
