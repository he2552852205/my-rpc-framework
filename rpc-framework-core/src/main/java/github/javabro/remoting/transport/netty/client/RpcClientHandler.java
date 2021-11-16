package github.javabro.remoting.transport.netty.client;

import github.javabro.enums.CompressTypeEnum;
import github.javabro.enums.SerializationEnum;
import github.javabro.factory.SingletonFactory;
import github.javabro.remoting.constants.RpcConstant;
import github.javabro.remoting.dto.RpcMessage;
import github.javabro.remoting.dto.RpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: HeZhongPan
 * @Date: 2021/11/10/9:01
 * @Description:
 */
@Slf4j
public class RpcClientHandler extends ChannelInboundHandlerAdapter {

    private final UnprocessedFutureCache unprocessedFutureCache;
    private final NettyRpcClient nettyRpcClient;


    public RpcClientHandler(){
        unprocessedFutureCache = SingletonFactory.getSingleton(UnprocessedFutureCache.class);
        nettyRpcClient = SingletonFactory.getSingleton(NettyRpcClient.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            log.info("client receive msg : {} ", msg.toString());
            if (msg instanceof RpcMessage) {
                RpcMessage message = (RpcMessage) msg;
                //获取到消息类型
                byte messageType = message.getMessageType();
                if (messageType == RpcConstant.HEARTBEAT_RESPONSE_TYPE) {
                    log.info("heart [{}]", message.getData());
                } else if (messageType == RpcConstant.RESPONSE_TYPE) {
                    RpcResponse rpcResponse = (RpcResponse) message.getData();
                    unprocessedFutureCache.complete(rpcResponse);
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                log.info("write idle happen [{}]", ctx.channel().remoteAddress());
                Channel channel = nettyRpcClient.getChannel((InetSocketAddress) ctx.channel().remoteAddress());
                RpcMessage rpcMessage = new RpcMessage();
                rpcMessage.setCodec(SerializationEnum.KRYO.getCode());
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                rpcMessage.setMessageType(RpcConstant.HEARTBEAT_REQUEST_TYPE);
                rpcMessage.setData(RpcConstant.PING);
                channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("client catch exception：", cause);
        cause.printStackTrace();
        ctx.close();
    }
}
