package com.hzgc.compare.worker.jni;

import java.io.Serializable;

public class ImageData implements Serializable {
    //图片数据流
    private byte[] bImageStream;

    //图片类型
    private Enum<ImageType> imageType;

    public byte[] getbImageStream() { return bImageStream; }

    public void setbImageStream(byte[] bImageStream) { this.bImageStream = bImageStream; }

    public Enum<ImageType> getImageType() { return imageType; }

    public void setImageType(Enum<ImageType> imageType) { this.imageType = imageType; }
}
