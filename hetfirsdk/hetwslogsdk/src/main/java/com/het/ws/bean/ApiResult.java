package com.het.ws.bean;

import java.io.Serializable;

public class ApiResult<T> implements Serializable {
    private int type;
    private T data;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ApiResult{" +
                "type=" + type +
                ", data=" + data +
                '}';
    }
}
