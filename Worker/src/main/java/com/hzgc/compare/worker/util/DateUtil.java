package com.hzgc.compare.worker.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DateUtil {
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public static List<String> getPeriod(String start, String end, int daysPerThread) throws ParseException {
        int days = daysPerThread;
        List<String> list = new ArrayList<>();
        long startTime = sdf.parse(start).getTime();
        long endTime = sdf.parse(end).getTime();
        String time1 = start;
        while (startTime < endTime){
            startTime += 1000L * 60 * 60 * 24 * (days - 1);
            startTime = startTime < endTime ? startTime : endTime;
            String time2 = sdf.format(new Date(startTime));
            list.add(time1 + "," + time2);
            startTime += 1000L * 60 * 60 * 24;
            time1 = sdf.format(new Date(startTime));
        }
        return list;
    }
}
