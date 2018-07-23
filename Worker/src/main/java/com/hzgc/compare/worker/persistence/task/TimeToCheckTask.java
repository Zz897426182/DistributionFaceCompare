package com.hzgc.compare.worker.persistence.task;

import com.hzgc.compare.worker.common.Quintuple;
import com.hzgc.compare.worker.common.taskhandle.FlushTask;
import com.hzgc.compare.worker.common.taskhandle.TaskToHandleQueue;
import com.hzgc.compare.worker.conf.Config;
import com.hzgc.compare.worker.persistence.LocalFileManager;

import java.util.List;
import java.util.TimerTask;

/**
 * 定期查看TaskToHandle中有无FlushTask，如果有，则flush其中的记录，并删除该FlushTask
 */
public class TimeToCheckTask extends TimerTask{
    private LocalFileManager manager;
    public TimeToCheckTask(LocalFileManager manager){
        this.manager = manager;
    }
    public void run() {
        FlushTask task = TaskToHandleQueue.getTaskQueue().getTask(FlushTask.class);
        if(task != null){
            List<Quintuple<String, String, String, String, byte[]>>data =  task.getRecords();
            manager.flush(data);
        }
    }
}
