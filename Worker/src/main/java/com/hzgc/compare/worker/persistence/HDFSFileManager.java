package com.hzgc.compare.worker.persistence;

import com.hzgc.compare.worker.common.Quintuple;
import com.hzgc.compare.worker.conf.Config;

import java.util.List;

public class HDFSFileManager implements FileManager {
    private Config conf;
    private Integer fileSavProgram = 1; //文件保存方案（几天的数据保存成一个文件），默认1
    private String fileParh = ""; //文件保存目录
    private Long fileCheckTime = 1000L * 60 * 30; //任务执行时间间隔，默认30分钟
    private Long timeToCheckTask = 1000L; //任务间隔时间，默认1秒

    public void init() {

    }

    public void flush() {

    }


    public void flush(List<Quintuple<String, String, String, String, byte[]>> buffer) {

    }

    public void createFile() {

    }

    public void checkFile() {

    }

    public void timeToFlush() {

    }

    public void checkTaskTodo() {

    }
}
