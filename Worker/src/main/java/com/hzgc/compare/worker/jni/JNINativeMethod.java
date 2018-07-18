package com.hzgc.compare.worker.jni;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;

public class JNINativeMethod implements Serializable {
    static {
        System.loadLibrary("FaceJNI");
    }

    /**
     * 人脸大图检测
     * @param retResult 执行结果
     * @param faceBigImageData 大图数据
     * @return 小图列表
     */
    public static native ArrayList<FaceSmallImageData> bigFaceFeatureCheck(int retResult, FaceBigImageData faceBigImageData);

    /**
     * 人脸特征提取
     * @param faceAttribute 人脸属性
     * @param imageData 图片数据
     */
    public static native int faceFeatureExtract(FaceAttribute faceAttribute, ImageData imageData);

    /**
     * 人脸比对粗筛
     * @param retResult 执行结果
     * @param diku  数据底库
     * @param queryList 图片数据
     * @param topN
     * @return
     */
    public static native ArrayList<CompareResult> faceCompareBit(int retResult, byte[][] diku, byte[][] queryList, int topN);

    /**
     * 人脸比对精筛
     * @param retResult 执行结果
     * @param diku  数据底库
     * @param queryList 图片数据
     * @param topN
     * @return
     */
    public static native ArrayList<CompareResult> faceCompareFloat(int retResult, float[][] diku, float[][] queryList, int topN);


}
