package com.hzgc.compare.worker.common.taskhandle;

import com.hzgc.compare.worker.common.Quintuple;

import java.util.List;

public class FlushTask<A1, A2, D> extends TaskToHandle {
        private List<Quintuple<A1, A2, String, String, D>> records;
        public FlushTask(List<Quintuple<A1, A2, String, String, D>> records){
            super();
            this.records = records;
        }

<<<<<<< HEAD
    public List <Quintuple <String, String, String, String, byte[]>> getRecords() {
=======
    public List<Quintuple<A1, A2, String, String, D>> getRecords() {
>>>>>>> 427c0b2d9c9db0bfb5bcb577563534bc75789439
        return records;
    }
}