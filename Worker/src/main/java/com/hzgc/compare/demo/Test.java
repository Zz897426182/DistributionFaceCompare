package com.hzgc.compare.demo;

import com.hzgc.compare.worker.util.FaceObjectUtil;


public class Test {
    public static void main(String[] args) {
        float[] data = new float[]{1.0f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 20f};
        String json = FaceObjectUtil.arrayToJson(data);
        System.out.println(json);
        float[] res = FaceObjectUtil.jsonToArray(json);
        System.out.println(res);
    }
}
