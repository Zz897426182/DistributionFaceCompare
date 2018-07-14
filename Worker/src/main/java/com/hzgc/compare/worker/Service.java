package com.hzgc.compare.worker;

import com.hzgc.compare.demo.CompareParam;
import com.hzgc.compare.rpc.client.result.AllReturn;
import com.hzgc.compare.worker.common.SearchResult;

import java.util.List;

public interface Service {

    AllReturn<SearchResult> retrieval(CompareParam paramt);
}
