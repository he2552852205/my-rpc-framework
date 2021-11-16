package github.javabro.serialize;

import github.javabro.extension.SPI;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: HeZhongPan
 * @Date: 2021/11/09/7:48
 * @Description: 序列化接口
 */
@SPI
public interface Serializer {


    /**
     * 将对象序列化为字节数组
     * @param obj 要序列化的对象
     * @return 返回的字节数组
     */
    byte[] serialize(Object obj);


    /**
     * 将字节数组反序列化为对象
     * @param bytes 字节数组
     * @param clazz 目标类
     * @param <T>类的类型。举个例子,  {@code String.class} 的类型是 {@code Class<String>}.
     *        如果不知道类的类型的话，使用 {@code Class<?>}
     * @return 反序列化的对象
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
