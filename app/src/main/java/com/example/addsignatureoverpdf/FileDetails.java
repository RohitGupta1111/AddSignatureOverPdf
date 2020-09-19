package com.example.addsignatureoverpdf;

import android.graphics.Bitmap;

import java.util.Date;

public class FileDetails {
    private String fileName;
    private Date fileModifiedDate;
    private Bitmap fileBitmap;

    FileDetails(String fileName, Date fileModifiedDate, Bitmap fileBitmap) {
        this.fileName = fileName;
        this.fileModifiedDate = fileModifiedDate;
        this.fileBitmap = fileBitmap;
    }

    public Bitmap getFileBitmap() {
        return fileBitmap;
    }

    public Date getFileModifiedDate() {
        return fileModifiedDate;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileBitmap(Bitmap fileBitmap) {
        this.fileBitmap = fileBitmap;
    }

    public void setFileModifiedDate(Date fileModifiedDate) {
        this.fileModifiedDate = fileModifiedDate;
    }

    public void setFileName(String filePath) {
        this.fileName = filePath;
    }
}

