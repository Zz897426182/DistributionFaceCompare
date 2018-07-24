package com.hzgc.compare.worker.persistence.task;

import com.hzgc.compare.worker.conf.Config;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.*;

/**
 * 定期任务，检查文件是否存在过期，并删除过期文件,按照月份进行删除
 */
public class TimeToCheckFile extends TimerTask {
    private Config conf;
    private String path;
    private String tag;  //1标识删除文件
    private static Logger LOG = Logger.getLogger(TimeToCheckFile.class);

    public TimeToCheckFile() {
        this.conf = Config.getConf();
    }

    public void init() {
        path = conf.getValue(Config.ROOT_PATH);
        tag = conf.getValue(Config.DELETE_OPEN);
    }

    @Override
    public void run() {
        init();
        if ("1".equals(tag)) {
            File pathFile = new File(path);
            File[] idFiles = pathFile.listFiles();
            if (idFiles != null && idFiles.length > 0) {
                //work_id文件
                for (File f : idFiles) {
                    if (f.isDirectory()) {
                        //年-月份文件
                        File[] ymFiles = f.listFiles();
                        if (ymFiles != null && ymFiles.length > 0) {
                            for (File file : ymFiles) {
                                String fName = file.getName();
                                Calendar calendar = Calendar.getInstance();
                                int month = calendar.get(Calendar.MONTH) + 1;
                                int year = calendar.get(Calendar.YEAR);
                                String ym = year + "-0" + month;
                                String lym = year + "-0" + (month - 1);
                                String llym = year + "-0" + (month - 2);
                                if (month == 2) {
                                    lym = year + "-" + (month - 1);
                                    llym = (year - 1) + "-" + 12;
                                } else if (month == 1) {
                                    lym = (year - 1) + "-" + 12;
                                    llym = (year - 1) + "-" + 11;
                                } else if (month == 12) {
                                    ym = year + "-" + month;
                                    lym = year + "-" + 11;
                                    llym = year + "-" + 10;
                                } else if (month == 11) {
                                    ym = year + "-" + month;
                                    lym = year + "-" + 10;
                                } else if (month == 10) {
                                    ym = year + "-" + month;
                                }
                                if (!fName.equals(ym) && !fName.equals(lym) && !fName.equals(llym)) {
                                    System.out.println(fName);
                                    File[] files = file.listFiles();
                                    //删除月份下的文件
                                    if (files != null && files.length > 0) {
                                        for (File file1 : files) {
                                            file1.delete();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            LOG.info("Data is not delete");
        }
    }
}
