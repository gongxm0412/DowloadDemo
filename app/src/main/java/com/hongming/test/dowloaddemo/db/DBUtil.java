package com.hongming.test.dowloaddemo.db;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.hongming.test.dowloaddemo.model.ThreadInfo;

public class DBUtil implements ThreadDAO {
	private DBHelper dbhelp;

	public DBUtil(Context con) {
		dbhelp = new DBHelper(con);
	}

	@Override
	public void insertThreadTable(ThreadInfo threadinfo) {
		// TODO Auto-generated method stub
		SQLiteDatabase db = dbhelp.getWritableDatabase();
		db.execSQL("insert into  thread_table (thread_id , url ,start ,end ,finished) values(?,?,?,?,?)", new Object[] {
				threadinfo.theadid, threadinfo.url, threadinfo.start, threadinfo.end, threadinfo.finished });
		db.close();
		db = null;
	}

	@Override
	public void deleteThreadTable(int threadid) {
		SQLiteDatabase db = dbhelp.getWritableDatabase();
		db.execSQL("delete from thread_table where thread_id = '" + threadid + "'");
		db.close();
	}

	@Override
	public void updateTheadTable(int threadid, int finished) {
		SQLiteDatabase db = dbhelp.getWritableDatabase();
		db.execSQL("update  thread_table set finished = ? where thread_id = ?", new Object[] { finished, threadid });
		db.close();
	}

	@Override
	public List<ThreadInfo> getThreads(String url) {
		List<ThreadInfo> threadLst = new ArrayList<ThreadInfo>();
		SQLiteDatabase db = dbhelp.getWritableDatabase();
		Cursor cursor = db.rawQuery("select * from thread_table where url = ? ", new String[] { url });
		ThreadInfo threadmodel = null;
		while (cursor.moveToNext()) {
			threadmodel = new ThreadInfo();
			threadmodel.theadid = cursor.getInt(1);
			threadmodel.url = cursor.getString(2);
			threadmodel.start = cursor.getInt(3);
			threadmodel.end = cursor.getInt(4);
			threadmodel.finished = cursor.getInt(5);
			threadLst.add(threadmodel);
		}
		cursor.close();
		db.close();
		return threadLst;
	}

	@Override
	public boolean isExists(String url, int taskid) {
		SQLiteDatabase db = dbhelp.getWritableDatabase();
		Cursor cursor = db.rawQuery("select * from thread_table where url = ? and thread_id = ? ",
				new String[] { url, taskid + "" });
		boolean isexists = cursor.moveToNext();
		cursor.close();
		db.close();
		return isexists;
	}
}
