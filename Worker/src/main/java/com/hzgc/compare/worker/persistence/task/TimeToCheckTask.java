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
public class TimeToCheckTask<A1, A2, D> extends TimerTask{
    private LocalFileManager<A1, A2, D> manager;
    public TimeToCheckTask(LocalFileManager<A1, A2, D> manager){
        this.manager = manager;
    }
    public void run() {
        FlushTask task = TaskToHandleQueue.getTaskQueue().getTask(FlushTask.class);
        if(task != null){
            List<Quintuple<A1, A2, String, String, D>>data =  task.getRecords();
            manager.flush(data);
        }
    }
}
