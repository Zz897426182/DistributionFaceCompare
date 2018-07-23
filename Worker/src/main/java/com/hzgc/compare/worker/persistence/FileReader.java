package com.hzgc.compare.worker.persistence;


import com.hzgc.compare.worker.common.Triplet;
import com.hzgc.compare.worker.conf.Config;
import com.hzgc.compare.worker.memory.cache.MemoryCacheImpl1;
import javafx.util.Pair;
import org.apache.log4j.Logger;
import sun.misc.BASE64Decoder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 */
public class FileReader {
    private Config conf;
    private String path;
    private static Logger LOG = Logger.getLogger(FileReader.class);

    public FileReader() {
        this.conf = Config.getConf();
    }

    public void init(){
        path = conf.getValue(Config.ROOT_PATH);
    }

    /**
     * 项目启动时，从本地文件中加载数据到内存
     */
    public void loadRecord() {
        init();
        BASE64Decoder decoder = new BASE64Decoder();
        LocalStreamCache streamCache = LocalStreamCache.getInstance();
        MemoryCacheImpl1 memoryCacheImpl1 = MemoryCacheImpl1.getInstance();
        Map <Triplet <String, String, String>, List <Pair <String, byte[]>>> cacheRecords =
                memoryCacheImpl1.getCacheRecords();
        String workId = conf.getValue(Config.WORKER_ID);
        File workFile = new File(path);
        File[] listFiles = workFile.listFiles();
        if (listFiles.length > 0) {
            //work_id文件目录
            for (File fi : listFiles) {
                if (fi.isDirectory()) {
                    String dirName = fi.getName();
                    if (workId.equals(dirName)) {
                        Date date = new Date();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
                        String ym = sdf.format(date);
                        String [] strings = ym.split("-");
                        Integer m = Integer.valueOf(strings[1]) - 1;
                        String lastMonth = null;
                        if (m > 0 && m < 10){
                            lastMonth = strings[0] + "-0" + m;
                        }
                        if (m == 0) {
                            int year = Integer.valueOf(strings[0]) - 1;
                            lastMonth = String.valueOf(year) + "-" + String.valueOf(12);
                        }
                        File[] files = fi.listFiles();
                        if (files.length > 0) {
                            //年-月份文件目录
                            for (File file : files) {
                                //读取上月文件
                                if (file.getName().equals(lastMonth)) {
                                    LOG.info("LastMonth data is " + lastMonth);
                                    File[] files1 = file.listFiles();
                                    //block文件
                                    if (files1.length > 0) {
                                        for (File f : files1) {
                                            if (f.isFile()) {
                                                System.out.println(f.getName());
                                                BufferedReader bufferedReader = streamCache.getReaderStream(f);
                                                try {
                                                    String line;
                                                    //数据封装
                                                    while ((line = bufferedReader.readLine()) != null) {
                                                        System.out.println(line);
                                                        String[] s = line.split("_");
                                                        Triplet <String, String, String> triplet = new Triplet <>(s[0], null, s[1]);
                                                        byte[] bytes = decoder.decodeBuffer(s[3]);
                                                        Pair <String, byte[]> pair = new Pair <>(s[2], bytes);
                                                        ArrayList <Pair <String, byte[]>> li = new ArrayList <>();
                                                        li.add(pair);
                                                        cacheRecords.put(triplet, li);
                                                    }
                                                    bufferedReader.close();
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    }
                                }
                                //读取本月文件
                                if (file.getName().equals(ym)) {
                                    LOG.info("NowDate data is " + ym);
                                    File[] files1 = file.listFiles();
                                    if (files1.length > 0) {
                                        //block文件
                                        for (File f : files1) {
                                            if (f.isFile()) {
                                                BufferedReader bufferedReader = streamCache.getReaderStream(f);
                                                try {
                                                    String line;
                                                    //数据封装
                                                    while ((line = bufferedReader.readLine()) != null) {
                                                        String[] s = line.split("_");
                                                        Triplet <String, String, String> triplet = new Triplet <>(s[0], null, s[1]);
                                                        byte [] bytes = decoder.decodeBuffer(s[3]);
                                                        Pair <String, byte[]> pair = new Pair <>(s[2], bytes);
                                                        ArrayList <Pair <String, byte[]>> li = new ArrayList <>();
                                                        li.add(pair);
                                                        cacheRecords.put(triplet, li);
                                                    }
                                                    bufferedReader.close();
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    public void loadRecordFromHDFS(){

    }
}
