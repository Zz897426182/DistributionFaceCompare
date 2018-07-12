package com.hzgc.compare.worker;

import com.hzgc.compare.demo.CompareParam;
import com.hzgc.compare.rpc.client.result.AllReturn;
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
    private int compareSize = 50000;
    private Config conf;

    public ServiceImpl(){
        this.conf = Config.getConf();
        resultDefaultCount = conf.getValue("", resultDefaultCount);
        compareSize = conf.getValue("", compareSize);
    }
//    public AllReturn<SearchResult> retrieval(List<String> arg1List, String arg2RangStart,
//                                             String arg2RangEnd, byte[] feature1, float[] feature2, int resultCount){
//        return new AllReturn<>(null);
//    }

    @Override
    public AllReturn<SearchResult> retrieval(CompareParam param) {
        List<String> ipcIdList = param.getArg1List();
        String dateStart = param.getDateStart();
        String dateEnd = param.getDateEnd();
        byte[] feature1 = param.getFeature1();
        float[] feature2 = param.getFeature2();
        float sim = param.getSim();
        int resultCount = param.getResultCount();
        if (resultCount == 0){
            resultCount = resultDefaultCount;
        }
        SearchResult result;
        HBaseClient client = new HBaseClient();
        Comparators comparators = new ComparatorsImpl();
        // 根据条件过滤
        List<Pair<String, byte[]>> dataFilterd =  comparators.filter(ipcIdList, null, dateStart, dateEnd);
        if(dataFilterd.size() > compareSize){
            // 若过滤结果太大，则需要第一次对比
            List<String> firstCompared =  comparators.compareFirst(feature1, 500, dataFilterd);
            //根据对比结果从HBase读取数据
            List<FaceObject> objs =  client.readFromHBase(firstCompared);
            // 第二次对比
            result = comparators.compareSecond(feature2, sim, objs);
            //结果排序
            result.sortBySim();
            //取相似度最高的几个
            result = result.take(resultDefaultCount);
        }else {
            //若过滤结果比较小，则直接进行第二次对比
            List<FaceObject> objs = client.readFromHBase2(dataFilterd);
            result = comparators.compareSecond(feature2, sim, objs);
            //结果排序
            result.sortBySim();
            //取相似度最高的几个
            result = result.take(resultCount);
        }
        return new AllReturn<>(result);
    }
}
