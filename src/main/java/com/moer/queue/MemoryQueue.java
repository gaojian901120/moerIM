package com.moer.queue;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by gaoxuejian on 2018/5/2.
 */
public class MemoryQueue<T> {
    private ConcurrentMap<String, Queue<T>> map = new ConcurrentHashMap();

    private Queue getQ(String key) {
        if (!map.containsKey(key)) {
            Queue queue = new ConcurrentLinkedQueue();
            Queue oldQ = map.putIfAbsent(key, queue);
            if (oldQ != null) {
                queue = oldQ;
            }
            return queue;
        } else return map.get(key);
    }

    public <T> boolean push(String key, T o) {
        Queue queue = getQ(key);
        return queue.add(o);
    }

    public T pop(String key) {
        Queue queue = getQ(key);
        return (T) queue.poll();
    }

    public int len(String key) {
        Queue queue = getQ(key);
        return queue.size();
    }
}
