package com.hzgc.compare.worker.util;

import com.google.gson.Gson;
import com.hzgc.compare.worker.common.FaceObject;

public class FaceObjectUtil {
    private static Gson gson;
    static {
        gson = new Gson();
    }
    public static FaceObject jsonToObject(String json) {
        return gson.fromJson(json, FaceObject.class);
    }

    public static String ObjectTojson(Object obj){
        return gson.toJson(obj);
    }
}