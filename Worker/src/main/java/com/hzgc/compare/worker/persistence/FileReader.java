package com.hzgc.compare.worker.persistence;


import com.hzgc.compare.worker.conf.Config;

/**
 *
 */
public class FileReader {
    private Config conf;
    private Integer loadDays = 90; //加载多少天的数据到内存中，默认90天

    public FileReader(){
        this.conf = Config.getConf();
    }
    /**
     * 项目启动时，从本地文件中加载数据到内存
     */
    public void loadRecord(){

    }

    public void loadRecordFromHDFS(){

    }
}
