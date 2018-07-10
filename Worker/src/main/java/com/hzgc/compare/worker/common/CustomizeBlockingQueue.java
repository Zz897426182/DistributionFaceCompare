package com.hzgc.compare.worker.common;

import java.util.List;

/**
 * 这是一个自定义的阻塞队列
 * @param <T>
 */
public class CustomizeBlockingQueue<T> {

    /**
     * 将多个元素加入到阻塞队列中，阻塞队列大小无限制
     * @param list
     * @return
     */
    public boolean add(List<T> list){
        return false;
    }

    /**
     * 从阻塞队列中取出最多size个元素，若没有元素，则阻塞
     * @param size
     * @return
     */
    public List<T> pop(int size){
        return null;
    }
}
