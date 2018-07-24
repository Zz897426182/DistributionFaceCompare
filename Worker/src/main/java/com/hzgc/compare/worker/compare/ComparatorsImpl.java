package com.hzgc.compare.worker.compare;

import com.hzgc.compare.worker.common.FaceObject;
import com.hzgc.compare.worker.common.Feature;
import com.hzgc.compare.worker.common.SearchResult;
import com.hzgc.compare.worker.common.Triplet;
import com.hzgc.compare.worker.conf.Config;
import com.hzgc.compare.worker.jni.CompareResult;
import com.hzgc.compare.worker.jni.FaceFeatureInfo;
import com.hzgc.compare.worker.jni.JNINativeMethod;
import com.hzgc.compare.worker.memory.cache.MemoryCacheImpl;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ComparatorsImpl implements Comparators{
    private static final Logger logger = LoggerFactory.getLogger(ComparatorsImpl.class);
    private Config conf;

    public ComparatorsImpl() {
        this.conf = Config.getConf();
    }


    public List<Pair<String, byte[]>> filter(List<String> arg1List, String arg2, String dateStart, String dateEnd) {
        List<Pair<String, byte[]>> result = new ArrayList<>();
        Map<Triplet<String, String, String>, List<Pair<String, byte[]>>> cacheRecords =
                MemoryCacheImpl.<String, String, byte[]>getInstance().getCacheRecords();
//        Set<Triplet<String, String, String>> temp = new HashSet<>();
//        temp.addAll(cacheRecords.keySet());
        Iterator<Triplet<String, String, String>> iterator =  cacheRecords.keySet().iterator();
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
        return result;
    }

    @Override
    public List<Pair<String, byte[]>> filter(List<String> arg1List, String arg2RangStart, String arg2RangEnd, String dateStart, String dateEnd) {
        return null;
    }

    @Override
    public List<String> compareFirst(byte[] feature, int num, List<Pair<String, byte[]>> data) {
        Long start = System.currentTimeMillis();
//        FeatureCompared.compareFirst(data, feature, num);
        List<String> rowkeys = new ArrayList<>();
        byte[][] diku = new byte[data.size()][64];
        int index = 0;
        for(Pair<String, byte[]> pair : data){
            diku[index] = pair.getValue();
            index ++;
        }

        byte[][] queryList = new byte[1][64];
        queryList[0] = feature;
        ArrayList<CompareResult> array =JNINativeMethod.faceCompareBit(0, diku, queryList, num);
        for(FaceFeatureInfo featureInfo : array.get(0).getPictureInfoArrayList()){
            String rowkey = data.get(Integer.parseInt(featureInfo.getImageID())).getKey();
            rowkeys.add(rowkey);
        }
        logger.info("The time first compare used is : " + (System.currentTimeMillis() - start));
        return rowkeys;
    }

    public List<String> compareFirstNotSamePerson(List<Feature> features, int num, List<Pair<String, byte[]>> data){
        Long start = System.currentTimeMillis();
        Map<Feature, List<String>> result = new HashMap<>();
        byte[][] diku = new byte[data.size()][64];
        int index = 0;
        for(Pair<String, byte[]> pair : data){
            diku[index] = pair.getValue();
            index ++;
        }

        byte[][] queryList = new byte[features.size()][64];
        index = 0;
        for (Feature feature : features){
            queryList[index] = feature.getFeature1();
            index ++;
        }
        ArrayList<CompareResult> array =JNINativeMethod.faceCompareBit(0, diku, queryList, num);
        List<String> rowkeys = new ArrayList<>();
        for(CompareResult compareResult : array){
            for(FaceFeatureInfo faceFeatureInfo : compareResult.getPictureInfoArrayList()){
                String rowkey = data.get(Integer.parseInt(faceFeatureInfo.getImageID())).getKey();
                if(!rowkeys.contains(rowkey)) {
                    rowkeys.add(rowkey);
                }
            }
        }
        logger.info("The time first compare used is : " + (System.currentTimeMillis() - start));
        return rowkeys;
    }

    @Override
    public List<String> compareFirstTheSamePerson(List<byte[]> features, int num, List<Pair<String, byte[]>> data) {
        Long start = System.currentTimeMillis();
        List<String> result = new ArrayList<>();
        byte[][] diku = new byte[data.size()][64];
        int index = 0;
        for(Pair<String, byte[]> pair : data){
            diku[index] = pair.getValue();
            index ++;
        }

        byte[][] queryList = new byte[features.size()][64];
        index = 0;
        for (byte[] feature : features){
            queryList[index] = feature;
            index ++;
        }
        Set<String> set = new HashSet<>();
        index = 0;
        ArrayList<CompareResult> array =JNINativeMethod.faceCompareBit(0, diku, queryList, num);
        for(CompareResult compareResult : array){
            List<String> rowkeys = new ArrayList<>();
            for(FaceFeatureInfo faceFeatureInfo : compareResult.getPictureInfoArrayList()){
                String rowkey = data.get(Integer.parseInt(faceFeatureInfo.getImageID())).getKey();
                rowkeys.add(rowkey);
            }
            if(index == 0) {
                set.addAll(rowkeys);
            }else {
                set.retainAll(rowkeys);
            }
            index ++;
        }
        result.addAll(set);
        logger.info("The time first compare used is : " + (System.currentTimeMillis() - start));
        return result;
    }

    @Override
    public SearchResult compareSecond(float[] feature, float sim, List<FaceObject> datas) {
        Long start = System.currentTimeMillis();
        float[][] diku = new float[datas.size()][512];
        int index = 0;
        for(FaceObject faceobj : datas){
            diku[index] = faceobj.getAttribute().getFeature();
            index ++;
        }

        float[][] queryList = new float[1][512];
        queryList[0] = feature;

        ArrayList<CompareResult> array = JNINativeMethod.faceCompareFloat(0, diku, queryList, 30);
        List<FaceFeatureInfo> list = array.get(0).getPictureInfoArrayList();
        SearchResult.Record[] records = new SearchResult.Record[list.size()];
        index = 0;
        for(FaceFeatureInfo faceFeatureInfo : list){
            float score = faceFeatureInfo.getScore();
            if(score < sim){
                continue;
            }
            FaceObject body = datas.get(Integer.parseInt(faceFeatureInfo.getImageID()));
            SearchResult.Record record = new SearchResult.Record(score, body);
            records[index] = record;
            index ++;
        }
        SearchResult result = new SearchResult(records);
        result.sortBySim();
        logger.info("The time second compare used is : " + (System.currentTimeMillis() - start));
        return result;
    }

    @Override
    public SearchResult compareSecondTheSamePerson(List<float[]> features, float sim, List<FaceObject> datas) {
        Long start = System.currentTimeMillis();
        float[][] diku = new float[datas.size()][512];
        int index = 0;
        for(FaceObject faceobj : datas){
            diku[index] = faceobj.getAttribute().getFeature();
            index ++;
        }

        float[][] queryList = new float[1][512];
        index = 0;
        for(float[] feature : features){
            queryList[index] = feature;
            index ++;
        }
        ArrayList<CompareResult> array = JNINativeMethod.faceCompareFloat(0, diku, queryList, 30);
        Map<FaceObject, Float> temp = new HashMap<>();
        for(CompareResult compareResult : array){
            ArrayList<FaceFeatureInfo> faceFeatureInfos = compareResult.getPictureInfoArrayList();
            for(FaceFeatureInfo faceFeatureInfo : faceFeatureInfos){
                FaceObject face = datas.get(Integer.parseInt(faceFeatureInfo.getImageID()));
                float score = faceFeatureInfo.getScore();
                if(score < sim){
                    continue;
                }
                float scoreTemp = temp.get(face);
                if(score < scoreTemp){
                    temp.put(face, score);
                }
            }
        }
        Set<Map.Entry<FaceObject, Float>> entrySet = temp.entrySet();
        SearchResult.Record[] records = new SearchResult.Record[entrySet.size()];
        index = 0;
        for(Map.Entry<FaceObject, Float> entry : entrySet){
            records[index] = new SearchResult.Record(entry.getValue(), entry.getKey());
            index ++;
        }
        SearchResult result = new SearchResult(records);
        result.sortBySim();
        logger.info("The time second compare used is : " + (System.currentTimeMillis() - start));
        return result;
    }

    @Override
    public Map<String, SearchResult> compareSecondNotSamePerson(List<Feature> features, float sim, List<FaceObject> datas) {
        Long start = System.currentTimeMillis();
        Map<String, SearchResult> result = new HashMap<>();
        float[][] diku = new float[datas.size()][512];
        int index = 0;
        for(FaceObject faceobj : datas){
            diku[index] = faceobj.getAttribute().getFeature();
            index ++;
        }

        float[][] queryList = new float[features.size()][512];
        index = 0;
        for(Feature feature : features){
            queryList[index] = feature.getFeature2();
            index ++;
        }
        ArrayList<CompareResult> array = JNINativeMethod.faceCompareFloat(0, diku, queryList, 30);
        for(CompareResult compareResult : array){
            SearchResult.Record[] records = new SearchResult.Record[compareResult.getPictureInfoArrayList().size()];
            int recordIndex = 0;
            for(FaceFeatureInfo faceFeatureInfo : compareResult.getPictureInfoArrayList()){
                float score = faceFeatureInfo.getScore();
                if(score < sim){
                    continue;
                }
                FaceObject faceObject = datas.get(Integer.parseInt(faceFeatureInfo.getImageID()));
                records[recordIndex] = new SearchResult.Record(score, faceObject);
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
