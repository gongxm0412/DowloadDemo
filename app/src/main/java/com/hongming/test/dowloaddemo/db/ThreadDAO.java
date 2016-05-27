package com.hongming.test.dowloaddemo.db;

import com.hongming.test.dowloaddemo.model.ThreadInfo;

import java.util.List;


public interface ThreadDAO {
	// 增加
	public void insertThreadTable(ThreadInfo threadinfo);

	//
	public void deleteThreadTable(int threadid);

	//
	public void updateTheadTable(int threadid, int finished);
	//查询下载信息
	public List<ThreadInfo> getThreads(String url);
	//
	public boolean isExists(String url, int taskid);
	
}
