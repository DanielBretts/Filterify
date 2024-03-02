package com.lirandaniel.filterify.model;

public class ImageData {

    private String img_bytes;

    public ImageData() {
    }

    public ImageData(String byteArray) {
        this.img_bytes = byteArray;
    }

    public String getByteArray() {
        return img_bytes;
    }

    public void setByteArray(String byteArray) {
        this.img_bytes = byteArray;
    }

    @Override
    public String toString() {
        return "ImageData{" +
                "img_bytes=" + img_bytes +
                '}';
    }
}