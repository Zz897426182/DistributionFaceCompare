package com.hzgc.compare.worker.common.taskhandle;

import com.hzgc.compare.worker.common.Quintuple;

import java.util.List;

public class FlushTask extends TaskToHandle {
        private List<Quintuple<String, String, String, String, byte[]>> records;
        public FlushTask(List<Quintuple<String, String, String, String, byte[]>> records){
            super();
            this.records = records;
        }

    public List <Quintuple <String, String, String, String, byte[]>> getRecords() {
        return records;
    }
}