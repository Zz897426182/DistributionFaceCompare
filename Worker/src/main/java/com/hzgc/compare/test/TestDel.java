package com.hzgc.compare.test;


import com.hzgc.compare.worker.persistence.task.TimeToCheckFile;

import java.util.Timer;

public class TestDel {
    public static void main(String[] args) {
        new Timer().schedule(new TimeToCheckFile(),1000);
    }
}
