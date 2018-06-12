package com.net.download.dao;

import android.content.Context;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.net.download.DownloadEntry;
import com.vincestyling.netroid.toolbox.FileDownloader;
//import com.netroid.toolbox.FileDownloader;


public class DownloadTask {
	private static final String TAG = "***DownloadTask***";
	public FileDownloader.DownloadController controller;
	private DownloadEntry entry;
	public String storeFileName;
	public String url;
	public int resFlag = -1;
	public int notifyId = 0;

	public ProgressBar mProgress;
	public TextView mDownloadRate;
	public TextView mProgressNum;
	public ImageView resTypeImg;
	public Button mBtnOperate;
	public CheckBox mCheckbox;
	private Context mContext;

	public long fileSize;
	public long downloadedSize;
	public DownloadStateEnum state;
	
	private long mCurTime = 0;
	private long mPrevTime = 0;
	private long mPrevDownloadSize = 0;


	public DownloadEntry getEntry() {
		return entry;
	}

	public DownloadTask(DownloadEntry entry) {
		this.entry = entry;
	}



	public DownloadTask(Context context, String storeFileName, String url, int res_flag, int notifyId, boolean isdownload) {
		
		this.mContext = context;
		this.storeFileName = storeFileName;
		this.url = url;
		this.resFlag = res_flag;
		this.notifyId = notifyId;

	}


	private String rateFormatter(long time, long size) {
		String str = "";
		
		long value = (size*1000)/(time*1024);
		if (value < 1024) {
			str = value + "K/s";
		}
		else if (value < (1024*1024)) {
			str = value/1024 + "M/s";
		}
		else {
			str = value/1024/1024 + "G/s";
		}
		
		return str;
	}

}
