package com.hzgc.compare.worker.comsumer;



import com.hzgc.compare.worker.common.FaceObject;
import com.hzgc.compare.worker.conf.Config;
import com.hzgc.compare.worker.memory.cache.MemoryCacheImpl1;
import com.hzgc.compare.worker.util.FaceObjectUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class Comsumer extends Thread{
    private static Logger LOG = Logger.getLogger(Comsumer.class);
    private MemoryCacheImpl1 memoryCache;
    private Config conf;
    private KafkaConsumer<String, String> comsumer;

    public Comsumer(){
        init();
    }

    /**
     * 初始化
     */
    private void init(){
        conf = Config.getConf();
        Properties prop = new Properties();
        prop.put("bootstrap.servers", conf.getValue(Config.KAFKA_BOOTSTRAP_SERVERS));
        prop.put("group.id", conf.getValue(Config.KAFKA_GROUP_ID));
        prop.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        prop.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        comsumer = new KafkaConsumer<String, String>(prop);
        LOG.info("Kafka comsumer is init.");
        memoryCache = MemoryCacheImpl1.getInstance(conf);
    }
    /**
     * 接收从kafka传来的数据
     */
    private void receiveAndSave(){
        comsumer.subscribe(Arrays.asList(conf.getValue(Config.KAFKA_TOPIC)));
        LOG.info("Comsumer is started to accept kafka info.");
        while(true){
            ConsumerRecords<String, String> records =
                    comsumer.poll(Long.parseLong(conf.getValue(Config.KAFKA_MAXIMUM_TIME)));
            List<FaceObject> objList = new ArrayList<FaceObject>();
            for(ConsumerRecord<String, String> record : records){
                FaceObject obj = FaceObjectUtil.jsonToObject(record.value());
//                System.out.println(record.value());
//                LOG.info(record.value());
                objList.add(obj);
            }
            memoryCache.recordToHBase(objList);
        }
    }

    public void run() {
        receiveAndSave();
    }

    public void stopKafkaClient(){
        comsumer.close();
    }
}
