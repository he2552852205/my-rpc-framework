package github.javabro.enums;

import lombok.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: HeZhongPan
 * @Date: 2021/11/10/14:18
 * @Description:
 */
@AllArgsConstructor
@Getter
@ToString
public enum SerializationEnum {

    KRYO((byte)0x01, "kryo");

    private final byte code;
    private final String name;

    public static String getName(byte code) {

        for (SerializationEnum value : SerializationEnum.values()) {
            if (value.code == code) {
                return value.name;
            }
        }
        return null;
    }
}
