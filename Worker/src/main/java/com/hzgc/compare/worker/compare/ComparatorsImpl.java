package com.hzgc.compare.worker.compare;

import com.hzgc.compare.worker.common.FaceObject;
import com.hzgc.compare.worker.common.SearchResult;
import com.hzgc.compare.worker.common.Triplet;
import com.hzgc.compare.worker.conf.Config;
import com.hzgc.compare.worker.jni.FeatureCompared;
import com.hzgc.compare.worker.memory.cache.MemoryCacheImpl1;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ComparatorsImpl implements Comparators{
    private Config conf;

    public ComparatorsImpl() {
        this.conf = Config.getConf();
    }

    @Override
    public List<Pair<String, byte[]>> filter(List<String> arg1List, String arg2, String dateStart, String dateEnd) {
        List<Pair<String, byte[]>> result = new ArrayList<>();
        Map<Triplet<String, String, String>, List<Pair<String, byte[]>>> cacheRecords =
                MemoryCacheImpl1.getInstance(conf).getCacheRecords();
        Iterator<Triplet<String, String, String> > iterator =  cacheRecords.keySet().iterator();
        Long start = System.currentTimeMillis();
        for(String arg1 : arg1List) {
            while (iterator.hasNext()) {
                Triplet<String, String, String> key = iterator.next();
                String key1 = key.getFirst();
                String key2 = key.getSecond();
                String key3 = key.getThird();
                if ((key1 == null || key1.equals(arg1)) &&
                        (key2 == null || key2.equals(arg2)) &&
                        key3.compareTo(dateStart) > 0 &&
                        key3.compareTo(dateEnd) < 0) {
                    result.addAll(cacheRecords.get(key));
                }
            }
        }

        System.out.println("The time used to filter is : " + (System.currentTimeMillis() - start));
        return result;
    }

    @Override
    public List<Pair<String, byte[]>> filter(List<String> arg1List, String arg2RangStart, String arg2RangEnd, String dateStart, String dateEnd) {
        return null;
    }

    @Override
    public List<String> compareFirst(byte[] feature,  int num, List<Pair<String, byte[]>> data) {
        Long start = System.currentTimeMillis();
        System.out.println("The time first compare used is : " + (System.currentTimeMillis() - start));
        return FeatureCompared.compareFirst(data, feature, num);
    }

    @Override
    public SearchResult compareSecond(float[] feature, float sim, List<FaceObject> data) {
        Long start = System.currentTimeMillis();
        SearchResult result = FeatureCompared.compareSecond(data, feature, sim);
        result.sortBySim();
        System.out.println("The time second compare used is : " + (System.currentTimeMillis() - start));
        return result;
    }
}
