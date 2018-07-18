package com.hzgc.compare.worker;

import com.hzgc.compare.worker.common.CompareParam;
import com.hzgc.compare.rpc.client.result.AllReturn;
import com.hzgc.compare.worker.common.SearchResult;

import java.util.List;
import java.util.Map;

public interface Service {

    /**
     * 单图片检索
     * @param paramt
     * @return
     */
    AllReturn<SearchResult> retrievalOnePerson(CompareParam paramt);

    /**
     * 多图片单人检索
     * @param paramt
     * @return
     */
    AllReturn<SearchResult> retrievalSamePerson(CompareParam paramt);

    /**
     * 多图片多人检索
     * @param paramt
     * @return
     */
    AllReturn<Map<String, SearchResult>> retrievalNotSamePerson(CompareParam paramt);

    void stopTheWorker();
}
