package com.hzgc.compare.worker.compare.task;

import com.hzgc.compare.worker.common.CompareParam;
import com.hzgc.compare.worker.common.FaceObject;
import com.hzgc.compare.worker.common.SearchResult;
import com.hzgc.compare.worker.compare.Comparators;
import com.hzgc.compare.worker.compare.ComparatorsImpl;
import com.hzgc.compare.worker.conf.Config;
import com.hzgc.compare.worker.persistence.HBaseClient;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CompareOnePerson extends CompareTask {
    private static final Logger logger = LoggerFactory.getLogger(CompareOnePerson.class);
    private int resultDefaultCount = 20;
    private Config conf;
    private int compareSize = 500;
    private CompareParam param;
    private String dateStart;
    private String dateEnd;


    public CompareOnePerson(CompareParam param, String dateStart, String dateEnd){
        this.param = param;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
    }

    public SearchResult getSearchResult(){
        return searchResult;
    }

    public boolean isEnd(){
        return isEnd;
    }

    @Override
    public SearchResult compare() {
        List<String> ipcIdList = param.getArg1List();
        byte[] feature1 = param.getFeatures().get(0).getFeature1();
        float[] feature2 = param.getFeatures().get(0).getFeature2();
        float sim = param.getSim();
        int resultCount = param.getResultCount();
        if (resultCount == 0){
            resultCount = resultDefaultCount;
        }
        SearchResult result;
        HBaseClient client = new HBaseClient();
        Comparators comparators = new ComparatorsImpl();
        // 根据条件过滤
        logger.info("To filter the records from memory.");
        List<Pair<String, byte[]>> dataFilterd =  comparators.<byte[]>filter(ipcIdList, null, dateStart, dateEnd);
        if(dataFilterd.size() > compareSize){
            // 若过滤结果太大，则需要第一次对比
            logger.info("The result of filter is too bigger , to compare it first.");
            List<String> firstCompared =  comparators.compareFirst(feature1, 500, dataFilterd);
            //根据对比结果从HBase读取数据
            logger.info("Read records from HBase with result of first compared.");
            List<FaceObject> objs =  client.readFromHBase(firstCompared);
            // 第二次对比
            logger.info("Compare records second.");
            result = comparators.compareSecond(feature2, sim, objs, param.getSort());
            //取相似度最高的几个
            logger.info("Take the top " + resultCount);
            result = result.take(resultCount);
        }else {
            //若过滤结果比较小，则直接进行第二次对比
            logger.info("Read records from HBase with result of filter.");
            List<FaceObject> objs = client.readFromHBase2(dataFilterd);
//            System.out.println("过滤结果" + objs.size() + " , " + objs.get(0));
            logger.info("Compare records second directly.");
            result = comparators.compareSecond(feature2, sim, objs, param.getSort());
            //取相似度最高的几个
            logger.info("Take the top " + resultCount);
            result = result.take(resultCount);
        }
//        System.out.println("对比结果2" + result.getRecords().length + " , " + result.getRecords()[0]);
        return result;
    }
}
