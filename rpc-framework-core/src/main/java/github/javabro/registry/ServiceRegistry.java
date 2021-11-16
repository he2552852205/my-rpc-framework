package github.javabro.registry;

import github.javabro.extension.SPI;

import java.net.InetSocketAddress;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: HeZhongPan
 * @Date: 2021/11/15/15:08
 * @Description: 服务注册
 */
@SPI
public interface ServiceRegistry {


    /**
     * 将服务注册到注册中心
     * @param serviceName 服务名称
     * @param inetSocketAddress 地址
     */
    void registry(String serviceName, InetSocketAddress inetSocketAddress);
}
