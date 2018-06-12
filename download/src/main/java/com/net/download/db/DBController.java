package com.net.download.db;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.net.download.DownloadEntry;
import com.net.download.DownloadStatus;
import com.net.download.util.Trace;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * 
 * @author shuwoom
 * @email 294299195@qq.com
 * @date 2015-9-2
 * @update 2015-9-2
 * @des DBController
 */
public class DBController {
    private static DBController mInstance;
    private OrmDBHelper mDBhelper;

    private DBController(Context context) {
        mDBhelper = new OrmDBHelper(context);
        mDBhelper.getWritableDatabase();
    }

    public static DBController getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DBController(context);
        }
        return mInstance;
    }

    public synchronized void newOrUpdate(DownloadEntry entry) {
        try {
            Dao<DownloadEntry, String> dao = mDBhelper.getDao(DownloadEntry.class);
            dao.createOrUpdate(entry);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized ArrayList<DownloadEntry> queryAll() {
        Dao<DownloadEntry, String> dao;
        try {
            dao = mDBhelper.getDao(DownloadEntry.class);
            return (ArrayList<DownloadEntry>) dao.query(dao.queryBuilder().prepare());
        } catch (SQLException e) {
            Trace.e(e.getMessage());
            return null;
        }
    }

    public synchronized ArrayList<DownloadEntry> querydownloaded() {
        Dao<DownloadEntry, String> dao;
        try {
            dao = mDBhelper.getDao(DownloadEntry.class);
            return (ArrayList<DownloadEntry>) dao.queryBuilder().where().eq("status", DownloadStatus.done).query();
        } catch (SQLException e) {
            Trace.e(e.getMessage());
            return null;
        }
    }

    public synchronized ArrayList<DownloadEntry> querydownloadnotCompelete() {
        Dao<DownloadEntry, String> dao;
        try {
            dao = mDBhelper.getDao(DownloadEntry.class);
            return (ArrayList<DownloadEntry>) dao.queryBuilder().where().in("status",DownloadStatus.pause,DownloadStatus.downloading).query();
        } catch (SQLException e) {
            Trace.e(e.getMessage());
            return null;
        }
    }

    public synchronized DownloadEntry queryByUrl(String url) {
        try {
            Dao<DownloadEntry, String> dao = mDBhelper.getDao(DownloadEntry.class);
            return dao.queryForId(url);
        } catch (SQLException e) {
            Trace.e(e.getMessage());
            return null;
        }
    }
    
    public synchronized void deleteByUrl(String url){
    	 try {
			Dao<DownloadEntry, String> dao = mDBhelper.getDao(DownloadEntry.class);
			dao.deleteById(url);
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }

    public synchronized void deleteAll(){
        try {
            Dao<DownloadEntry, String> dao = mDBhelper.getDao(DownloadEntry.class);
//            DeleteBuilder<DownloadEntry,String> deleteBuilder = mDBhelper.getDao(DownloadEntry.class).deleteBuilder();
            dao.deleteBuilder().delete();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized void deleteDownlaoded(){
        try {
            Dao<DownloadEntry, String> dao = mDBhelper.getDao(DownloadEntry.class);
//            DeleteBuilder<DownloadEntry,String> deleteBuilder = mDBhelper.getDao(DownloadEntry.class).deleteBuilder();
            dao.delete(querydownloaded());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
