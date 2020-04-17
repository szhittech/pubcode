package com.het.ws.event;

public interface IEvent<T> {
    void onEvent(T event);
}
