package com.hzgc.compare.worker.compare.task;

import com.hzgc.compare.worker.common.SearchResult;

public abstract class CompareTask implements Runnable{
    boolean isEnd = false;
    SearchResult searchResult;


    public abstract SearchResult compare();

    @Override
    public void run() {
        try{
            searchResult = compare();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            isEnd = true;
        }
    }
}
