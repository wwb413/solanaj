package org.p2p.solanaj.utils;

import cn.hutool.core.util.ArrayUtil;
import com.syntifi.near.borshj.BorshInput;
import com.syntifi.near.borshj.annotation.BorshField;
import com.syntifi.near.borshj.comparator.FieldComparator;
import com.syntifi.near.borshj.util.BorshUtil;
import org.bitcoinj.core.Base58;
import org.p2p.solanaj.Test;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

import static java.util.Objects.requireNonNull;

public class DecodeUtil {

    private static  <T> T read(final Class<T> clazz,DataInputStream dis) throws Exception{
        requireNonNull(clazz);
        if (clazz == Long.class || clazz == long.class) {
            return (T) Long.valueOf(readU64(dis));
        }else if (clazz == String.class) {
            return (T) readString(dis);
        }else if (clazz == Byte.class || clazz == byte.class) {
            return (T) Byte.valueOf(readU8(dis));
        }else if (clazz == Boolean.class || clazz == boolean.class) {
            return (T) Boolean.valueOf(readU8(dis) != 0);
        }
        throw new IllegalArgumentException();
    }

    private static byte readU8(DataInputStream dis) throws Exception{
        return dis.readByte();
    }


    public static long readU64(DataInputStream dis) throws Exception {
        byte[] longBytes = new byte[8];
        dis.readFully(longBytes);
        return ByteBuffer.wrap(longBytes).order(ByteOrder.LITTLE_ENDIAN).getLong();
    }

    private static String readString(DataInputStream dis) throws IOException {
        byte[] strBytes = new byte[32];
        dis.readFully(strBytes);
        return Base58.encode(strBytes);
    }

    public static Field[] getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
        }
        return fields.toArray(new Field[0]);
    }

    public static <T> T decodeAdvanced(byte[] decodedBytes,final Class<T> clazz) throws Exception {


        // 2. 使用DataInputStream进行精确解析
        ByteArrayInputStream bais = new ByteArrayInputStream(decodedBytes);
        DataInputStream dis = new DataInputStream(bais);

        if (clazz == Long.class || clazz == long.class) {
            return (T) Long.valueOf(readU64(dis));
        }

        final T object = clazz.getConstructor().newInstance();
        Field[] allFields = getAllFields(object.getClass());
        for (final Field field : allFields) {
            field.setAccessible(true);
            field.set(object, read(field.getType(),dis));
        }

        return object;
    }
}
