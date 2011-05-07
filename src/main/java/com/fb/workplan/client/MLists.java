package com.fb.workplan.client;

import java.util.ArrayList;
import java.util.List;

public class MLists {
    public static <T,K> List<K> transform(List<T> list, MFunction<T, K> func) {
        List<K> result = new ArrayList<K>(list.size());
        for(T child: list) {
            result.add(func.apply(child));
        }
        return result;
    }
}
