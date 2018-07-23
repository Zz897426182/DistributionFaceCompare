package com.hzgc.compare.worker.persistence;

import com.hzgc.compare.worker.conf.Config;

import java.io.*;

public class LocalStreamCache extends StreamCache {
    private Config conf;
    private  static LocalStreamCache localStreamCache;

    private LocalStreamCache() {
        this.conf = Config.getConf();
    }

    public BufferedWriter getWriterStream(File file) {
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,true)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bufferedWriter;
    }

    @Override
    public BufferedReader getReaderStream(File file) {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
        return bufferedReader;
    }

    public static LocalStreamCache getInstance(){
        if(localStreamCache == null){
            localStreamCache = new LocalStreamCache();
        }
        return localStreamCache;
    }
}
