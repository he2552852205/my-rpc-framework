package github.javabro.serialize.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import github.javabro.exception.SerializeException;
import github.javabro.remoting.dto.RpcRequest;
import github.javabro.remoting.dto.RpcResponse;
import github.javabro.serialize.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: HeZhongPan
 * @Date: 2021/11/09/7:51
 * @Description: 使用Kryo序列化
 */
public class KryoSerializer implements Serializer {

    private static final ThreadLocal<Kryo> kryoLocal = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            kryo.register(RpcRequest.class);
            kryo.register(RpcResponse.class);
            return kryo;
        }
    };

    @Override
    public byte[] serialize(Object obj) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Output output = new Output(byteArrayOutputStream)) {
            Kryo kryo = kryoLocal.get();
            //obj -> byte[]
            kryo.writeObject(output, obj);
            kryoLocal.remove();
            return output.toBytes();
        } catch (Exception e) {
            throw new SerializeException("Serialization failed");
        }


    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                Input input = new Input(byteArrayInputStream)) {
            Kryo kryo = kryoLocal.get();
            Object o = kryo.readObject(input, clazz);
            kryoLocal.remove();
            return clazz.cast(o);
        } catch (Exception e) {
            throw new SerializeException("Serialization failed");
        }
    }
}
