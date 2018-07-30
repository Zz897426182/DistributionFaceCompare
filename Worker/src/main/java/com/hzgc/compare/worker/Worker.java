package com.hzgc.compare.worker;


import com.hzgc.compare.rpc.server.RpcServer;
import com.hzgc.compare.rpc.server.zk.ServiceRegistry;
import com.hzgc.compare.worker.common.FaceInfoTable;
import com.hzgc.compare.worker.common.taskhandle.TaskToHandleQueue;
import com.hzgc.compare.worker.comsumer.Comsumer;
import com.hzgc.compare.worker.conf.Config;
import com.hzgc.compare.worker.memory.cache.MemoryCacheImpl;
import com.hzgc.compare.worker.memory.manager.MemoryManager;
import com.hzgc.compare.worker.persistence.FileManager;
import com.hzgc.compare.worker.persistence.FileReader;
import com.hzgc.compare.worker.persistence.HBaseClient;
import com.hzgc.compare.worker.persistence.LocalFileManager;
import com.hzgc.compare.worker.util.HBaseHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


/**
 * 整合所有组件
 */
public class Worker<A1, A2, D> {
    private String workId;
    private static final Logger logger = LoggerFactory.getLogger(Worker.class);
    private Config conf;
    private Comsumer comsumer;
    private MemoryManager memoryManager;
    private FileManager fileManager;
    private HBaseClient hBaseClient;

    private void init(){
        conf = Config.getConf();
        comsumer = new Comsumer();
        workId = conf.getValue(Config.WORKER_ID);
        logger.info("To start worker " + workId);
        logger.info("To init the memory module.");
        MemoryCacheImpl.<A1, A2, D>getInstance();
        memoryManager = new MemoryManager<A1, A2, D>();
        logger.info("To init persistence module.");
        if(Config.SAVE_TO_LOCAL == conf.getValue(Config.WORKER_FILE_SAVE_SYSTEM, 0)){
            fileManager = new LocalFileManager<A1, A2, D>();
        }
        hBaseClient = new HBaseClient();
        logger.info("Load data from file System.");
        FileReader fileReader = new FileReader();
        fileReader.loadRecordFromLocal();
        HBaseHelper.getTable(FaceInfoTable.TABLE_NAME);
        TaskToHandleQueue.getTaskQueue();
    }

    private void start(){
        comsumer.start();
        memoryManager.startToCheck();
        memoryManager.toShowMemory();
        if(conf.getValue(Config.WORKER_FLUSH_PROGRAM, 0) == 0){
            memoryManager.timeToCheckFlush();
        }
        fileManager.checkFile();
        fileManager.checkTaskTodo();
        hBaseClient.timeToWrite2();
        logger.info("Registry the service.");
        ServiceRegistry registry = new ServiceRegistry(conf.getValue(Config.ZOOKEEPER_ADDRESS));
        RpcServer rpcServer = new RpcServer(conf.getValue(Config.WORKER_ADDRESS),
                conf.getValue(Config.WORKER_RPC_PORT, 4086), registry);
        rpcServer.start();
    }

    void stop() {
    }

    public static void main(String args[]){
        Worker worker = new Worker<String, String, float[]>();
        worker.init();
        worker.start();
    }
}
