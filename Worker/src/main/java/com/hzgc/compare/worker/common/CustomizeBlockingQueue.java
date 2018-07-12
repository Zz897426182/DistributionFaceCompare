package com.hzgc.compare.worker.common;

import java.util.LinkedList;
import java.util.List;

/**
 * 这是一个自定义的阻塞队列
 * @param <T>
 */
public class CustomizeBlockingQueue<T> {

    private LinkedList<T> list = new LinkedList <>();
    private LinkedList<T> outList = new LinkedList <>();
    private int size = 1000;

    /**
     * 将多个元素加入到阻塞队列中，阻塞队列大小无限制
     * @param list
     * @return
     */
    public synchronized boolean add(List<T> list){
        return this.list.addAll(list);
    }

    /**
     * 从阻塞队列中取出最多size个元素，若没有元素，则阻塞
     * @param size
     * @return
     */
    public synchronized List<T> pop(int size){
        while(this.list.size() == 0){
            try {
                wait();
            } catch (InterruptedException e) {

                e.printStackTrace();
            }
        }
        if (this.list.size() >= size){
            for (int i =0;i<size;i++){
                outList.add(list.get(i));
                list.remove(i);
            }
        }
        return outList;
    }
}
