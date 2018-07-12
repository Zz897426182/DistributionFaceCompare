package com.hzgc.compare.demo;

import java.util.List;

public class CompareParam {
    private List<String> arg1List;
    private List<String> arg2List;
    private String dateStart;
    private String dateEnd;
    private byte[] feature1;
    private float[] feature2;
    private float sim;
    private int resultCount;

    public CompareParam(List<String> arg1List, List<String> arg2List, String dateStart, String dateEnd, byte[] feature1, float[] feature2, float sim, int resultCount) {
        this.arg1List = arg1List;
        this.arg2List = arg2List;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.feature1 = feature1;
        this.feature2 = feature2;
        this.sim = sim;
        this.resultCount = resultCount;
    }

    public List<String> getArg1List() {
        return arg1List;
    }

    public List<String> getArg2List() {
        return arg2List;
    }

    public String getDateStart() {
        return dateStart;
    }

    public String getDateEnd() {
        return dateEnd;
    }

    public byte[] getFeature1() {
        return feature1;
    }

    public float[] getFeature2() {
        return feature2;
    }

    public float getSim() {
        return sim;
    }

    public int getResultCount() {
        return resultCount;
    }
}
