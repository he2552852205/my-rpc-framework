package github.javabro.remoting.transport.netty.server;

import github.javabro.config.RpcServiceConfig;
import github.javabro.factory.SingletonFactory;
import github.javabro.provider.ServiceProvider;
import github.javabro.provider.impl.ServiceProviderImpl;
import github.javabro.remoting.transport.netty.codec.RpcMessageDecoder;
import github.javabro.remoting.transport.netty.codec.RpcMessageEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: HeZhongPan
 * @Date: 2021/11/09/20:42
 * @Description:
 */
@Slf4j
public class NettyRpcServer {

    public static final int PORT = 9001;
    private final ServiceProvider serviceProvider = SingletonFactory.getSingleton(ServiceProviderImpl.class);

    public void registerService (RpcServiceConfig rpcServiceConfig) {
        serviceProvider.publishService(rpcServiceConfig);
    }


    @SneakyThrows
    public void start() {
        NioEventLoopGroup boss = null;
        NioEventLoopGroup worker = null;
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            boss = new NioEventLoopGroup();
            worker = new NioEventLoopGroup(2);
            String host = InetAddress.getLocalHost().toString();

            bootstrap
                    .group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    //是否开启nagle算法
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    //是否开启心跳机制
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    //表示系统用于临时存放已完成三次握手的请求的队列的最大长度,如果连接建立频繁，服务器处理创建新连接较慢，可以适当调大这个参数
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            //30s未接到客户端的请求，自动解除连接
                            pipeline.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
                            pipeline.addLast(new RpcMessageDecoder());
                            pipeline.addLast(new RpcMessageEncoder());
                            pipeline.addLast(new RpcServerHandler());

                        }
                    });
            ChannelFuture future = bootstrap.bind(host, PORT).sync();
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("server occur a exception: {}", e);
        } finally {
            log.info("server shutdown");
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }

    }
}
