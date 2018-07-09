package com.hzgc.compare.worker;


import com.hzgc.compare.rpc.server.connect.RpcServer;
import com.hzgc.compare.rpc.server.connect.ServiceRegistry;
import com.hzgc.compare.worker.comsumer.Comsumer;
import com.hzgc.compare.worker.conf.Config;
import com.hzgc.compare.worker.memory.cache.MemoryCacheImpl1;
import com.hzgc.compare.worker.memory.manager.MemoryManager;
import com.hzgc.compare.worker.persistence.FileManager;
import com.hzgc.compare.worker.persistence.FileReader;
import com.hzgc.compare.worker.persistence.HBaseClient;
import com.hzgc.compare.worker.persistence.LocalFileManager;
import com.hzgc.compare.worker.util.PropertiesUtil;
import com.hzgc.compare.worker.util.ZookeeperUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;


/**
 * 整合所有组件
 */
public class Worker {
    private static final Logger logger = LoggerFactory.getLogger(Worker.class);
    private Config conf;
    private Comsumer comsumer;
    private MemoryManager memoryManager;
    private FileManager fileManager;
    private HBaseClient hBaseClient;

    private void init(){
        Properties prop = PropertiesUtil.getProperties();
        conf = new Config(prop);
        ZookeeperUtil.registered();
        comsumer = new Comsumer(conf);
        MemoryCacheImpl1.getInstance(conf);
        memoryManager = new MemoryManager(conf);
        if(Config.SAVE_TO_LOCAL == conf.getValue(Config.WORKER_FILE_SAVE_SYSTEM, 0)){
            fileManager = new LocalFileManager(conf);
        }
        hBaseClient = new HBaseClient(conf);

        FileReader fileReader = new FileReader(conf);
        fileReader.loadRecord();
        logger.info("");
    }

    private void start(){
        comsumer.start();
        memoryManager.startToCheck();
        fileManager.checkFile();
        hBaseClient.timeToWrite();
        ServiceRegistry registry = new ServiceRegistry(conf.getValue(Config.ZOOKEEPER_ADDRESS));
        RpcServer rpcServer = new RpcServer(conf.getValue(Config.WORKER_ADDRESS),
                conf.getValue(Config.WORKER_RPC_PORT, 4086), registry);
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
