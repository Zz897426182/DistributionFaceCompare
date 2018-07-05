package com.hzgc.compare.worker.jni;

import com.hzgc.compare.worker.common.SearchResult;
import javafx.util.Pair;
import java.util.List;

public class FeatureCompared {

    public static native SearchResult compare(List<Pair<String, byte[]>> dataToCompare, byte[] feature, float sim);

    public static native float featureCompare(List<Pair<String, float[]>> data, float[] feature, float sim);
}
