package com.hzgc.compare.worker;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CreateRecordToKafka {
    public static void createRecords(int days, int num) throws IOException {
        Properties prop = new Properties();
        prop.put("bootstrap.servers", "172.18.18.100:9092,172.18.18.101:9092,172.18.18.102:9092");
        prop.put("kafka.retries", "0");
        prop.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        prop.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
//        prop.put("", "");
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(prop);


        Random ran = new Random();
        List<String> ipcIdList = new ArrayList<String>();
        for(int i = 0; i < 100 ; i ++){
            ipcIdList.add(i + "");
        }
        File file = new File("E:\\hzgc\\DistributionFaceCompare5\\Worker\\src\\test\\java\\com\\hzgc\\compare\\worker\\json.txt");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        List<String> list = new ArrayList<String>();
        while((line = reader.readLine()) != null){
            list.add(line.substring(line.indexOf("\"timeSlot\"") - 2 , line.length()));
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        List<File> files = new ArrayList<File>();
//        File file0 = new File("metadata" + File.separator + "metadata_0");
//        files.add(file0);
//        BufferedWriter bw = new BufferedWriter(new FileWriter(file0));
        for(long i = 0L ; i < days ; i ++){
            String date = sdf.format(new Date(System.currentTimeMillis() + i * 24 * 60 * 60 * 1000));
            for(int j = 0 ; j < num ; j++){
                String ipcId = ipcIdList.get(ran.nextInt(100));
                String timeStamp = ",\"timeStamp\":\"2018-07-13 11:28:47\",\"date\":\"";
                String end = list.get(ran.nextInt(26));
                String data = "{\"ipcId\":\"" +ipcId+ "\"" + timeStamp + date + end;
//                System.out.println(data);
//                bw.write(ipcId + " " + date + " " + );//"\t\n"
                producer.send(new ProducerRecord<>("feature", "1", data));

            }
        }

    }
}