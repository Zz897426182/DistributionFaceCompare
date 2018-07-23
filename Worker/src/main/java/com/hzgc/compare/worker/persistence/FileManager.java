package com.hzgc.compare.worker.persistence;


import com.hzgc.compare.worker.common.Quintuple;

import java.util.List;

/**
 * FilterManager主要用于管理本地文件（或HDFS文件），主要作用是内存持久化，本地文件的创建和删除
 */
public interface FileManager {

    /**
     * FileManager初始化
     */
    void init();
    /**
     * 获取当前buffer数据，持久化
     */
    void flush();

    void flush(List<Quintuple<String, String, String, String, byte[]>> buffer);

    /**
     * 若写出的数据不在当前时间段，则需要创建新的文件，建立新的输出流，并保存流
     */
    void createFile();

    /**
     * 启动定期任务，检查文件是否存在过期，并删除过期文件，以及对应的HBase数据
     */
    void checkFile();

}
