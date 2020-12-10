package ru.objects;

import java.lang.String;

public class ImageInfo {
    public Integer heigh;
    public Integer width;
    public String base64;
    public String index;

    public ImageInfo(String base64, String index, Integer width, Integer heigh) {
        this.heigh = heigh;
        this.width = width;
        this.base64 = base64;
        this.index = index;
    }
}