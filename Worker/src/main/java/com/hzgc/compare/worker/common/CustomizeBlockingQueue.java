package com.hzgc.compare.worker.common;

import java.util.LinkedList;
import java.util.List;

/**
 * 这是一个自定义的阻塞队列
 * @param <T>
 */
public class CustomizeBlockingQueue<T> {

    private LinkedList<T> list = new LinkedList <>();
    private int size = 1000;

    /**
     * 将多个元素加入到阻塞队列中，阻塞队列大小无限制
     * @param list
     * @return
     */
    public synchronized void add(List<T> list){
//        System.out.println("Push some record in to the Queue !");
        this.list.addAll(list);
        notify();
    }

    /**
     * 从阻塞队列中取出最多size个元素，若没有元素，则阻塞
     * @param size
     * @return
     */
    public synchronized List<T> pop(int size){
        while(this.list.size() == 0){
            try {
                System.out.println("There is no data in the Queue !");
                wait();
            } catch (InterruptedException e) {

                e.printStackTrace();
            }
        }
        if (this.list.size() >= size){
            System.out.println("The num of record in The Queue  is " + list.size());
            List<T> tempList = list.subList(0, size - 1);
            for (int i = 0; i  < size - 1; i++) {
                list.remove(0);
            }
            return tempList;
        }
        return new LinkedList<>();
    }

//    public static void main(String args[]){
//        CustomizeBlockingQueue<String> queue = new CustomizeBlockingQueue<>();
//        queue.add()
//    }
}
