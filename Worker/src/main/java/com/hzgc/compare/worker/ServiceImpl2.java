package com.hzgc.compare.worker;

import com.hzgc.compare.rpc.client.result.AllReturn;
import com.hzgc.compare.rpc.server.annotation.RpcService;
import com.hzgc.compare.worker.common.CompareParam;
import com.hzgc.compare.worker.common.Feature;
import com.hzgc.compare.worker.common.SearchResult;
import com.hzgc.compare.worker.compare.ComparatorsImpl2;
import com.hzgc.compare.worker.conf.Config;
import com.hzgc.compare.worker.persistence.HBaseClient;
import com.hzgc.compare.worker.util.FaceObjectUtil;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RpcService(Service.class)
public class ServiceImpl2 implements Service{
    private static final Logger logger = LoggerFactory.getLogger(ServiceImpl2.class);
    private int resultDefaultCount = 20;
    private Config conf;

    public ServiceImpl2(){
        this.conf = Config.getConf();
        resultDefaultCount = conf.getValue("", resultDefaultCount);
    }

    @Override
    public AllReturn<SearchResult> retrievalOnePerson(CompareParam param) {
        logger.info("The param is : " + FaceObjectUtil.objectToJson(param));
        List<String> ipcIdList = param.getArg1List();
        String dateStart = param.getDateStart();
        String dateEnd = param.getDateEnd();
        float[] feature2 = param.getFeatures().get(0).getFeature2();
        float sim = param.getSim();
        int resultCount = param.getResultCount();
        if (resultCount == 0){
            resultCount = resultDefaultCount;
        }
        SearchResult result;
        HBaseClient client = new HBaseClient();
        ComparatorsImpl2 comparators = new ComparatorsImpl2();
        // 根据条件过滤
        logger.info("To filter the records from memory.");
        List<Pair<String, float[]>> dataFilterd =  comparators.filter(ipcIdList, null, dateStart, dateEnd);
        // 执行对比
        logger.info("To compare the result of filterd.");
        result = comparators.compareSecond(feature2, sim, dataFilterd);
        //取相似度最高的几个
        logger.info("Take the top " + resultCount);
        result = result.take(resultCount);
        logger.info("Read records from HBase.");
        result = client.readFromHBase2(result);
        return new AllReturn<>(result);
    }

    @Override
    public AllReturn<SearchResult> retrievalSamePerson(CompareParam param) {
        logger.info("The param is : " + FaceObjectUtil.objectToJson(param));
        List<String> ipcIdList = param.getArg1List();
        String dateStart = param.getDateStart();
        String dateEnd = param.getDateEnd();
        List<Feature> features = param.getFeatures();
        float sim = param.getSim();
        int resultCount = param.getResultCount();
        if (resultCount == 0){
            resultCount = resultDefaultCount;
        }
        List<float[]> feature2List = new ArrayList<>();
        for(Feature feature : features){
            feature2List.add(feature.getFeature2());
        }
        SearchResult result;
        HBaseClient client = new HBaseClient();
        ComparatorsImpl2 comparators = new ComparatorsImpl2();
        // 根据条件过滤
        logger.info("To filter the records from memory.");
        List<Pair<String, float[]>> dataFilterd =  comparators.filter(ipcIdList, null, dateStart, dateEnd);
        // 执行对比
        logger.info("To compare the result of filterd.");
        result = comparators.compareSecondTheSamePerson(feature2List, sim, dataFilterd);
        //取相似度最高的几个
        logger.info("Take the top " + resultCount);
        result = result.take(resultCount);
        //从HBase读取数据
        logger.info("Read records from HBase.");
        result = client.readFromHBase2(result);
        return new AllReturn<>(result);
    }

    @Override
    public AllReturn<Map<String, SearchResult>> retrievalNotSamePerson(CompareParam param) {
        logger.info("The param is : " + FaceObjectUtil.objectToJson(param));
        List<String> ipcIdList = param.getArg1List();
        String dateStart = param.getDateStart();
        String dateEnd = param.getDateEnd();
        List<Feature> features = param.getFeatures();
        float sim = param.getSim();
        int resultCount = param.getResultCount();
        if (resultCount == 0){
            resultCount = resultDefaultCount;
        }

        Map<String, SearchResult> resultTemp;
        Map<String, SearchResult> result = new HashMap<>();
        HBaseClient client = new HBaseClient();
        ComparatorsImpl2 comparators = new ComparatorsImpl2();
        // 根据条件过滤
        logger.info("To filter the records from memory.");
        List<Pair<String, float[]>> dataFilterd =  comparators.filter(ipcIdList, null, dateStart, dateEnd);
        // 执行对比
        logger.info("To compare the result of filterd.");
        resultTemp = comparators.compareSecondNotSamePerson(features, sim, dataFilterd);
        for(Map.Entry<String, SearchResult> searchResult : resultTemp.entrySet()){
            SearchResult res1 = searchResult.getValue().take(resultCount);
            //从HBase读取数据
            SearchResult res2 = client.readFromHBase2(res1);
            result.put(searchResult.getKey(), res2);
        }
        return new AllReturn<>(result);
    }

    @Override
    public AllReturn<Boolean> stopTheWorker() {
        return new AllReturn<>(true);
    }
}
