package github.javabro.provider;

import github.javabro.config.RpcServiceConfig;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: HeZhongPan
 * @Date: 2021/11/17/15:12
 * @Description:
 */
public interface ServiceProvider {


    void addService(RpcServiceConfig rpcServiceConfig);

    Object getService(String serviceName);

    void publishService(RpcServiceConfig rpcServiceConfig);
}
