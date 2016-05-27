package com.hongming.test.dowloaddemo;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.hongming.test.dowloaddemo.db.DBUtil;
import com.hongming.test.dowloaddemo.model.FileInfo;
import com.hongming.test.dowloaddemo.model.ThreadInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * 下载任务类
 *
 * @author gongxm
 */
public class DownloadTask {
    public FileInfo fileInfo;
    public Context context;
    public DBUtil dbutils;
    public long fileFinished = 0;
    public boolean isStop = false;
    public DownloadCompment downloadcom;
    public final static int DownSucceed = 1;
    public final static int DownFaile = -1;
    public final static int DownUnFinish = 0;
    private final String TAG = "downloadservice";

    downloadThread mthread = null;
//    private static DownloadTask curtask;

//    public  DownloadTask getInstance(Context mcon, FileInfo _mfile,DownloadCompment downloadcom) {
//        if (curtask == null) {
//            curtask = new DownloadTask(mcon, _mfile,downloadcom);
//        }
//        return curtask;
//    }

    public DownloadTask(Context mcon, FileInfo _mfile) {
        context = mcon;
        fileInfo = _mfile;
        dbutils = new DBUtil(context);
        mthread = null;
    }

    public void download() {
        // 读取下载信息
        List<ThreadInfo> info = dbutils.getThreads(fileInfo.FileUrl);
        ThreadInfo threadinfo = null;
        if (info.size() == 0) {
            threadinfo = new ThreadInfo();
            threadinfo.theadid = 0;
            threadinfo.start = 0;
            threadinfo.end = (int) fileInfo.FileSize;
            threadinfo.finished = 0;
            threadinfo.url = fileInfo.FileUrl;
        } else {
            threadinfo = info.get(0);
        }
        mthread = new downloadThread(threadinfo);
        mthread.start();
    }


    public void Stop() {
        mthread = null;
    }

    class downloadThread extends Thread {

        private ThreadInfo threadinfo;

        public downloadThread(ThreadInfo _theadinfo) {
            this.threadinfo = _theadinfo;
        }

        @Override
        public void run() {
            HttpURLConnection conn = null;
            RandomAccessFile raf = null;
            InputStream instrm = null;
            byte[] buffer = null;
            // 向数据库插入数据
            if (!dbutils.isExists(threadinfo.url, threadinfo.theadid)) {
                dbutils.insertThreadTable(threadinfo);
            }
            try {
                URL url = new URL(threadinfo.url);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("connection", "close");
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                // 设置下载位置
                int start = threadinfo.start + threadinfo.finished;
                conn.setRequestProperty("Range", "bytes=" + start + "-" + threadinfo.end);
                // Log.i(TAG, "END:" + threadinfo.end);
                fileFinished += threadinfo.finished;
                // 查找文件写入位置
                File mfile = new File(DownloadService.FilePath, fileInfo.FileName);
                raf = new RandomAccessFile(mfile, "rwd");
                raf.seek(start);
                // 开始下载
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK || conn.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) {
                    Intent mintent = new Intent();
                    mintent.setAction(MainActivity.ACTION_UPDATE);
                    instrm = conn.getInputStream();
                    buffer = new byte[1024 * 20];
                    int len = -1;
//                    long time = System.currentTimeMillis();
                    while ((len = instrm.read(buffer)) != -1) {
                        raf.write(buffer, 0, len);
                        // 把进度发送给activity
                        fileFinished += len;
                        // 500 毫秒更新一下 UI
//                        if (System.currentTimeMillis() - time > 500) {
//                            time = System.currentTimeMillis();
//                            mintent.putExtra("finished", String.valueOf(fileFinished * 100 /
//                                    fileInfo.FileSize) + "%");
//                            context.sendBroadcast(mintent);
//                        }

                        if (isStop) {
                            dbutils.updateTheadTable(threadinfo.theadid, Integer.parseInt(fileFinished + ""));
                            downloadcom.DownloadFinished(DownUnFinish, "Stop");
                            return;
                        }
                    }
                    dbutils.deleteThreadTable(threadinfo.theadid);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.w(TAG, e.getMessage());
                downloadcom.DownloadFinished(DownUnFinish, e.getMessage());
            } finally {
                conn.disconnect();
                downloadcom.DownloadFinished(DownSucceed, fileInfo.FileName);
                Log.i(TAG, "下载完成");
                try {
                    raf.close();
                    instrm.close();
                    conn = null;
                    raf = null;
                    instrm = null;
                    buffer = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }


    // 下载完成回调接口
    public interface DownloadCompment {
        void DownloadFinished(int flag, String message);
    }
}
