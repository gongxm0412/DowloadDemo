package com.hongming.test.dowloaddemo.model;

public class ThreadInfo {
    public int theadid;
    public String url;
    public int start;
    public int end;
    /**
     * 完成字节
     */
    public int finished;

    @Override
    public String toString() {
        String mes = "theadid:" + theadid
                + "url:" + url
                + "start:" + start
                + "end:" + end
                + "finished:" + finished;
        return mes;
    }
}
