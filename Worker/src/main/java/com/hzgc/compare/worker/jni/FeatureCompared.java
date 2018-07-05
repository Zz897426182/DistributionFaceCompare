package com.hzgc.compare.worker.jni;

import com.hzgc.compare.worker.common.SearchResult;
import javafx.util.Pair;

import java.math.BigDecimal;
import java.util.List;

public class FeatureCompared {

    public static native SearchResult compare(List<Pair<String, byte[]>> dataToCompare, byte[] feature, float sim);

    public static float featureCompare(float[] currentFeature, float[] historyFeature) {
        double similarityDegree = 0.0D;
        double currentFeatureMultiple = 0.0D;
        double historyFeatureMultiple = 0.0D;

        for(int i = 0; i < currentFeature.length; ++i) {
            similarityDegree += (double)(currentFeature[i] * historyFeature[i]);
            currentFeatureMultiple += Math.pow((double)currentFeature[i], 2.0D);
            historyFeatureMultiple += Math.pow((double)historyFeature[i], 2.0D);
        }

        double tempSim = similarityDegree / Math.sqrt(currentFeatureMultiple) / Math.sqrt(historyFeatureMultiple);
        double actualValue = (new BigDecimal((0.5D + tempSim / 2.0D) * 100.0D)).setScale(2, 4).doubleValue();
        return actualValue >= 100.0D ? 100.0F : (float)actualValue;
    }
}
