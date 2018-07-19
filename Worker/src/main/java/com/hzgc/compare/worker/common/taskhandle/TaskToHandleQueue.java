package com.hzgc.compare.worker.common.taskhandle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class TaskToHandleQueue {
    private static final Logger logger = LoggerFactory.getLogger(TaskToHandleQueue.class);
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
        logger.info("Add a task to the queue, task class is " + handle.getClass().getName());
        try {
            tasks.add(handle);
        } finally {
            lock.unlock();
        }
    }

    public <T> T getTask(Class<T> clazz){
        lock.lock();
        logger.info("Get a task from the queue, task class is " + clazz.getName());
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
