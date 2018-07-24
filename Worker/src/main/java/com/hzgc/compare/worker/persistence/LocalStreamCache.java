package com.hzgc.compare.worker.persistence;

import com.hzgc.compare.worker.conf.Config;

import java.io.*;

public class LocalStreamCache {
    private Config conf;
    private  static LocalStreamCache localStreamCache;
    private BufferedWriter bufferedWriter;
    private File file;

    private LocalStreamCache() {
        this.conf = Config.getConf();
    }

    public BufferedWriter getWriterStream(File file) {
        if(bufferedWriter == null || !file.equals(this.file)) {
            try {
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));
                this.file = file;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return bufferedWriter;
    }

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
