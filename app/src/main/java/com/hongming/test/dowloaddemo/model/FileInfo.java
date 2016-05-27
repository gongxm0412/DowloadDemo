package com.hongming.test.dowloaddemo.model;

import java.io.Serializable;

public class FileInfo implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -8865467509708563824L;
    public int id;
    public String FileUrl;
    public String FileName;
    public long FileSize;
    public int isFinished;

    @Override
    public String toString() {
        String mes = "FileUrl:" + FileUrl + "\\" + "FileName:" + FileName + "\\FileSize:" + FileSize + "\\isFinished："
                + isFinished;
        return mes;
    }
}
