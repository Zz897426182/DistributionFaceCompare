package com.hzgc.compare.worker;

import com.hzgc.compare.worker.common.SearchResult;

import java.util.List;

public interface Service {
    SearchResult retrieval(List<String> arg1List, String arg2RangStart, String arg2RangEnd, byte[] feature1, float[] feature2, int resultCount);


    SearchResult retrieval(List<String> arg1List, List<String> arg2List, String dateStart, String dateEnd,
                           byte[] feature1, float sim1, float[] feature2, float sim2, int resultCount);
}
