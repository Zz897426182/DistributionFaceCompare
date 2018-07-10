package com.hzgc.compare.worker.persistence;

import com.hzgc.compare.worker.conf.Config;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class LocalStreamCache extends StreamCache {
    private Config conf;

    public OutputStream getStream(String fileName) {
        OutputStream stream = streams.get(fileName);
        if(stream == null){
            try {
                stream = new FileOutputStream(fileName, true);
                streams.put(fileName, stream);
                check();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return stream;
    }
}
