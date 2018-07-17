package com.hzgc.compare.worker.common;

public class Feature{
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
}