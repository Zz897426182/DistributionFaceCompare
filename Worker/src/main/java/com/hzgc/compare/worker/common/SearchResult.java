package com.hzgc.compare.worker.common;

import com.hzgc.compare.rpc.client.result.AllReturn;

import java.util.Arrays;

public class SearchResult {
    private static Integer size = 1000;
    private Record[] records;

    public SearchResult(){
        records = new Record[0];
    }

    public SearchResult(Record[] records){
        this.records = records;
    }

    /**
     * 获取数据的前num条，封装成新的SearchResult
     * @param num
     * @return
     */
    public SearchResult take(int num){
        Record[] recordsTemp = new Record[num];
        System.arraycopy(records, 0, new Record[num], 0, num);
        return new SearchResult(recordsTemp);
    }

    /**
     * 将当前的records根据Sim排序
     */
    public void sortBySim(){ //TODO 选择合适的排序
        Arrays.sort(records);
//        quickSort(records, 0, records.length);
    }

    /**
     * 将多个SearchResult的 records 合并，并根据Sim排序
     * @param result
     * @return
     */
    public void merge(SearchResult result){
        if(records.length == 0) {
            records = result.getRecords();
        } else {
            Record[] arr1 = records;
            Record[] arr2 = result.getRecords();
            Record[] arr3 =  new Record[arr2.length + arr1.length];
            int i , j , k;
            i = j = k = 0;
            while (i < arr1.length && j < arr2.length){
                if(arr1[i].compareTo(arr2[j]) > 0){
                    arr3[k++] = arr1[i++];
                } else {
                    arr3[k++] = arr2[j++];
                }
            }
            while (i < arr1.length){
                arr3[k++] = arr1[i++];
            }
            while (j < arr2.length){
                arr3[k++] = arr2[j++];
            }
            arr1 = arr3;
        }
    }

    private void quickSort(Record[] records , int begin, int end){
        int tbegin = begin;
        int tend = end;
    }

    public Record[] getRecords(){
        return records;
    }


    public static class Record implements  Comparable<Record>{
        double sim;
        Object body;
        public Record(double sim, Object body){
            this.sim = sim;
            this.body = body;
        }

        public double getKey(){
            return sim;
        }

        public Object getValue(){
            return body;
        }

        public int compareTo(Record o) {
            return Double.compare(this.sim, o.sim);
        }
    }
}
