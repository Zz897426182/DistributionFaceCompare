package com.hzgc.compare.test;

import com.hzgc.compare.worker.persistence.FileReader;

public class TestLoadFile {
    public static void main(String[] args) {
        FileReader fileReader = new FileReader();
        fileReader.loadRecordFromLocal();
    }
}
