package com.hzgc.compare.worker;

import com.hzgc.compare.worker.common.FaceObject;
import com.hzgc.compare.worker.memory.cache.MemoryCacheImpl;
import com.hzgc.compare.worker.persistence.task.TimeToWrite;
import com.hzgc.compare.worker.util.FaceObjectUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class CreateRecordForCompared {
    public void createRecords(int days, int num) throws IOException {
        Random ran = new Random();
        List<String> ipcIdList = new ArrayList<String>();
        for(int i = 0; i < 100 ; i ++){
            ipcIdList.add(i + "");
        }
        File file = new File("src" + File.separator + "test" + File.separator + "java"
                + File.separator + "com" + File.separator + "hzgc" + File.separator + "compare"
                + File.separator + "worker" + File.separator + "json.txt");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        List<String> list = new ArrayList<String>();
        while((line = reader.readLine()) != null){
            list.add(line.substring(line.indexOf("\"timeSlot\"") - 2 , line.length()));
        }

        TimeToWrite task = new TimeToWrite();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//        List<File> files = new ArrayList<File>();
//        File file0 = new File("metadata" + File.separator + "metadata_0");
//        files.add(file0);
//        BufferedWriter bw = new BufferedWriter(new FileWriter(file0));
        for(int i = 0 ; i < days ; i ++){
            List<FaceObject> arr = new ArrayList<>();
            String date = sdf.format(new Date(System.currentTimeMillis() + i * 24 * 60 * 60 * 1000));
            for(int j = 0 ; j < num ; j++){
                String ipcId = ipcIdList.get(ran.nextInt(100));
                String timeStamp = ",\"timeStamp\":\"2018-07-13 11:28:47\",\"date\":\"";
                String end = list.get(ran.nextInt(26));
                String data = "{\"ipcId\":\"" +ipcId+ "\"" + timeStamp + date + end;
//                System.out.println(data);
//                bw.write(ipcId + " " + date + " " + );//"\t\n"
                FaceObject obj = FaceObjectUtil.jsonToObject(data);
                arr.add(obj);
            }
            MemoryCacheImpl cache = MemoryCacheImpl.<String, String, byte[]>getInstance();
            cache.addFaceObjects(arr);
            task.writeToHBase();
            cache.flush();
        }
    }
}
