package github.javabro.remoting.transport.netty.server;

import github.javabro.enums.CompressTypeEnum;
import github.javabro.enums.RpcResponseCodeEnum;
import github.javabro.enums.SerializationEnum;
import github.javabro.factory.SingletonFactory;
import github.javabro.remoting.constants.RpcConstant;
import github.javabro.remoting.dto.RpcMessage;
import github.javabro.remoting.dto.RpcRequest;
import github.javabro.remoting.dto.RpcResponse;
import github.javabro.remoting.handler.RpcRequestHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
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

    private final RpcRequestHandler rpcRequestHandler;

    public RpcServerHandler(){
        rpcRequestHandler = SingletonFactory.getSingleton(RpcRequestHandler.class);
    }


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
                if (messageType == RpcConstant.HEARTBEAT_REQUEST_TYPE) {
                    rpcMessage.setMessageType(RpcConstant.HEARTBEAT_RESPONSE_TYPE);
                    rpcMessage.setData(RpcConstant.PONG);
                } else {
                    RpcRequest rpcRequest = (RpcRequest) rpcMessage.getData();
                    //调用本地服务
                    Object result = rpcRequestHandler.handle(rpcRequest);
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

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                log.info("idle check happen, so close the connection");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }
}

