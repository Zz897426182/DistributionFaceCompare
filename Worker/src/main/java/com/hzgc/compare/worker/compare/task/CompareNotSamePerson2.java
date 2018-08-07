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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompareNotSamePerson2 implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(CompareNotSamePerson2.class);
    private int resultDefaultCount = 20;
    private int compareSize = 500;
    private Config conf;
    private CompareParam param;
    private String dateStart;
    private String dateEnd;
    boolean isEnd = false;
    Map<String, SearchResult> searchResult;

    public Map<String, SearchResult> getSearchResult(){
        return searchResult;
    }

    public boolean isEnd(){
        return isEnd;
    }

    public CompareNotSamePerson2(CompareParam param, String dateStart, String dateEnd){
        this.param = param;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
    }


    public Map<String, SearchResult> compare() {
        List<String> ipcIdList = param.getArg1List();
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
        resultTemp = comparators.compareSecondNotSamePerson(features, sim, dataFilterd, param.getSort());
        for(Map.Entry<String, SearchResult> searchResult : resultTemp.entrySet()){
            SearchResult res1 = searchResult.getValue();
            SearchResult res2 = res1;

            List<Integer> sorts = param.getSort();
            if(sorts != null && (sorts.size() > 1 || (sorts.size() == 1 && sorts.get(0) != 5))){
                res2 = res1.take(compareSize);
                //从HBase读取数据
                SearchResult res3 = client.readFromHBase2(res2);
                res3.sort(param.getSort());
                result.put(searchResult.getKey(), res3.take(resultCount));
                continue;
            }
            res2 = res1.take(resultCount);
            //从HBase读取数据
            SearchResult res3 = client.readFromHBase2(res2);
            result.put(searchResult.getKey(), res3);
        }
        return result;
    }

    @Override
    public void run() {
        searchResult = compare();
        isEnd = true;
    }
}
