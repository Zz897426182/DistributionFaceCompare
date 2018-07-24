package com.hzgc.compare.worker.compare;

import com.hzgc.compare.worker.common.Feature;
import com.hzgc.compare.worker.common.SearchResult;
import com.hzgc.compare.worker.common.Triplet;
import com.hzgc.compare.worker.jni.CompareResult;
import com.hzgc.compare.worker.jni.FaceFeatureInfo;
import com.hzgc.compare.worker.jni.FeatureCompared;
import com.hzgc.compare.worker.memory.cache.MemoryCacheImpl;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ComparatorsImpl2 {
    private static final Logger logger = LoggerFactory.getLogger(ComparatorsImpl2.class);

    public List<Pair<String, float[]>> filter(List<String> arg1List, String arg2, String dateStart, String dateEnd) {
        List<Pair<String, float[]>> result = new ArrayList<>();
        Map<Triplet<String, String, String>, List<Pair<String, float[]>>> cacheRecords =
                MemoryCacheImpl.<String, String, float[]>getInstance().getCacheRecords();
//        Set<Triplet<String, String, String>> temp = new HashSet<>();
//        temp.addAll(cacheRecords.keySet());
        Iterator<Triplet<String, String, String> > iterator =  cacheRecords.keySet().iterator();
        Long start = System.currentTimeMillis();
        if(arg1List == null || arg1List.size() == 0){
            while (iterator.hasNext()) {
                Triplet<String, String, String> key = iterator.next();
                String key1 = key.getFirst();
                String key2 = key.getSecond();
                String key3 = key.getThird();
                if ((key2 == null || key2.equals(arg2)) &&
                        key3.compareTo(dateStart) > 0 &&
                        key3.compareTo(dateEnd) < 0) {
                    result.addAll(cacheRecords.get(key));
                }
            }
            logger.info("The time used to filter is : " + (System.currentTimeMillis() - start));
            logger.info("The size filterd is : " + result.size());
            return result;
        }
        for(String arg1 : arg1List) {
            while (iterator.hasNext()) {
                Triplet<String, String, String> key = iterator.next();
                String key1 = key.getFirst();
                String key2 = key.getSecond();
                String key3 = key.getThird();
                if ((key1 == null || key1.equals(arg1)) &&
                        (key2 == null || key2.equals(arg2)) &&
                        key3.compareTo(dateStart) > 0 &&
                        key3.compareTo(dateEnd) < 0) {
                    result.addAll(cacheRecords.get(key));
                }
            }
        }
        logger.info("The time used to filter is : " + (System.currentTimeMillis() - start));
        logger.info("The size filterd is : " + result.size());
        return result;
    }


    public SearchResult compareSecond(float[] feature, float sim, List<Pair<String, float[]>> datas){
        Long start = System.currentTimeMillis();
        float[][] diku = new float[datas.size()][512];
        int index = 0;
        for(Pair<String, float[]> data : datas){
            diku[index] = data.getValue();
            index ++;
        }

        float[][] queryList = new float[1][512];
        queryList[0] = feature;
        ArrayList<CompareResult> array = FeatureCompared.faceCompareFloat(0, diku, queryList, sim * 100);
        if(array.size() == 0){
            return new SearchResult();
    }
        ArrayList<FaceFeatureInfo> list = array.get(0).getPictureInfoArrayList();
        SearchResult.Record[] records = new SearchResult.Record[list.size()];
        index = 0;
        for(FaceFeatureInfo faceFeatureInfo : list){
            float score = faceFeatureInfo.getScore();
            String rowkey = datas.get(Integer.parseInt(faceFeatureInfo.getImageID())).getKey();
            records[index] = new SearchResult.Record(score, rowkey);
            index ++;
        }
        SearchResult result = new SearchResult(records);
        logger.info("The size of Compared is : " + records.length);
        result.sortBySim();
        logger.info("The time comparing used is : " + (System.currentTimeMillis() - start));
        return result;
    }

    public SearchResult compareSecondTheSamePerson(List<float[]> features, float sim, List<Pair<String, float[]>> datas){
        Long start = System.currentTimeMillis();
        float[][] diku = new float[datas.size()][512];
        int index = 0;
        for(Pair<String, float[]> data : datas){
            diku[index] = data.getValue();
            index ++;
        }

        float[][] queryList = features.toArray(new float[features.size()][512]);
        ArrayList<CompareResult> array = FeatureCompared.faceCompareFloat(0, diku, queryList, sim * 100);
        Map<String, Float> temp = new HashMap<>();
        for(CompareResult compareResult : array){
            ArrayList<FaceFeatureInfo> faceFeatureInfos = compareResult.getPictureInfoArrayList();
            for(FaceFeatureInfo faceFeatureInfo : faceFeatureInfos){
                String rowkey = datas.get(Integer.parseInt(faceFeatureInfo.getImageID())).getKey();
                float score = faceFeatureInfo.getScore();
                float scoreTemp = temp.get(rowkey);
                if(score < scoreTemp){
                    temp.put(rowkey, score);
                }
            }
        }
        Set<Map.Entry<String, Float>> entrySet = temp.entrySet();
        SearchResult.Record[] records = new SearchResult.Record[entrySet.size()];
        index = 0;
        for(Map.Entry<String, Float> entry : entrySet){
            records[index] = new SearchResult.Record(entry.getValue(), entry.getKey());
            index ++;
        }
        SearchResult result = new SearchResult(records);
        logger.info("The size of Compared is : " + records.length);
        result.sortBySim();
        logger.info("The time comparing used is : " + (System.currentTimeMillis() - start));
        return result;
    }

    public Map<String, SearchResult> compareSecondNotSamePerson(List<Feature> features, float sim, List<Pair<String, float[]>> datas){
        Long start = System.currentTimeMillis();
        Map<String, SearchResult> result = new HashMap<>();
        float[][] diku = new float[datas.size()][512];
        int index = 0;
        for(Pair<String, float[]> data : datas){
            diku[index] = data.getValue();
            index ++;
        }

        float[][] queryList = new float[features.size()][512];
        index = 0;
        for(Feature feature : features){
            queryList[index] = feature.getFeature2();
            index ++;
        }

        ArrayList<CompareResult> array = FeatureCompared.faceCompareFloat(0, diku, queryList, sim * 100);
        for(CompareResult compareResult : array){
            SearchResult.Record[] records = new SearchResult.Record[compareResult.getPictureInfoArrayList().size()];
            int recordIndex = 0;
            for(FaceFeatureInfo faceFeatureInfo : compareResult.getPictureInfoArrayList()){
                float score = faceFeatureInfo.getScore();
                String rowkey = datas.get(Integer.parseInt(faceFeatureInfo.getImageID())).getKey();
                records[recordIndex] = new SearchResult.Record(score, rowkey);
                recordIndex ++;
            }
            SearchResult searchResult = new SearchResult(records);
            searchResult.sortBySim();
            String id = features.get(Integer.parseInt(compareResult.getIndex())).getId();
            result.put(id, searchResult);
        }
        logger.info("The time second compare used is : " + (System.currentTimeMillis() - start));
        return result;
    }
}
