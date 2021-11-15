package github.javabro.registry.zk;

import github.javabro.registry.ServiceRegistry;
import github.javabro.registry.zk.utils.CuratorUtil;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: HeZhongPan
 * @Date: 2021/11/15/15:42
 * @Description:
 */
public class ZkServiceRegistryImpl implements ServiceRegistry {

    @Override
    public void registry(String serviceName, InetSocketAddress inetSocketAddress) {
        String path = CuratorUtil.ZK_ROOT_PATH + "/" + serviceName + inetSocketAddress.toString();
        CuratorFramework zkClient = CuratorUtil.getZkClient();
        CuratorUtil.createPersistentNode(zkClient, path);
    }
}
