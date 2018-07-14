package com.hzgc.compare.worker.common;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class DoubleBufferQueue<T> {
    private List<T> writeList = new ArrayList<>();
    private ReentrantLock writeLock = new ReentrantLock();

    public void push(T value) {
        writeLock.lock();
        try {
            writeList.add(value);
        } finally {
            writeLock.unlock();
        }
    }

    public void push(List<T> values) {
        writeLock.lock();
        try {
            writeList.addAll(values);
        } finally {
            writeLock.unlock();
        }
    }

    public int getWriteListSize() {
        return writeList.size();
    }

    public List<T> get() {
        writeLock.lock();
        try {
            int currentSize = writeList.size();
            if (currentSize == 0) {
                return new ArrayList<>();
            }

            List<T> readList = writeList;
            writeList = new ArrayList<>();
            return readList;
        } finally {
            writeLock.unlock();
        }
    }

    public List<T> getWithoutRemove(){
        writeLock.lock();
        try {
            int currentSize = writeList.size();
            if (currentSize == 0) {
                return new ArrayList<>();
            }
            return writeList;
        } finally {
            writeLock.unlock();
        }
    }

}
