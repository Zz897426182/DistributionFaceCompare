package com.hzgc.compare.worker.common;

import java.io.Serializable;

public class FaceAttribute implements Serializable {
    private float[] feature;
    private byte[] feature2;
    private int hairColor;
    private int hairStyle;
    private int gender;
    private int hat;
    private int tie;
    private int huzi;
    private int eyeglasses;
    private int sharpness;

    public FaceAttribute() {
    }

    public byte[] getFeature2() {
        return feature2;
    }

    public float[] getFeature() {
        return feature;
    }
}