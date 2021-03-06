package github.javabro.remoting.transport.netty.client;

import github.javabro.enums.CompressTypeEnum;
import github.javabro.enums.SerializationEnum;
import github.javabro.extension.ExtensionLoader;
import github.javabro.factory.SingletonFactory;
import github.javabro.registry.ServiceDiscovery;
import github.javabro.registry.zk.ZkServiceDiscoveryImpl;
import github.javabro.remoting.constants.RpcConstant;
import github.javabro.remoting.dto.RpcMessage;
import github.javabro.remoting.dto.RpcRequest;
import github.javabro.remoting.dto.RpcResponse;
import github.javabro.remoting.transport.RpcRequestTransport;
import github.javabro.remoting.transport.netty.codec.RpcMessageDecoder;
import github.javabro.remoting.transport.netty.codec.RpcMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: HeZhongPan
 * @Date: 2021/11/09/20:42
 * @Description:
 */
@Slf4j
public class NettyRpcClient implements RpcRequestTransport {

    private final NioEventLoopGroup worker;
    private final Bootstrap bootstrap;
    private final ChannelProvider channelProvider;
    private final UnprocessedFutureCache unprocessedFutureCache;
    private final ServiceDiscovery serviceDiscovery;
    public NettyRpcClient() {
        worker = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap
                .group(worker)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new RpcMessageEncoder());
                        pipeline.addLast(new RpcMessageDecoder());
                        pipeline.addLast(new RpcClientHandler());
                    }
                });
        this.channelProvider = SingletonFactory.getSingleton(ChannelProvider.class);
        this.unprocessedFutureCache = SingletonFactory.getSingleton(UnprocessedFutureCache.class);
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ZkServiceDiscoveryImpl.class).getExtension("zk");
    }

    @SneakyThrows
    private Channel doConnect(InetSocketAddress inetSocketAddress) {
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("Connection established successfully, address =  {}", inetSocketAddress.toString());
                completableFuture.complete(future.channel());
            } else {
                log.info("Connection established failed");
                throw new IllegalStateException();
            }

        });
        return completableFuture.get();
    }

    @Override
    public Object sendMessage(RpcRequest rpcRequest) {
        //??????????????????????????????
        CompletableFuture<RpcResponse<Object>> resultFuture = new CompletableFuture<>();
        //??????rpcServiceName???????????????????????????
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest);
        //??????????????????channel
        Channel channel = getChannel(inetSocketAddress);
        if (channel.isActive()) {
            //???future??????map????????????????????????????????????????????????????????????
            unprocessedFutureCache.set(rpcRequest.getRequestId(), resultFuture);
            RpcMessage rpcMessage = RpcMessage.builder()
                    .compress(CompressTypeEnum.GZIP.getCode())
                    .codec(SerializationEnum.KRYO.getCode())
                    .messageType(RpcConstant.REQUEST_TYPE)
                    .data(rpcRequest).build();
            channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.info("client send successful, {}", rpcMessage);
                } else {
                    future.channel().close();
                    resultFuture.completeExceptionally(future.cause());
                    log.info("client send failed, {}", future.cause());
                }
            });
        } else {
            throw new IllegalStateException();
        }
        return resultFuture;
    }

    public Channel getChannel(InetSocketAddress inetSocketAddress){
        Channel channel = channelProvider.get(inetSocketAddress);
        if (channel == null) {
            channel = doConnect(inetSocketAddress);
            channelProvider.set(inetSocketAddress, channel);
        }
        return channel;
    }
}
