package github.javabro.provider.impl;

import github.javabro.config.RpcServiceConfig;
import github.javabro.enums.RpcErrorMessageEnum;
import github.javabro.exception.RpcException;
import github.javabro.extension.ExtensionLoader;
import github.javabro.provider.ServiceProvider;
import github.javabro.registry.ServiceRegistry;
import github.javabro.remoting.transport.netty.server.NettyRpcServer;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: HeZhongPan
 * @Date: 2021/11/17/15:12
 * @Description:
 */
@Slf4j
public class ServiceProviderImpl implements ServiceProvider {

    private final Map<String, Object> serviceMap;
    private final Set<String> registeredService;
    private final ServiceRegistry serviceRegistry;

    public ServiceProviderImpl(){
        serviceMap = new ConcurrentHashMap<>();
        registeredService = ConcurrentHashMap.newKeySet();
        serviceRegistry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension("zk");
    }

    @Override
    public void addService(RpcServiceConfig rpcServiceConfig) {
        String serviceName = rpcServiceConfig.getRpcServiceName();
        if (registeredService.contains(serviceName)) {
            return ;
        }
        registeredService.add(serviceName);
        serviceMap.put(serviceName, rpcServiceConfig.getService());
        log.info("Add a service : {}, interface: {}",serviceName, rpcServiceConfig.getService().getClass().getInterfaces());
    }

    @Override
    public Object getService(String serviceName) {
        Object service = serviceMap.get(serviceName);
        if (null == service) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }

    @Override
    public void publishService(RpcServiceConfig rpcServiceConfig) {
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            this.addService(rpcServiceConfig);
            serviceRegistry.registry(rpcServiceConfig.getServiceName(), new InetSocketAddress(host, NettyRpcServer.PORT));
        } catch (UnknownHostException e) {
            log.error("occur exception when getHostAddress", e);
        }
    }
}
