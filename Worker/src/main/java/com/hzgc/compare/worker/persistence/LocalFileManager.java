package com.hzgc.compare.worker.persistence;


import com.hzgc.compare.worker.common.Quad;
import com.hzgc.compare.worker.common.Quintuple;
import com.hzgc.compare.worker.conf.Config;
import com.hzgc.compare.worker.persistence.task.TimeToCheckFile;
import com.hzgc.compare.worker.persistence.task.TimeToCheckTask;
import com.hzgc.compare.worker.util.FaceObjectUtil;
import org.apache.log4j.Logger;
import sun.misc.BASE64Encoder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class LocalFileManager<A1, A2, D> implements FileManager<A1, A2, D> {
    private Config conf;
    private String path = ""; //文件保存目录
    private Long fileSize = 256 * 1024 * 1024L;
    private Long timeToCheckFile = 24 * 60 * 60 * 1000L;
    private String work_id = "";
    private static Logger LOG = Logger.getLogger(LocalFileManager.class);
    private LocalStreamCache streamCache;

    public LocalFileManager() {
        this.conf = Config.getConf();
        init();
    }

    public void init() {
        path = conf.getValue(Config.WORKER_FILE_PATH, path);
        work_id = conf.getValue(Config.WORKER_ID);
        timeToCheckFile = conf.getValue(Config.WORKER_FILE_CHECK_TIME, timeToCheckFile);
        fileSize = conf.getValue(Config.WORKER_FILE_SIZE, fileSize);
        streamCache = LocalStreamCache.getInstance();
    }

    public void flush() {

    }

    /*
     *文件存储，大小默认为256MB
     * 根据workID和月份进行存储
     */
    @Override
    public void flush(List <Quintuple <A1, A2, String, String, D>> buffer) {
        BASE64Encoder encoder = new BASE64Encoder();
        Map<String , List<Quad<A1, String, String, String>>> temp = new Hashtable<>();
        for(Quintuple <A1, A2, String, String, D> quintuple : buffer){
            D fea = quintuple.getFifth();
            String feature = fea instanceof byte[] ? encoder.encode((byte[])fea) : FaceObjectUtil.arrayToJson((float[]) fea);
            String dateYMD = quintuple.getThird();
            String[] strings = dateYMD.split("-");
            String dateYM = strings[0] + "-" + strings[1];
            List<Quad<A1, String, String, String>> list = temp.get(dateYM);
            if(list == null){
                list = new ArrayList<>();
                temp.put(dateYM, list);
            }
            list.add(new Quad<>(quintuple.getFirst(), dateYMD, quintuple.getFourth(), feature));
        }

        for(Map.Entry<String , List<Quad<A1, String, String, String>>> entry : temp.entrySet()){
            String dateYM = entry.getKey();
            List<Quad<A1, String, String, String>> datas = entry.getValue();
            try {
                flushForMonth(dateYM, datas);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 文件存储，大小默认为256MB
     * 根据workID和月份进行存储
     * @param month 要写的月份
     * @param datas 数据
     * @throws IOException
     */
    private void flushForMonth(String month, List<Quad<A1, String, String, String>> datas) throws IOException {
        File rootPath = new File(path);
        if (!rootPath.exists()) {
            boolean res = rootPath.mkdir();
            if(!res){
                throw new IOException("创建文件夹 " + rootPath.getAbsolutePath() + " 失败");
            }
        }
        //创建workid目录
        File workFile = new File(path, work_id);
        if (!workFile.exists()) {
            boolean res = workFile.mkdir();
            if(!res){
                throw new IOException("创建文件夹 " + workFile.getAbsolutePath() + " 失败");
            }
            LOG.info("WorkFile name is " + workFile.getName());
        }
        //寻找目标月的文件夹
        File dirMonth = new File(workFile, month);
        File[] ymFiles = workFile.listFiles();
        if (ymFiles != null && ymFiles.length > 0){
            for (File f : ymFiles){
                if (f.isDirectory() && f.getName().equals(month)){
                    dirMonth = f;
                }
            }
        }
        //若没有该文件夹，创建
        if(!dirMonth.exists() || !dirMonth.isDirectory()){
            boolean res = dirMonth.mkdir();
            if(!res){
                throw new IOException("创建文件夹 " + dirMonth.getAbsolutePath() + " 失败");
            }
        }
        //寻找目标文件
        File[] files = dirMonth.listFiles();
        File fileToWrite;
        Comparator<File> comparator = new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return Integer.valueOf(o1.getName().split("\\.")[0]) - Integer.valueOf(o2.getName().split("\\.")[0]);
            }
        };
        if(files == null || files.length == 0){
            fileToWrite = new File(dirMonth, 0 + ".txt");
            boolean res = fileToWrite.createNewFile();
            if(!res){
                throw new IOException("创建文件" + fileToWrite.getName()  + "失败。");
            }
        } else{
            fileToWrite = files[0];
            for(File f : files){
                if(comparator.compare(f, fileToWrite) > 0){
                    fileToWrite = f;
                }
            }
        }
        long theFileSize = fileToWrite.length();
        for(Quad<A1, String, String, String>data : datas) {
            BufferedWriter bufferedWriter = null;
            String info = data.getFirst() + "_" + data.getSecond() + "_" + data.getThird() + "_" + data.getFourth();
            Integer fileName = Integer.valueOf(fileToWrite.getName().split("\\.")[0]);
            //判断文件大小
            if (theFileSize + info.length() + 2 >= fileSize) {
                bufferedWriter = streamCache.getWriterStream(fileToWrite);
                bufferedWriter.flush();
                bufferedWriter.close();
                fileName = fileName + 1;
                fileToWrite = new File(dirMonth, String.valueOf(fileName) + ".txt");
                boolean res = fileToWrite.createNewFile();
                if(!res){
                    throw new IOException("创建文件" + fileToWrite.getName()  + "失败。");
                }
                theFileSize = 0L;
            }

            bufferedWriter = streamCache.getWriterStream(fileToWrite);
            bufferedWriter.write(info, 0, info.length());
            bufferedWriter.newLine();
            theFileSize += (info.length() + 2);
            if(theFileSize > 1024 * 100) {
                bufferedWriter.flush();
                theFileSize = fileToWrite.length();
            }
        }
        streamCache.getWriterStream(fileToWrite).flush();

    }

    public void createFile() {

    }

    public void checkFile() {
        new Timer().schedule(new TimeToCheckFile(), timeToCheckFile, timeToCheckFile);
    }

    public void checkTaskTodo() {
        new Timer().schedule(new TimeToCheckTask<A1,A2,D>(this), 1000, 1000);
    }
}
