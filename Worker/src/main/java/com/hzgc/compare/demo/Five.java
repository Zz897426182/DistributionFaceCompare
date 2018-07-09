package com.hzgc.compare.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Five {
    private String mingcheng;
    private List<String> shouzhijihe;
    private Map<String, String> jiheduiyingmingcheng;
    private int xuhao;

    public Five() {
        this.mingcheng = "zhaozhe";
        List<String> list = new ArrayList<String>();
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");
        list.add("5");
        this.shouzhijihe = list;
        Map<String, String> map = new HashMap<>();
        map.put("1", "damuzhi");
        map.put("2", "shizhi");
        map.put("3", "zhongzhi");
        map.put("4", "wumingzhi");
        map.put("5", "xiaomuzhi");
        this.jiheduiyingmingcheng = map;
        this.xuhao = 1;
    }

    public Five(String mingcheng, List<String> shouzhijihe, Map<String, String> jiheduiyingmingcheng, int xuhao) {
        this.mingcheng = mingcheng;
        this.shouzhijihe = shouzhijihe;
        this.jiheduiyingmingcheng = jiheduiyingmingcheng;
        this.xuhao = xuhao;
    }

    public String getMingcheng() {
        return mingcheng;
    }

    public void setMingcheng(String mingcheng) {
        this.mingcheng = mingcheng;
    }

    public List<String> getShouzhijihe() {
        return shouzhijihe;
    }

    public void setShouzhijihe(List<String> shouzhijihe) {
        this.shouzhijihe = shouzhijihe;
    }

    public Map<String, String> getJiheduiyingmingcheng() {
        return jiheduiyingmingcheng;
    }

    public void setJiheduiyingmingcheng(Map<String, String> jiheduiyingmingcheng) {
        this.jiheduiyingmingcheng = jiheduiyingmingcheng;
    }

    public int getXuhao() {
        return xuhao;
    }

    public void setXuhao(int xuhao) {
        this.xuhao = xuhao;
    }
}
