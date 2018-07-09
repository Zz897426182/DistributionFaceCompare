package com.hzgc.compare.worker;

import java.util.List;

public interface Service {
    String retrieval(List<String> arg1List, String arg2RangStart, String arg2RangEnd, byte[] feature1, float[] feature2, int resultCount);
    String retrieval(List<String> arg1List, String arg2RangStart, String arg2RangEnd, byte[] feature1, float[] feature2);

    String retrieval(List<String> arg1List, List<String> arg2List, byte[] feature1, float[] feature2);
    String retrieval(List<String> arg1List, List<String> arg2List, byte[] feature1, float[] feature2, int resultCount);
}
