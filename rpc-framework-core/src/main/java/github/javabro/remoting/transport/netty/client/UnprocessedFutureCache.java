package github.javabro.remoting.transport.netty.client;


import github.javabro.remoting.dto.RpcResponse;
import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: HeZhongPan
 * @Date: 2021/11/10/15:21
 * @Description: 存放一些未处理的任务
 */
public class UnprocessedFutureCache {

    private static Map<String, CompletableFuture<RpcResponse<Object>>> futureMap;

    public UnprocessedFutureCache(){
        futureMap = new ConcurrentHashMap<>();
    }

    public void set(String requestId, CompletableFuture<RpcResponse<Object>> future) {
        futureMap.put(requestId, future);
    }

    public void complete(RpcResponse<Object> rpcResponse) {
        CompletableFuture<RpcResponse<Object>> future = futureMap.remove(rpcResponse.getRequestId());
        if (future != null) {
            future.complete(rpcResponse);
        } else {
            throw new IllegalStateException();
        }
    }
}
