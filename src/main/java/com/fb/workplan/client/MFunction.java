package com.fb.workplan.client;

public interface MFunction<T, K> {
    K apply(T obj);
}
