package com.hzgc.compare.worker.compare.task;

import com.hzgc.compare.worker.common.CompareParam;
import com.hzgc.compare.worker.common.Feature;
import com.hzgc.compare.worker.common.SearchResult;
import com.hzgc.compare.worker.compare.ComparatorsImpl2;
import com.hzgc.compare.worker.conf.Config;
import com.hzgc.compare.worker.persistence.HBaseClient;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CompareSamePerson2 extends CompareTask{
    private static final Logger logger = LoggerFactory.getLogger(CompareSamePerson2.class);
    private int resultDefaultCount = 20;
    private int compareSize = 500;
    private Config conf;
    private CompareParam param;
    private String dateStart;
    private String dateEnd;


    public CompareSamePerson2(CompareParam param, String dateStart, String dateEnd){
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
        result = comparators.compareSecondTheSamePerson(feature2List, sim, dataFilterd, param.getSort());
        //取相似度最高的几个
        logger.info("Take the top " + resultCount);
        List<Integer> sorts = param.getSort();
        if(sorts != null && (sorts.size() > 1 || (sorts.size() == 1 && sorts.get(0) != 5))){
            result = result.take(compareSize);
            logger.info("Read records from HBase.");
            result = client.readFromHBase2(result);
            result.sort(sorts);
            result = result.take(resultCount);
            return result;
        }
        result = result.take(resultCount);
        //从HBase读取数据
        logger.info("Read records from HBase.");
        result = client.readFromHBase2(result);
        return result;
    }
}
