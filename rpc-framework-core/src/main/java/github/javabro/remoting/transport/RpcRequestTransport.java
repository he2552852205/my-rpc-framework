package github.javabro.remoting.transport;

import github.javabro.extension.SPI;
import github.javabro.remoting.dto.RpcRequest;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: HeZhongPan
 * @Date: 2021/11/09/8:16
 * @Description:
 */
@SPI
public interface RpcRequestTransport {


    /**
     * 发送消息
     * @param rpcRequest
     * @return
     */
    Object sendMessage(RpcRequest rpcRequest);
}
