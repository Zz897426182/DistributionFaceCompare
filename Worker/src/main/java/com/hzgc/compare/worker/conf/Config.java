package com.hzgc.compare.worker.conf;

import com.hzgc.compare.worker.util.PropertiesUtil;
import org.apache.commons.lang.StringUtils;

import java.util.Properties;

public class Config {
    private static Config conf;
    private Properties ps;
    public static final int ACTION = 1;
    public static final int SAVE_TO_LOCAL = 0;
    public static final int SAVE_TO_HDFS = 1;

    public static final String WORKER_BUFFER_SIZE_MAX = "worker.buffer.size.max"; //内存中buffer的阈值
    public static final String WORKER_CACHE_SIZE_MAX = "worker.cach.size.max"; //内存中缓存数据的最大值
    public static final String WORKER_MEMORY_CHECK_TIME = "worker.memory.check.time"; //内存数据的检查时间间隔
    public static final String WORKER_RECORD_TIME_OUT = "work.record.time.out"; //内存中记录的过期时间
    public static final String WORKER_CHECK_TASK_TIME = "work.check_task.time"; //检查任务列表的时间间隔
    public static final String WORKER_FILE_TIME_OUT = "worker.file.time.out"; //文件的过期时间
    public static final String WORKER_FILE_CHECK_TIME = "worker.file.check.time"; //文件检查时间间隔
    public static final String WORKER_HBASE_WRITE_TIME = "worker.hbase.write.time"; //写HBase任务的时间间隔
    public static final String WORKER_FILE_SAVE_PROGRAM = "worker.file.save.program"; //数据持久化方案（一个文件保存几天的数据）
    public static final String WORKER_FILE_PATH = "worker.file.path"; //文件保存路径
    public static final String WORKER_LOAD_DATA_DAYS = "worker.load.data.days"; //项目启动时，加载多少天的数据到内存中
    public static final String WORKER_STREAM_TIME_OUT = "worker.stream.time.out"; //文件流过期时间
    public static final String WORKER_PORT = "worker.port"; //worker绑定本地端口
    public static final String WORKER_FILE_SAVE_SYSTEM = "worker.file.save.system"; //数据持久化的文件系统 0 本地  1 HDFS
    public static final String WORKER_FLUSH_PROGRAM = "worker.flush.program"; //持久化触发方式 0 定期触发  1定量触发
    public static final String KAFKA_TOPIC = "kafka.topic";
    public static final String KAFKA_BOOTSTRAP_SERVERS = "kafka.bootstrap.servers";
    public static final String KAFKA_GROUP_ID = "kafka.group.id";
    public static final String KAFKA_MAXIMUM_TIME = "kafka.maximum.time";
    public static final String ZOOKEEPER_ADDRESS = "zookeeper.address";
    public static final String WORKER_ADDRESS = "worker.address";
    public static final String WORKER_RPC_PORT = "worker.rpc.port";
    public static final String WORKER_ID = "worker.id";
    public static final String ROOT_PATH = "root.path";
    public static final String DELETE_OPEN = "delete.open";

    public static Config getConf(Properties ps) {
        if(conf == null){
            conf = new Config(ps);
        }
        return conf;
    }

    public static Config getConf(){
        if(conf == null){
            Properties prop = PropertiesUtil.getProperties();
            conf = new Config(prop);
        }
        return conf;
    }

    private Config(Properties ps){
        this.ps = ps;
    }

    public String getValue(String name) {
        return ps.getProperty(name);
    }

    public String getValue(String name, String value){
        String res = ps.getProperty(name);
        if(StringUtils.isBlank(res)){
            return value;
        }
        return res;
    }

    public Long getValue(String name, Long value){
        String res = ps.getProperty(name);
        if(StringUtils.isBlank(res)){
            return value;
        }
        return Long.parseLong(res);
    }

    public Integer getValue(String name, Integer value){
        String res = ps.getProperty(name);
        if(StringUtils.isBlank(res)){
            return value;
        }
        return Integer.parseInt(res);
    }

    public void setValue(String name, String value){
        ps.put(name, value);
    }
}
