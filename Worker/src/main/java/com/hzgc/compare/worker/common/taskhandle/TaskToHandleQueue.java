package com.hzgc.compare.worker.common.taskhandle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class TaskToHandleQueue {
    private static TaskToHandleQueue queue;
    private ReentrantLock lock = new ReentrantLock();
    private List<TaskToHandle> tasks;

    public static TaskToHandleQueue getTaskQueue(){
        if(queue == null ){
            queue = new TaskToHandleQueue();
        }
        return queue;
    }
    private TaskToHandleQueue(){
        tasks = new ArrayList<>();
    }

    public void addTask(TaskToHandle handle){
        lock.lock();
        try {
            tasks.add(handle);
        } finally {
            lock.unlock();
        }
    }

    public <T> T getTask(Class<T> clazz){
        lock.lock();
        try {
            TaskToHandle res = null;
            for (TaskToHandle task : tasks) {
                if (task.getClass() == clazz) {
                    res = task;
                    tasks.remove(task);
                    break;
                }
            }
            if(res != null){
                return (T) res;
            }
            return  null;
        } finally {
            lock.unlock();
        }
    }
}
class TaskToHandle{

}
