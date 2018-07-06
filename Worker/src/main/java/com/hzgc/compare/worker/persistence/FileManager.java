package com.hzgc.compare.worker.persistence;



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

    /**
     * 若写出的数据不在当前时间段，则需要创建新的文件，建立新的输出流，并保存流
     */
    void createFile();

    /**
     * 启动定期任务，检查文件是否存在过期，并删除过期文件
     */
    void checkFile();

    /**
     * 启动定时任务，定期查看TaskToHandle中有无FlushTask，如果有，则flush其中的记录，并删除该FlushTask
     */
    void checkTaskTodo();

}
