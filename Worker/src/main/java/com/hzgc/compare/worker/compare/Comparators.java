package com.hzgc.compare.worker.compare;

import com.hzgc.compare.worker.common.FaceObject;
import com.hzgc.compare.worker.common.SearchResult;
import javafx.util.Pair;

import java.util.List;

/**
 * 对比两次，第一次用比byte[]做对比，得到对比结果，然后读取HBase得到特征值，用feature去对比
 */
public interface Comparators {

    /**
     * 先对内存数据进行过滤，根据参数1和参数2
     * @param arg1List
     * @param arg2
     * @param dateStart
     * @param dateEnd
     * @return Record<rowkey, feature>
     */
    List<Pair<String, byte[]>> filter(List<String> arg1List, String arg2, String dateStart, String dateEnd);

    /**
     * 先对内存数据进行过滤，根据参数1和参数2的取值范围
     * @param arg1List
     * @param arg2RangStart
     * @param arg2RangEnd
     * @param dateStart
     * @param dateEnd
     * @return
     */
    List<Pair<String, byte[]>> filter(List<String> arg1List, String arg2RangStart, String arg2RangEnd, String dateStart, String dateEnd);

    /**
     * 若数据量过大则需要第一次对比
     * @param feature
     * @param sim
     * @param data
     * @return List<rowkey>
     */
    List<String> compareFirst(byte[] feature, float sim, List<Pair<String, byte[]>> data);

    /**
     * 把从HBase读取的数据，进行第二次对比
     * @param feature
     * @param sim
     * @param data
     * @return
     */
    SearchResult compareSecond(float[] feature, float sim, List<FaceObject> data);
}
