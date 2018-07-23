package com.hzgc.compare.test;

import com.hzgc.compare.worker.common.Quintuple;
import com.hzgc.compare.worker.persistence.FileManager;
import com.hzgc.compare.worker.persistence.LocalFileManager;

import java.util.ArrayList;

public class TestPersistence {

    public static void main(String[] args) throws Exception{
        ArrayList <Quintuple<String,String,String,String,byte []>> list = new ArrayList <>();
        String A = "DS-2CD2T20FD-I320160122AACH571485690";
        String B = null;
        String C = "2018-07-19";
        String D = "1";
        byte [] bytes = {1,2,3};
        Quintuple <String,String,String,String,byte []> quintuple = new Quintuple <>(A,B,C,D,bytes);
        list.add(quintuple);
        C = "2018-06-31";
        Quintuple <String,String,String,String,byte []> quintuple1 = new Quintuple <>(A,B,C,D,bytes);
        list.add(quintuple1);
        C = "2018-07-19";
        Quintuple <String,String,String,String,byte []> quintuple2 = new Quintuple <>(A,B,C,D,bytes);
        list.add(quintuple2);
        C = "2018-05-21";
        Quintuple <String,String,String,String,byte []> quintuple3 = new Quintuple <>(A,B,C,D,bytes);
        list.add(quintuple3);
        C = "2018-04-21";
        Quintuple <String,String,String,String,byte []> quintuple4 = new Quintuple <>(A,B,C,D,bytes);
        list.add(quintuple4);
        FileManager localFileManager = new LocalFileManager();
        localFileManager.flush(list);
    }
}
