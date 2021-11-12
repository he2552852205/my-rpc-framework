package github.javabro.remoting.transport.netty.client;

import github.javabro.factory.SingletonFactory;
import github.javabro.remoting.constants.RpcConstant;
import github.javabro.remoting.dto.RpcMessage;
import github.javabro.remoting.dto.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

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

    public RpcClientHandler(){
        unprocessedFutureCache = SingletonFactory.getSingleton(UnprocessedFutureCache.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            log.info("client receive msg : {} ", msg.toString());
            if (msg instanceof RpcMessage) {
                RpcMessage message = (RpcMessage) msg;
                //获取到消息类型
                byte messageType = message.getMessageType();
                if (messageType == RpcConstant.RESPONSE_TYPE) {
                    RpcResponse rpcResponse = (RpcResponse) message.getData();
                    unprocessedFutureCache.complete(rpcResponse);
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}
