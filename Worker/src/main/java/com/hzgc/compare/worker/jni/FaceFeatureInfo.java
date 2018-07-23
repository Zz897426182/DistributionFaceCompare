package com.hzgc.compare.worker.jni;

import java.io.Serializable;

public class FaceFeatureInfo implements Serializable {
    //图片ID
    private String imageID;

    //分数
    private Float score;

    //汉明距离
    private int dist;

    public String getImageID() { return imageID; }

    public void setImageID(String imageID) { this.imageID = imageID; }

    public Float getScore() { return score; }

    public void setScore(Float score) { this.score = score; }

    public int getDist() { return dist; }

    public void setDist(int dist) { this.dist = dist; }
}
