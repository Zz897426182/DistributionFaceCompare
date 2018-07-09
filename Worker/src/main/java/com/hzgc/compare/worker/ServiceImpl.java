package com.hzgc.compare.worker;

import com.hzgc.compare.rpc.server.annotation.RpcService;
import java.util.List;

@RpcService(Service.class)
public class ServiceImpl implements Service{
    public String retrieval(List<String> arg1List, String arg2RangStart,
                            String arg2RangEnd, byte[] feature1, float[] feature2, int resultCount){
        return null;
    }
    public String retrieval(List<String> arg1List, String arg2RangStart, String arg2RangEnd, byte[] feature1, float[] feature2){
        return null;
    }

    public String retrieval(List<String> arg1List, List<String> arg2List, byte[] feature1, float[] feature2){
        return null;
    }
    public String retrieval(List<String> arg1List, List<String> arg2List, byte[] feature1, float[] feature2, int resultCount){
        return null;
    }
}
