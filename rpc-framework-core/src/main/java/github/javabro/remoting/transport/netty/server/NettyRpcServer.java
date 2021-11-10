package github.javabro.remoting.transport.netty.server;

import github.javabro.remoting.transport.netty.codec.RpcMessageDecoder;
import github.javabro.remoting.transport.netty.codec.RpcMessageEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: HeZhongPan
 * @Date: 2021/11/09/20:42
 * @Description:
 */
@Slf4j
public class NettyRpcServer {

    private static final int PORT = 9001;


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
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new RpcMessageDecoder());
                            pipeline.addLast(new RpcMessageEncoder());

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
