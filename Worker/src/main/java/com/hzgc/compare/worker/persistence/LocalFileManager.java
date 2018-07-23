package com.hzgc.compare.worker.persistence;


import com.hzgc.compare.worker.common.Quintuple;
import com.hzgc.compare.worker.conf.Config;
import com.hzgc.compare.worker.persistence.task.TimeToCheckFile;
import com.hzgc.compare.worker.persistence.task.TimeToCheckTask;
import org.apache.log4j.Logger;
import sun.misc.BASE64Encoder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class LocalFileManager implements FileManager {
    private Config conf;
    private String path = ""; //文件保存目录
    private Long fileSize = 256 * 1024 * 1024L;
    private Long deleteTime = 3 * 30 * 24 * 60 * 60 * 1000L;
    private String work_id = "";
    private static Logger LOG = Logger.getLogger(LocalFileManager.class);

    public LocalFileManager() {
        this.conf = Config.getConf();
    }

    public void init() {
        path = conf.getValue(Config.ROOT_PATH);
        work_id = conf.getValue(Config.WORKER_ID);
    }

    public void flush() {

    }

    /*
     *文件存储，大小为256MB
     * 根据workID和月份进行存储
     */
    @Override
    public void flush(List <Quintuple <String, String, String, String, byte[]>> buffer) {
        init();
        LocalStreamCache streamCache = LocalStreamCache.getInstance();
        BASE64Encoder encoder = new BASE64Encoder();
        for (Quintuple <String, String, String, String, byte[]> quintuple : buffer) {
            //创建workid目录
            File workFile = new File(path, work_id);
            if (!workFile.exists()) {
                workFile.mkdir();
                LOG.info("WorkFile name is " + workFile.getName());
            }
            //创建年-月份目录
//            Date date = new Date();
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
//            String ym = sdf.format(date);
//            File ymFile = new File(workFile, ym);
//            if (!ymFile.exists()) {
//                ymFile.mkdir();
//                LOG.info("ymFile name is " + ymFile.getName());
//            }
            //开始写数据
            BufferedWriter bufferedWriter = null;
            //进行base64编码
            String encodedText = encoder.encode(quintuple.getFifth());
            //进行日期判断
            String dateYMD = quintuple.getThird();
            String[] strings = dateYMD.split("-");
            String dateYM = strings[0] + "-" + strings[1];
            LOG.info("dateYM is " + dateYM);
            //年-月份文件
            File[] ymFiles = workFile.listFiles();
            if (ymFiles.length > 0) {
                for (File f : ymFiles) {
                    if (f.isDirectory()) {
                        String name = f.getName();
                        if (name.equals(dateYM)) {
                            //block文件
                            File[] files = f.listFiles();
                            if (files.length > 0) {
                                Arrays.sort(files, new Comparator <File>() {
                                    @Override
                                    public int compare(File o1, File o2) {
                                        return Integer.valueOf(o1.getName().split("\\.")[0]) - Integer.valueOf(o2.getName().split("\\.")[0]);
                                    }
                                });
                                File file = files[files.length - 1];
                                Integer fileName = Integer.valueOf(file.getName().split("\\.")[0]);
                                String data = quintuple.getFirst() + "_" + quintuple.getThird() + "_" + quintuple.getFourth() + "_" + encodedText;
                                //判断文件大小
                                if (file.length() + data.getBytes().length / 8 >= fileSize) {
                                    fileName = fileName + 1;
                                    File blockFile = new File(f, String.valueOf(fileName) + ".txt");
                                    try {
                                        blockFile.createNewFile();
                                        bufferedWriter = streamCache.getWriterStream(blockFile);
                                        bufferedWriter.write(data, 0, data.length());
                                        bufferedWriter.newLine();
                                        bufferedWriter.flush();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    try {
                                        bufferedWriter = streamCache.getWriterStream(file);
                                        bufferedWriter.write(data, 0, data.length());
                                        bufferedWriter.newLine();
                                        bufferedWriter.flush();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                File blockFile = new File(f, 0 + ".txt");
                                try {
                                    if (!blockFile.exists()){
                                        blockFile.createNewFile();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                String data = quintuple.getFirst() + "_" + quintuple.getThird() + "_" + quintuple.getFourth() + "_" + encodedText;
                                try {
                                    if (bufferedWriter != null){
                                        bufferedWriter.close();
                                    }
                                    bufferedWriter = streamCache.getWriterStream(blockFile);
                                    bufferedWriter.write(data, 0, data.length());
                                    bufferedWriter.newLine();
                                    bufferedWriter.flush();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }else {
                            File file = new File(workFile, dateYM);
                            if (!file.exists()){
                                file.mkdir();
                            }
                            File bFile = new File(file, 0 + ".txt");
                            if (!bFile.exists()){
                                try {
                                    bFile.createNewFile();
                                    String data = quintuple.getFirst() + "_" + quintuple.getThird() + "_" + quintuple.getFourth() + "_" + encodedText;
                                    if (bufferedWriter != null){
                                        bufferedWriter.close();
                                    }
                                    bufferedWriter = streamCache.getWriterStream(bFile);
                                    bufferedWriter.write(data, 0, data.length());
                                    bufferedWriter.newLine();
                                    bufferedWriter.flush();
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

    public void createFile() {

    }

    public void checkFile() {
        new Timer().schedule(new TimeToCheckFile(), deleteTime);
    }

    public void checkTaskTodo() {
        new Timer().schedule(new TimeToCheckTask(this), 1000, 1000);
    }
}
