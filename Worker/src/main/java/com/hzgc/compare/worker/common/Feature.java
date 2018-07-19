package com.hzgc.compare.worker.common;

import java.util.Arrays;

public class Feature{
    private String id;
    private byte[] feature1;
    private float[] feature2;
    public Feature(byte[] feature1, float[] feature2){
        this.feature1 = feature1;
        this.feature2 = feature2;
    }

    public byte[] getFeature1() {
        return feature1;
    }

    public float[] getFeature2() {
        return feature2;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Feature{" +
                "id='" + id + '\'' +
                ", feature1=" + Arrays.toString(feature1) +
                ", feature2=" + Arrays.toString(feature2) +
                '}';
    }
}