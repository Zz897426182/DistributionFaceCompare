package com.hzgc.compare.worker;

import com.hzgc.compare.rpc.server.annotation.RpcService;
import com.hzgc.compare.worker.common.FaceObject;
import com.hzgc.compare.worker.common.SearchResult;
import com.hzgc.compare.worker.compare.Comparators;
import com.hzgc.compare.worker.compare.ComparatorsImpl;
import com.hzgc.compare.worker.conf.Config;
import com.hzgc.compare.worker.persistence.HBaseClient;
import javafx.util.Pair;

import java.util.List;

@RpcService(Service.class)
public class ServiceImpl implements Service{
    private int resultDefaultCount = 10;
    private int compareSize = 5000;
    private Config conf;
    public void init(Config conf){
        this.conf = conf;
        resultDefaultCount = conf.getValue("", resultDefaultCount);
        compareSize = conf.getValue("", compareSize);
    }
    public SearchResult retrieval(List<String> arg1List, String arg2RangStart,
                                  String arg2RangEnd, byte[] feature1, float[] feature2, int resultCount){
        return null;
    }

    @Override
    public SearchResult retrieval(List<String> ipcIdList, List<String> arg2List, String dateStart, String dateEnd,
                                  byte[] feature1, float sim1, float[] feature2, float sim2, int resultCount) {
        if (resultCount == 0){
            resultCount = resultDefaultCount;
        }
        SearchResult result;
        HBaseClient client = new HBaseClient(conf);
        Comparators comparators = new ComparatorsImpl(conf);
        // 根据条件过滤
        List<Pair<String, byte[]>> dataFilterd =  comparators.filter(ipcIdList, null, dateStart, dateStart);
        if(dataFilterd.size() > compareSize){
            // 若过滤结果太大，则需要第一次对比
            List<String> firstCompared =  comparators.compareFirst(feature1, sim1, dataFilterd);
            //根据对比结果从HBase读取数据
            List<FaceObject> objs =  client.readFromHBase(firstCompared);
            // 第二次对比
            result = comparators.compareSecond(feature2, sim2, objs);
            //结果排序
            result.sortBySim();
            //取相似度最高的几个
            result = result.take(resultDefaultCount);
        }else {
            //若过滤结果比较小，则直接进行第二次对比
            List<FaceObject> objs = client.readFromHBase2(dataFilterd);
            result = comparators.compareSecond(feature2, sim2, objs);
            //结果排序
            result.sortBySim();
            //取相似度最高的几个
            result = result.take(resultCount);
        }
        return result;
    }
}
