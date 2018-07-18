package com.hzgc.compare.worker.jni;

import java.util.ArrayList;

public class CompareResult {
    //queryList下标
    private String index;

    //topN 图片信息数据
    private ArrayList<FaceFeatureInfo> faceFeatureInfoArrayList;

    public String getIndex() { return index; }

    public void setIndex(String index) { this.index = index; }

    public ArrayList<FaceFeatureInfo> getPictureInfoArrayList() { return faceFeatureInfoArrayList; }

    public void setPictureInfoArrayList(ArrayList<FaceFeatureInfo> faceFeatureInfoArrayList) { this.faceFeatureInfoArrayList = faceFeatureInfoArrayList; }
}
