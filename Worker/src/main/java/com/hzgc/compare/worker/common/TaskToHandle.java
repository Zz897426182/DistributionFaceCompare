package com.hzgc.compare.worker.common;

import java.util.ArrayList;
import java.util.List;

public class TaskToHandle {
    private static List<TaskToHandle> tasks;

    public static List<TaskToHandle> getTasks(){
        if(tasks == null){
            tasks = new ArrayList<TaskToHandle>();
        }
        return tasks;
    }

    public synchronized static void addTask(TaskToHandle handle){
        if(tasks == null){
            tasks = new ArrayList<TaskToHandle>();
        }
        tasks.add(handle);
    }

    public synchronized static void remove(TaskToHandle handle){
        if(tasks == null){
            tasks = new ArrayList<TaskToHandle>();
        }
        tasks.remove(handle);
    }

    public static class FlushTask extends TaskToHandle {
        private List<Quintuple<String, String, String, String, byte[]>> records;
        public FlushTask(List<Quintuple<String, String, String, String, byte[]>> records){
            this.records = records;
        }
    }


}
