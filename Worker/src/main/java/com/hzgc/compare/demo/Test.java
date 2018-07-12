package com.hzgc.compare.demo;

import com.hzgc.compare.worker.util.FaceObjectUtil;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;


public class Test {
    public static void main(String args[]) throws IOException {
        Properties prop = new Properties();
        prop.put("bootstrap.servers", "172.18.18.100:9092,172.18.18.101:9092,172.18.18.102:9092");
        prop.put("kafka.retries", "0");
        prop.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        prop.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
//        prop.put("", "");
//        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(prop);


        Random ran = new Random();
        List<String> ipcIdList = new ArrayList<String>();
        for(int i = 0; i < 100 ; i ++){
            ipcIdList.add(i + "");
        }
        File file = new File("json.txt");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        List<String> list = new ArrayList<String>();
        while((line = reader.readLine()) != null){
            list.add(line.substring(line.indexOf("\"feature\"") + 10 , line.indexOf("\"hairColor\"") -1));
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<File> files = new ArrayList<File>();
        File file0 = new File("metadata" + File.separator + "metadata_0");
        files.add(file0);
        BufferedWriter bw = new BufferedWriter(new FileWriter(file0));
        int index = 1;
        int fileIndex = 1;
        for(int i = 1 ; i < 90 ; i ++){
            String date = sdf.format(new Date(System.currentTimeMillis() + i * 24 * 60 * 60 * 1000));
            for(int j = 0 ; j < 100001 ; j++){
                String ipcId = ipcIdList.get(ran.nextInt(100));
                String timeStamp = ",\"timeStamp\":\"2018-07-13 11:28:47\",\"date\":\"";
                String end = list.get(ran.nextInt(26));
                String data = "{\"ipcId\":\"" +ipcId+ "\"" + timeStamp + date + end;
                System.out.println(data);
                bw.write(ipcId + " " + date + " " + end + "\t\n");//"\t\n"
                index ++;
                if(index % 100 == 0){
                    bw.flush();
                    if(file0.length() > 128 * 1024 * 1024){
                        bw.close();
                        file0 = new File("metadata" + File.separator + "metadata_" + fileIndex);
                        bw = new BufferedWriter(new FileWriter(file0));
                        fileIndex ++;
                    }
                }
//                producer.send(new ProducerRecord<String, String>("feature", "1", data));

            }

        }

    }
}
