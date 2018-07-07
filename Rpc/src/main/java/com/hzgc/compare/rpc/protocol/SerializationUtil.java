package com.hzgc.compare.rpc.protocol;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import net.sf.cglib.reflect.FastClass;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 序列化反序列化对象，使用了Objenesis，即java字节码技术，可以在不使用
 * 构造器的情况下初始化一个对象
 */
public class SerializationUtil {
    private static Map<Class<?>, Schema<?>> classSchema = new ConcurrentHashMap<>();
    private static Objenesis objenesis = new ObjenesisStd(true);

    private SerializationUtil() {
    }

    @SuppressWarnings("unchecked")
    private static <T> Schema<T> getSchema(Class<T> clz) {
        Schema<T> schema = (Schema<T>) classSchema.get(clz);
        if (schema == null) {
            schema = RuntimeSchema.createFrom(clz);
            classSchema.put(clz, schema);
        }
        return schema;
    }

    /**
     *  序列化（对象 -> 字节数组）
     */
    @SuppressWarnings("unchecked")
    static <T> byte[] serialize(T obj) {
        Class<T> clz = (Class<T>) obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema<T> schema = getSchema(clz);
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            buffer.clear();
        }
    }

    /**
     * 反序列化（字节数组 -> 对象）
     */
    @SuppressWarnings("unchecked")
    static <T> T deserialize(byte[] data, Class<T> clz) {
        try {
            T message = objenesis.newInstance(clz);
            Schema<T> schema = getSchema(clz);
            ProtostuffIOUtil.mergeFrom(data, message, schema);
            return message;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
