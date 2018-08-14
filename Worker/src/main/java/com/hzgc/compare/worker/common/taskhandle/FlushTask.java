package com.hzgc.compare.worker.common.taskhandle;

import com.hzgc.compare.worker.common.tuple.Quintuple;

import java.util.List;

public class FlushTask<A1, A2, D> extends TaskToHandle {
        private List<Quintuple<A1, A2, String, String, D>> records;
        public FlushTask(List<Quintuple<A1, A2, String, String, D>> records){
            super();
            this.records = records;
        }

    public List<Quintuple<A1, A2, String, String, D>> getRecords() {
        return records;
    }
}