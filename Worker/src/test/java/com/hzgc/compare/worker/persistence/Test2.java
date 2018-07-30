package com.hzgc.compare.worker.persistence;

import com.hzgc.compare.worker.memory.cache.MemoryCacheImpl;
import com.hzgc.compare.worker.memory.manager.MemoryManager;
import com.hzgc.compare.worker.util.FaceObjectUtil;
import javafx.beans.NamedArg;
import javafx.util.Pair;


import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;


public class Test2 {
    private static MemoryCacheImpl cache;
    public static void main(String args[]) throws IOException {
        cache = MemoryCacheImpl.<String, String, float[]>getInstance();
        Test2 test2 = new Test2();
        test2.loadRecordFromLocal();

    }

    public void loadRecordFromLocal() throws FileNotFoundException {
        String workId = "1";
        File workFile = new File("test");
        if(!workFile.isDirectory()){
            return;
        }
        File[] listFiles = workFile.listFiles();
        // 得到当前worker的目录
        File dirForThisWorker = null;
        if (listFiles != null && listFiles.length > 0) {
            for(File fi : listFiles){
                if(fi.isDirectory() && workId.equals(fi.getName())){
                    dirForThisWorker = fi;
                }
            }
        }
        if(dirForThisWorker == null || !dirForThisWorker.isDirectory()){
            return;
        }
        //得到本月和上月
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        String ym = sdf.format(date);
        String [] strings = ym.split("-");
        Integer m = Integer.valueOf(strings[1]) - 1;
        String lastMonth = null;
        if (m > 0 && m < 10){
            lastMonth = strings[0] + "-0" + m;
        }
        if (m == 0) {
            int year = Integer.valueOf(strings[0]) - 1;
            lastMonth = String.valueOf(year) + "-" + String.valueOf(12);
        }
        long start = System.currentTimeMillis();
        // 加载上月的记录
        loadRecordForMonth2(dirForThisWorker, lastMonth);
        // 加载本月的记录
        loadRecordForMonth2(dirForThisWorker, ym);
        System.out.println("The time used to load record is : " + (System.currentTimeMillis() - start));
    }

    private void loadRecordForMonth2(File fi, String month) throws FileNotFoundException {
        System.out.println("Read month is : " + month);
        //得到目标月份的文件夹
        File monthdir = null;
        File[] files = fi.listFiles();
        if(files != null && files.length > 0){
            for(File file : files){
                if (file.isDirectory() && file.getName().equals(month)){
                    monthdir = file;
                }
            }
        }
        if(monthdir == null){
            return;
        }
        //遍历加载数据文件
        File[] files1 = monthdir.listFiles();
        if(files1 == null || files1.length == 0){
            return;
        }
        long count = 0L;
        Map<Triplet<String, String, String>, List<Pair<String, float[]>>> temp = cache.getCacheRecords();
        for(File f : files1){
            if(f.isFile()){
                System.out.println(f.getName());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
                try {
                    String line;
                    //数据封装
                    while ((line = bufferedReader.readLine()) != null) {
                        String[] s = line.split("_");
                        Triplet<String, String, String> key = new Triplet <String, String, String>(s[0], null, s[1]);
                        String featureString = s[3].substring(s[3].indexOf("[") +1, s[3].indexOf("]"));
                        String[] featuresTemp = featureString.split(",");
                        float[] features = new float[512];
                        int i = 0;
                        for(String str : featuresTemp){
                            features[i] = Float.valueOf(str);
                            i++;
                        }
                        Pair<String, float[]> value = new Pair <String, float[]>(s[2], features);
                        List<Pair<String, float[]>> list = temp.get(key);
                        if(list == null){
                            list = new ArrayList<Pair<String, float[]>>();
                            temp.put(key, list);
                        }
                        list.add(value);
                        count ++ ;
                    }
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("The num of Records Loaded is : " + count);
//        memoryCacheImpl1.loadCacheRecords(temp);
    }
}


class Triplet<A, B, C> implements Serializable {
    private A first;
    private B second;
    private C third;

    public Triplet(@NamedArg("first") A first, @NamedArg("second") B second, @NamedArg("third") C third) {
        this.first = first;
        this.second = second;
        this.third = third;

    }

    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }

    public C getThird() {
        return third;
    }

    public String toString() {
        return "(" + first + " , " + second + " , " + third + ")";
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof Triplet) {
            Triplet triplet = (Triplet) o;
            if (first != null ? !first.equals(triplet.first) : triplet.first != null) return false;
            if (second != null ? !second.equals(triplet.second) : triplet.second != null) return false;
            if (third != null ? !third.equals(triplet.third) : triplet.third != null) return false;
            return true;
        }
        return false;
    }
}