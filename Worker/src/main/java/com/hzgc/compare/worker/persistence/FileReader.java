package com.hzgc.compare.worker.persistence;


import com.hzgc.compare.worker.common.Triplet;
import com.hzgc.compare.worker.conf.Config;
import com.hzgc.compare.worker.memory.cache.MemoryCacheImpl;
import com.hzgc.compare.worker.util.FaceObjectUtil;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(MemoryCacheImpl.class);
    private Config conf;
    private String path;
    private BASE64Decoder decoder = new BASE64Decoder();
    private LocalStreamCache streamCache = LocalStreamCache.getInstance();

    public FileReader() {
        this.conf = Config.getConf();
        init();
    }

    public void init(){
        path = conf.getValue(Config.WORKER_FILE_PATH);
    }

    /**
     * 项目启动时，从本地文件中加载数据到内存
     */
    public void loadRecordFromLocal() {
        String workId = conf.getValue(Config.WORKER_ID);
        File workFile = new File(path);
        if(!workFile.isDirectory()){
            return;
        }
        File[] listFiles = workFile.listFiles();
        // 得到当前worker的目录
        File dirForThisWorker = null;
        if (listFiles != null && listFiles.length > 0) {
            for(File fi : listFiles){
                if(fi.isDirectory() && workId.equals(fi.getName())){
                    dirForThisWorker = fi;
                }
            }
        }
        if(dirForThisWorker == null || !dirForThisWorker.isDirectory()){
            return;
        }
        //得到本月和上月
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
        long start = System.currentTimeMillis();
        // 加载上月的记录
        loadRecordForMonth2(dirForThisWorker, lastMonth);
        // 加载本月的记录
        loadRecordForMonth2(dirForThisWorker, ym);
        logger.info("The time used to load record is : " + (System.currentTimeMillis() - start));
    }

    /**
     * 加载一个月的数据到内存（内存存储byte特征值）
     * @param fi 目录
     * @param month 目标月份
     */
    private void loadRecordForMonth(File fi, String month){
        logger.info("Read month is : " + month);
        MemoryCacheImpl<String, String, byte[]> memoryCacheImpl1 = MemoryCacheImpl.getInstance();
        //得到目标月份的文件夹
        File monthdir = null;
        File[] files = fi.listFiles();
        if(files != null && files.length > 0){
            for(File file : files){
                if (file.isDirectory() && file.getName().equals(month)){
                    monthdir = file;
                }
            }
        }
        if(monthdir == null){
            return;
        }
        //遍历加载数据文件
        File[] files1 = monthdir.listFiles();
        if(files1 == null || files1.length == 0){
            return;
        }
        long count = 0L;
        Map<Triplet <String, String, String>, List <Pair <String, byte[]>>> temp = new HashMap<>();
        for(File f : files1){
            if(f.isFile()){
                BufferedReader bufferedReader = streamCache.getReaderStream(f);
                try {
                    String line;
                    //数据封装
                    while ((line = bufferedReader.readLine()) != null) {
                        System.out.println(line);
                        String[] s = line.split("_");
                        Triplet<String, String, String> key = new Triplet <>(s[0], null, s[1]);
                        byte[] bytes = decoder.decodeBuffer(s[3]);
                        Pair<String, byte[]> value = new Pair <>(s[2], bytes);
                        List<Pair<String, byte[]>> list = temp.get(key);
                        if(list == null){
                            list = new ArrayList<>();
                            temp.put(key, list);
                        }
                        list.add(value);
                        count ++ ;
                    }
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        logger.info("The num of Records Loaded is : " + count);
        memoryCacheImpl1.loadCacheRecords(temp);
    }

    /**
     * 加载一个月的数据到内存（内存存储float特征值）
     * @param fi 目录
     * @param month 目标月份
     */
    private void loadRecordForMonth2(File fi, String month){
        logger.info("Read month is : " + month);
        MemoryCacheImpl<String, String, float[]> memoryCacheImpl1 = MemoryCacheImpl.getInstance();
        //得到目标月份的文件夹
        File monthdir = null;
        File[] files = fi.listFiles();
        if(files != null && files.length > 0){
            for(File file : files){
                if (file.isDirectory() && file.getName().equals(month)){
                    monthdir = file;
                }
            }
        }
        if(monthdir == null || !monthdir.isDirectory()){
            return;
        }
        //遍历加载数据文件
        File[] files1 = monthdir.listFiles();
        if(files1 == null || files1.length == 0){
            return;
        }
        long count = 0L;
        Map<Triplet <String, String, String>, List <Pair <String, float[]>>> temp = new HashMap<>();
        for(File f : files1){
            if(f.isFile()){
                BufferedReader bufferedReader = streamCache.getReaderStream(f);
                try {
                    String line;
                    //数据封装
                    while ((line = bufferedReader.readLine()) != null) {
                        String[] s = line.split("_");
                        Triplet <String, String, String> key = new Triplet <>(s[0], null, s[1]);
                        System.out.println(s[3]);
                        float[] floats = FaceObjectUtil.jsonToArray(s[3]);
                        Pair<String, float[]> value = new Pair <>(s[2], floats);
                        List<Pair<String, float[]>> list = temp.get(key);
                        if(list == null){
                            list = new ArrayList<>();
                            temp.put(key, list);
                        }
                        list.add(value);
                        count ++ ;
                    }
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        logger.info("The num of Records Loaded is : " + count);
        memoryCacheImpl1.loadCacheRecords(temp);
    }

    public void loadRecordFromHDFS(){

    }
}
