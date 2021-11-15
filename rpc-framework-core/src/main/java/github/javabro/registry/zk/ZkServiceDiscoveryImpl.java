package github.javabro.registry.zk;

import github.javabro.registry.ServiceDiscovery;
import github.javabro.registry.zk.utils.CuratorUtil;
import github.javabro.remoting.dto.RpcRequest;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.util.CollectionUtils;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: HeZhongPan
 * @Date: 2021/11/15/15:42
 * @Description:
 */
public class ZkServiceDiscoveryImpl implements ServiceDiscovery {

    @Override
    public InetSocketAddress lookupService(RpcRequest rpcRequest) {
        //根据请求获取到服务名
        String serviceName = rpcRequest.getRpcServiceName();
        CuratorFramework zkClient = CuratorUtil.getZkClient();
        //通过注册注册中心获取到对应服务的地址
        List<String> urls = CuratorUtil.getChildrenNode(zkClient, serviceName);
        if (urls == null || CollectionUtils.isEmpty(urls)) {
            throw new RuntimeException("The service is not registered");
        }
        //TODO:负载均衡

        String address = urls.get(0);
        String[] hostAndPort = address.split(":");
        String host = hostAndPort[0];
        int port = Integer.parseInt(hostAndPort[1]);
        return new InetSocketAddress(host, port);
    }
}
