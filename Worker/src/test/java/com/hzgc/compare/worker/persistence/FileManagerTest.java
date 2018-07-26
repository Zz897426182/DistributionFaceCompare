package com.hzgc.compare.worker.persistence;

import com.hzgc.compare.worker.CreateRecordToBuffer;
import com.hzgc.compare.worker.common.Quintuple;
import com.hzgc.compare.worker.memory.cache.MemoryCacheImpl;
import com.hzgc.compare.worker.persistence.task.TimeToCheckFile;
import org.junit.Test;

import java.util.List;
import java.util.Timer;

public class FileManagerTest {

    /*
    * 数据存储到磁盘中
    * */
    @Test
    public void testSaveData() throws Exception{
        CreateRecordToBuffer.createRecords(100);
        List <Quintuple <String, String, String, String, float[]>> buffer =
                MemoryCacheImpl.<String,String, float[]>getInstance().getBuffer();
        FileManager fileManager = new LocalFileManager <>();
        fileManager.flush(buffer);
    }

    /*
    * 数据加载到内存中
    * */
    @Test
    public void testLoadData(){
        FileReader fileReader = new FileReader();
        fileReader.loadRecordFromLocal();
    }

    /*
    * 删除过期文件
    * */
    @Test
    public void testDelData(){
        new Timer().schedule(new TimeToCheckFile(),1000);
    }
}
