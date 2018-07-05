package com.hzgc.compare.worker;


import com.hzgc.compare.rpc.server.connect.RpcServer;
import com.hzgc.compare.rpc.server.connect.ServiceRegistry;
import com.hzgc.compare.worker.common.TaskToHandle;
import com.hzgc.compare.worker.comsumer.Comsumer;
import com.hzgc.compare.worker.conf.Config;
import com.hzgc.compare.worker.memory.cache.MemoryCache;
import com.hzgc.compare.worker.memory.manager.MemoryManager;
import com.hzgc.compare.worker.persistence.FileManager;
import com.hzgc.compare.worker.persistence.FileReader;
import com.hzgc.compare.worker.persistence.HBaseClient;


/**
 * 整合所有组件
 */
public class Worker {
    private Config conf;

    private Comsumer comsumer;
    private MemoryCache memoryCache;
    private MemoryManager memoryManager;
    private FileManager fileManager;
    private FileReader fileReader;
    private HBaseClient hBaseClient;
    private TaskToHandle taskToHandle;
    private ServiceImpl service;

    private void init(){

    }

    private void start(){

        ServiceRegistry registry = new ServiceRegistry("172.18.18.105");
        RpcServer rpcServer = new RpcServer("172.18.18.146", 4086, registry);
        rpcServer.start();
    }

    void stop() {
    }

    public Config getConf(){
        return conf;
    }

    public static void main(String args[]){
        Worker worker = new Worker();
        worker.init();
        worker.start();
        WorkerAccept workerAccept = new WorkerAccept(worker);
        workerAccept.accept();
    }
}
