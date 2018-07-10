package com.hzgc.compare.worker.persistence;

import com.hzgc.compare.worker.common.FaceInfoTable;
import com.hzgc.compare.worker.common.FaceObject;
import com.hzgc.compare.worker.conf.Config;
import com.hzgc.compare.worker.persistence.task.TimeToWrite;
import com.hzgc.compare.worker.util.FaceObjectUtil;
import com.hzgc.compare.worker.util.HBaseHelper;
import javafx.util.Pair;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;


/**
 * 负责与HBas交互，定期插入数据，以及读取第一次比较结果
 */
public class HBaseClient {
    private Config conf;

    public HBaseClient(Config conf){
        this.conf = conf;
    }

    /**
     * 启动任务，定期读取内存中的recordToHBase，保存在HBase中，并生成元数据保存入内存的buffer
     */
    public void timeToWrite(){
        TimeToWrite task = new TimeToWrite(conf);
        task.start();
    }

    /**
     * 根据过滤结果，查询HBase中的数据
     * @param rowkeys
     * @return
     */
    public List<FaceObject> readFromHBase(List<String> rowkeys){
        List<FaceObject> list = new ArrayList<>();
        long start = System.currentTimeMillis();
        Connection conn = HBaseHelper.getHBaseConnection();
        try {
            Table table = conn.getTable(TableName.valueOf(FaceInfoTable.TABLE_NAME));
            List<Get> gets = new ArrayList<>();
            for(String rowkey : rowkeys){
                gets.add(new Get(Bytes.toBytes(rowkey)));
            }
            Result[]  results = table.get(gets);
            for (Result result : results){//对返回的结果集进行操作
                for (Cell kv : result.rawCells()) {
                    FaceObject value = FaceObjectUtil.jsonToObject(Bytes.toString(CellUtil.cloneValue(kv))) ;
                    list.add(value);
                }
            }
            System.out.println("The time used to get data from hbase is : " + (System.currentTimeMillis() - start));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 根据第一次比较的结果，查询HBase中的数据
     * @param records
     * @return
     */
    public List<FaceObject> readFromHBase2(List<Pair<String, byte[]>> records){
        List<FaceObject> list = new ArrayList<>();
        long start = System.currentTimeMillis();
        Connection conn = HBaseHelper.getHBaseConnection();
        try {
            Table table = conn.getTable(TableName.valueOf(FaceInfoTable.TABLE_NAME));
            List<Get> gets = new ArrayList<>();
            for(Pair<String, byte[]> record : records){
                gets.add(new Get(Bytes.toBytes(record.getKey())));
            }
            Result[]  results = table.get(gets);
            for (Result result : results){//对返回的结果集进行操作
                for (Cell kv : result.rawCells()) {
                    FaceObject value = FaceObjectUtil.jsonToObject(Bytes.toString(CellUtil.cloneValue(kv))) ;
                    list.add(value);
                }
            }
            System.out.println("The time used to get data from hbase is : " + (System.currentTimeMillis() - start));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

//    /**
//     * 对比结束，根据结果查询HBase数据
//     * @param compareRes
//     * @return
//     */
//    public SearchResult readFromHBase2(SearchResult compareRes){
//        long start = System.currentTimeMillis();
//        Connection conn = HBaseHelper.getHBaseConnection();
//        try {
//            Table table = conn.getTable(TableName.valueOf(FaceInfoTable.TABLE_NAME));
//            List<Get> gets = new ArrayList<>();
//            for(SearchResult.Record record : compareRes.getRecords()){
//                gets.add(new Get(Bytes.toBytes((String) record.getValue())));
//            }
//            Result[]  results = table.get(gets);
//            int index = 0;
//            for (Result result : results){//对返回的结果集进行操作
//                for (Cell kv : result.rawCells()) {
//                    FaceObject object = FaceObjectUtil.jsonToObject(Bytes.toString(CellUtil.cloneValue(kv))) ;
//                    compareRes.getRecords()[index] = new SearchResult.Record(compareRes.getRecords()[index].getKey(), object);
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        System.out.println("The time used to get result is : " + (System.currentTimeMillis() - start));
//        return compareRes;
//    }
}
