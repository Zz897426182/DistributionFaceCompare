package com.hzgc.compare.worker.compare;

import com.hzgc.compare.worker.common.CompareParam;
import com.hzgc.compare.worker.common.SearchResult;
import com.hzgc.compare.worker.conf.Config;
import com.hzgc.compare.worker.persistence.HBaseClient;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CompareOnePerson implements Runnable{
    private static final Logger logger = LoggerFactory.getLogger(CompareOnePerson.class);
    private int resultDefaultCount = 20;
    private Config conf;
    @Override
    public void run() {

    }

    public SearchResult compare(CompareParam param, String dateStart, String dateEnd){
        List<String> ipcIdList = param.getArg1List();
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
        return result;
    }
}
