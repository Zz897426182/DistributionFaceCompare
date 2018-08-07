package com.hzgc.compare.worker.common.collects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class BatchBufferQueue<T> {
    private List<T[]> list = new ArrayList<>();
    private ReentrantLock writeLock = new ReentrantLock();
    private int size = 1000;
    private int pushN = 0;

    public BatchBufferQueue(){
        list.add((T[]) new Object[size]);
    }

    public List<T> get(){
        writeLock.lock();
        try {
            T[] temp = list.remove(0);
            if(list.size() == 0){
                pushN = 0;
                list.add((T[]) new Object[size]);
            }
            List res = Arrays.asList(temp);

            return (List<T>) res.stream().filter(r -> r != null).collect(Collectors.<T>toList());
        } finally {
            writeLock.unlock();
        }


    }

    public void push(List<T> records){
        writeLock.lock();
        try {
            T[] arrayToPush = list.get(list.size() -1);
            if(pushN >= size){
                arrayToPush = (T[]) new Object[size];
                list.add(arrayToPush);
                pushN = 0;
            }
            T[] arrayTemp = (T[]) records.toArray();
            int off = 0;
            int len = records.size();
            while (off <= len - 1){
                int pushSize = size - pushN;
                pushSize = Math.min(pushSize, len - off);
                System.arraycopy(arrayTemp, off, arrayToPush, pushN, pushSize);
                off += pushSize;
                pushN += pushSize;
                if(pushN >= size){
                    arrayToPush = (T[]) new Object[size];
                    list.add(arrayToPush);
                    pushN = 0;
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

    public int size(){
        writeLock.lock();
        try {
            T[] temp = list.get(list.size() -1);
            int count = 0;
            for(T t : temp){
                if(t != null){
                    count ++;
                }
            }
            return (list.size() - 1) * size + count;
        } finally {
            writeLock.unlock();
        }
    }
}
