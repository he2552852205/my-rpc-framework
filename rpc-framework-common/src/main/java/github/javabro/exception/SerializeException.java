package github.javabro.exception;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: HeZhongPan
 * @Date: 2021/11/09/8:06
 * @Description: 序列化异常类
 */
public class SerializeException extends RuntimeException{

    public SerializeException(String message) {
        super(message);
    }
}
