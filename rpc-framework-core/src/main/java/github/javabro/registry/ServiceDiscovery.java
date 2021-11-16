package github.javabro.registry;

import github.javabro.extension.SPI;
import github.javabro.remoting.dto.RpcRequest;

import java.net.InetSocketAddress;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: HeZhongPan
 * @Date: 2021/11/15/15:08
 * @Description: 服务发现
 */
@SPI
public interface ServiceDiscovery {


    /**
     * 根据服务名查询服务地址
     * @param rpcRequest
     * @return
     */
    InetSocketAddress lookupService(RpcRequest rpcRequest);
}
