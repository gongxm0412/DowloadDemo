package com.hongming.test.dowloaddemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.hongming.test.dowloaddemo.model.FileInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnStart, btnEnd;
    TextView tvLog;
    /**
     * 下载文件数目
     */
    List<FileInfo> fileLst = new ArrayList<>();

    public static final String ACTION_UPDATE = "action_update";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setContentView(R.layout.activity_main);

        btnStart = (Button) findViewById(R.id.btnStart);
        btnEnd = (Button) findViewById(R.id.btnEnd);
        tvLog = (TextView) findViewById(R.id.tvLog);

        btnStart.setOnClickListener(this);
        btnEnd.setOnClickListener(this);

        initData();

        //注册广播监听下载服务
        IntentFilter mfilter = new IntentFilter();
        mfilter.addAction(ACTION_UPDATE);
        registerReceiver(logreceiver, mfilter);

    }


    private void initData() {
        FileInfo mfileInfo = null;

        Resources res = getResources();
        String[] city = res.getStringArray(R.array.urls);

        for (int i = 0; i < city.length; i++) {
            mfileInfo = new FileInfo();
            mfileInfo.FileName = i + "_" + city[i];
            mfileInfo.FileUrl = "http://58.210.67.150:8000/EMWebServer/files/" + city[i];
            mfileInfo.isFinished = 0;
            mfileInfo.FileSize = 0;
            fileLst.add(mfileInfo);
//            addLog("文件序号(" + (i + 1) + ")" + mfileInfo.toString());
        }
        addLog("文件数目:" + fileLst.size());
    }

    //输出日志
    public void addLog(final String mes) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvLog.append(mes + "\n");
            }
        });
    }


    @Override
    public void onClick(View v) {
        if (v == btnStart) {

            Intent mintent = new Intent(MainActivity.this, DownloadService.class);
            mintent.setPackage(getPackageName());
            mintent.putExtra("files", (Serializable) fileLst);
            mintent.setAction(DownloadService.ACTION_START);
            startService(mintent);

        } else if (v == btnEnd) {

        }
    }


    BroadcastReceiver logreceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_UPDATE == intent.getAction()) {
                String mes = intent.getStringExtra("finished");
                addLog("下载量:" + mes);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(logreceiver);
    }
}
