package com.hzgc.compare.rpc.protocol;

import java.io.Serializable;

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
