package com.hzgc.compare.worker.common;

import java.util.List;

public class CompareParam {
    private List<String> arg1List;
    private List<String> arg2List;
    private String dateStart;
    private String dateEnd;
    private List<Feature> features;
    private float sim;
    private int resultCount;
    private boolean isTheSamePerson;

    public CompareParam(List<String> arg1List, List<String> arg2List, String dateStart, String dateEnd,
                        List<Feature> features, float sim, int resultCount, boolean isTheSamePerson) {
        this.arg1List = arg1List;
        this.arg2List = arg2List;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.features = features;
        this.isTheSamePerson = isTheSamePerson;
        this.sim = sim;
        this.resultCount = resultCount;
    }

    public List<String> getArg1List() {
        return arg1List;
    }

    public List<String> getArg2List() {
        return arg2List;
    }

    public String getDateStart() {
        return dateStart;
    }

    public String getDateEnd() {
        return dateEnd;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public boolean isTheSamePerson() {
        return isTheSamePerson;
    }

    public float getSim() {
        return sim;
    }

    public int getResultCount() {
        return resultCount;
    }
}
