package com.hzgc.compare.worker.persistence;

import com.hzgc.compare.worker.conf.Config;

import java.io.OutputStream;

public class HDFSStreamCache extends StreamCache{
    private Config conf;

    public OutputStream getStream(String fileName) {
        return null;
    }
}
