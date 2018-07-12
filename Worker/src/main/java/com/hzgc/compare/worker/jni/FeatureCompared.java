package com.hzgc.compare.worker.jni;

import com.hzgc.compare.worker.common.FaceObject;
import com.hzgc.compare.worker.common.SearchResult;
import javafx.util.Pair;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class FeatureCompared {

    public static native List<String> compareFirst(List<Pair<String, byte[]>> dataToCompare, byte[] feature, int num);

//    public static native SearchResult compareSecond(List<FaceObject> data, float[] feature, float sim);


    public static SearchResult compareSecond(List<FaceObject> data, float[] currentFeature, float sim){
        List<SearchResult.Record> list = new ArrayList<>();

        for(FaceObject obj : data){
            float[] historyFeature = obj.getAttribute().getFeature();
            double similarityDegree = 0;
            double currentFeatureMultiple = 0;
            double historyFeatureMultiple = 0;
            if (currentFeature.length == 512 && historyFeature.length == 512) {
                for (int i = 0; i < currentFeature.length; i++) {
                    similarityDegree = similarityDegree + currentFeature[i] * historyFeature[i];
                    currentFeatureMultiple = currentFeatureMultiple + Math.pow(currentFeature[i], 2);
                    historyFeatureMultiple = historyFeatureMultiple + Math.pow(historyFeature[i], 2);
                }
                double tempSim = similarityDegree / Math.sqrt(currentFeatureMultiple) / Math.sqrt(historyFeatureMultiple);
                double actualValue = new BigDecimal((0.5 + (tempSim / 2)) * 100).
                        setScale(2, BigDecimal.ROUND_HALF_UP).
                        doubleValue();
                if (actualValue > sim) {
                    list.add(new SearchResult.Record(actualValue, obj));
                }
            }
        }
        return  new SearchResult(list.toArray(new SearchResult.Record[list.size()]));
    }
}
