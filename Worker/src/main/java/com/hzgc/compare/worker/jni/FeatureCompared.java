package com.hzgc.compare.worker.jni;

import com.hzgc.compare.worker.common.FaceObject;
import com.hzgc.compare.worker.common.SearchResult;
import javafx.util.Pair;

import java.util.List;

public class FeatureCompared {

    public static native List<String> compareFirst(List<Pair<String, byte[]>> dataToCompare, byte[] feature, float sim);

    public static native SearchResult compareSecond(List<FaceObject> data, float[] feature, float sim);
}
