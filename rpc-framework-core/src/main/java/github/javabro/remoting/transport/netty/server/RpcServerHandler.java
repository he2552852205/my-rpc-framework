package github.javabro.remoting.transport.netty.server;

import github.javabro.enums.CompressTypeEnum;
import github.javabro.enums.RpcResponseCodeEnum;
import github.javabro.enums.SerializationEnum;
import github.javabro.factory.SingletonFactory;
import github.javabro.remoting.constant.RpcConstant;
import github.javabro.remoting.dto.RpcMessage;
import github.javabro.remoting.dto.RpcRequest;
import github.javabro.remoting.dto.RpcResponse;
import github.javabro.remoting.transport.netty.client.ChannelProvider;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: HeZhongPan
 * @Date: 2021/11/10/19:00
 * @Description:
 */
@Slf4j
public class RpcServerHandler extends ChannelInboundHandlerAdapter {


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            log.info("server received msg : {}", msg);
            if (msg instanceof RpcMessage) {
                RpcMessage rpcMessage = (RpcMessage) msg;
                byte messageType = rpcMessage.getMessageType();

                RpcMessage responseMessage = RpcMessage.builder().messageType(RpcConstant.RESPONSE_TYPE)
                        .compress(CompressTypeEnum.GZIP.getCode())
                        .codec(SerializationEnum.KRYO.getCode())
                        .request(rpcMessage.getRequest()).build();
                if (messageType == RpcConstant.REQUEST_TYPE) {
                    RpcRequest rpcRequest = (RpcRequest) rpcMessage.getData();
                    //TODO 调用本地服务
                    Object result = "result";
                    if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                        RpcResponse<Object> rpcResponse = new RpcResponse<>();
                        responseMessage.setData(rpcResponse.success(result, rpcRequest.getRequestId()));
                    } else {
                        log.error("channel is not active or writable");
                        RpcResponse<Object> rpcResponse = new RpcResponse<>();
                        responseMessage.setData(rpcResponse.fail(RpcResponseCodeEnum.FAIL));
                    }
                }
                ctx.writeAndFlush(responseMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}

