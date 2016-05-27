package com.hongming.test.dowloaddemo;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.hongming.test.dowloaddemo.model.FileInfo;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by gongxm on 16/5/26.
 */
public class DownloadService extends Service {

    public final String TAG = "Eric";
    //文件保存路径
    public static String FilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
            + "downloads/";

    public final String ServiceName = "downloadservice";

    public static final String ACTION_START = "action_start";
    public static final String ACTION_STOP = "action_stop";
    public static final int MSG_INIT = 1;
    public DownloadTask downloadTask = null;
    private int DownloadIndex = 0;// 下载标记为

    public List<FileInfo> fileinfoLst = new ArrayList<>();
    initThread minitthred = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        if (!new File(FilePath).exists()) {
            new File(FilePath).mkdirs();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (intent.getAction() == ACTION_START) {
                fileinfoLst = (List<FileInfo>) intent.getSerializableExtra("files");
                Log.i(TAG, "下载服务:" + "文件数目" + fileinfoLst.size());
                DownloadTask();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    Handler mhandle = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (MSG_INIT == msg.what) {
                minitthred = null;
                FileInfo mfile = (FileInfo) msg.obj;
                Log.i(TAG, "开始下载:" + mfile.toString());
                // 启动下载任务
                downloadTask = null;
                downloadTask = new DownloadTask(DownloadService.this, mfile);
                downloadTask.downloadcom = new DownloadTask.DownloadCompment() {
                    @Override
                    public void DownloadFinished(int flag, String message) {
                        downloadTask.Stop();
                        String mess = "";
                        if (flag == DownloadTask.DownSucceed) {
                            mess = "下载成功" + message;
                        } else {
                            mess = "下载失败:" + message;
                        }
                        Intent mintent = new Intent();
                        mintent.setAction(MainActivity.ACTION_UPDATE);
                        mintent.putExtra("finished", mess);
                        getApplicationContext().sendBroadcast(mintent);
                        DownloadTask();
                    }
                };
                downloadTask.download();
//                DownloadTask.getInstance(DownloadService.this, mfile, new downloadListener()).download();
            }
        }
    };

    class downloadListener implements DownloadTask.DownloadCompment {

        @Override
        public void DownloadFinished(int flag, String message) {
            String mess = "";
            if (flag == DownloadTask.DownSucceed) {
                mess = "下载成功";
            } else {
                mess = "下载失败:" + message;
            }
            Intent mintent = new Intent();
            mintent.setAction(MainActivity.ACTION_UPDATE);
            mintent.putExtra("finished", mess);
            getApplicationContext().sendBroadcast(mintent);
            DownloadTask();
        }
    }

    private void DownloadTask() {
        Log.i(TAG, "开始创建下载进程" + DownloadIndex);
        if (DownloadIndex < fileinfoLst.size()) {
            minitthred = new initThread(fileinfoLst.get(DownloadIndex));
            minitthred.start();
            DownloadIndex++;
        } else {
            Log.i("Eric", "Start:" + new Date().getTime() + "");
            Log.i("Eric", "下载服务停止");
            stopSelf();
        }
    }

    class initThread extends Thread {
        private FileInfo mfileinfo;

        public initThread(FileInfo _mfileinfo) {
            mfileinfo = _mfileinfo;
        }

        @Override
        public void run() {
            super.run();
            HttpURLConnection conn = null;
            RandomAccessFile ref = null;
            File newfile = null;
            try {
                // 链接网络文件
                URL mulr = new URL(mfileinfo.FileUrl);
                conn = (HttpURLConnection) mulr.openConnection();
                conn.setConnectTimeout(5000);
                conn.setRequestMethod("GET");
                int length = -1;
                // 获取文件长度
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK || conn.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) {
                    length = conn.getContentLength();
                }
                if (length <= 0) {
                    return;
                }
//                File dir = new File(FilePath);
//                if (!dir.exists()) {
//                    dir.mkdirs();
//                }
                // 本地创建文件
                newfile = new File(FilePath + mfileinfo.FileName);
                ref = new RandomAccessFile(newfile, "rw");
                ref.setLength(length);
                mfileinfo.FileSize = length;
                mhandle.obtainMessage(MSG_INIT, mfileinfo).sendToTarget();
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("Eric", e.toString());
            } finally {
                try {
                    ref.close();
                    newfile = null;
                    conn = null;
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}
