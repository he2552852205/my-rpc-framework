package github.javabro.remoting.transport.netty.client;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: HeZhongPan
 * @Date: 2021/11/10/9:40
 * @Description:
 */
public class ChannelProvider {

    private static Map<String, Channel> map;

    public ChannelProvider(){
        map = new ConcurrentHashMap<>();
    }

    public Channel get(InetSocketAddress inetSocketAddress) {
        String key = inetSocketAddress.toString();
        if (map.containsKey(key)) {
            Channel channel = map.get(key);
            if (channel != null && channel.isActive()) {
                return channel;
            } else {
                map.remove(key);
            }
        }
        return null;
    }

    public void set(InetSocketAddress inetSocketAddress, Channel channel) {
        String key = inetSocketAddress.toString();
        map.put(key, channel);
    }
}
