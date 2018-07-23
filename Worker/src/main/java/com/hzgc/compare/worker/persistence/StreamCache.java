package com.hzgc.compare.worker.persistence;


import com.hzgc.compare.worker.conf.Config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStream;
import java.util.Map;

/**
 * 用于保存本地或HDFS文件的输出流，只保存时间最近的几个
 * 单例
 */
public abstract class StreamCache {
    Map<String, BufferedWriter> streams;
    private Config conf;
    private Long timeToCheckStream = 1000L * 60 * 60;

    public Map<String, BufferedWriter> getStreams() {
        return streams;
    }

    /**
     * 根据文件名，取得输出流，若无，则重新创建
     * @param file
     * @return
     */
    public abstract BufferedReader getReaderStream(File file);

    /**
     * 启动定期任务，检查是否存在过期的stream，过期删除
     */
//    public void check(){
//
//    }
}
