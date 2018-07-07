package com.hzgc.compare.rpc.protocol;

import java.io.Serializable;

/**
 * 基础消息类,所有主站或者入站消息对象都需要继承此对象
 * 此处的MsgType是一个枚举类型，继承BaseMsg的子类可以通过
 * 枚举类型来区分业务类型从而做出不同的操作
 */
public class BaseMsg implements Serializable {
    private static final long serialVersionUID = 1L;
    private MsgType type;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public MsgType getType() {
        return type;
    }

    public void setType(MsgType type) {
        this.type = type;
    }
}
