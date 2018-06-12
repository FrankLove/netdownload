package com.net.download;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;

import com.net.download.dao.DownloadTask;
import com.net.download.dao.Netroid;
import com.net.download.db.DBController;
import com.net.download.notify.DataChanger;
import com.net.download.util.Constants;
import com.net.download.util.FileUtil;
import com.net.download.util.NotificationUtil;
import com.net.download.util.Trace;
import com.vincestyling.netroid.Listener;
import com.vincestyling.netroid.NetroidError;
import com.vincestyling.netroid.request.FileDownloadRequest;
import com.vincestyling.netroid.toolbox.FileDownloader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

//import com.lancoo.afterclassondemand.model.Resource;
//import com.lancoo.afterclassondemand.util.ToolUtil;
//import com.lancoo.downlaod.dao.MyNotification;
//import org.simple.eventbus.EventBus;
//import org.simple.eventbus.Subscriber;

public class DownloadService extends Service {

	private static final String TAG = "***DownloadService***";
    private Context mContext = this;
    private String mSaveDirPath;
	private LinkedList<DownloadTask> mTaskList;
	private ArrayList<DownloadTask> mDownloadList;
	private FileDownloader mDownloader;
	private long lastStamp = 0;
	
	private int mNotifyId = 0;

	private DataChanger dataChanger;
	private DBController dbController;
	private HashMap<String, DownloadTask> mDownloadingTasks;
	private LinkedBlockingQueue<DownloadEntry> mWaitingQueue;
    
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.i(TAG, "onCreate: ");

		mDownloadingTasks = new HashMap<String, DownloadTask>();
		mWaitingQueue = new LinkedBlockingQueue<DownloadEntry>();

		dataChanger = DataChanger.getInstance(getApplicationContext());
		dbController = DBController.getInstance(getApplicationContext());
		
		initNetroid();
		
		mSaveDirPath = FileUtil.getFileDownloadDir(mContext);
		
		File downloadDir = new File(mSaveDirPath);
		if (!downloadDir.exists()) downloadDir.mkdirs();
		
		mTaskList = new LinkedList<DownloadTask>();
		mDownloadList = new ArrayList<DownloadTask>();
		
	}

	protected void initNetroid() {
		Netroid.init(null);

		Netroid.setFileDownloder(new FileDownloader(Netroid.getRequestQueue(), 2) {
			@Override
			public FileDownloadRequest buildRequest(File storeFile, String url) {
				return new FileDownloadRequest(storeFile, url) {
					@Override
					public void prepare() {
						addHeader("Accept-Encoding", "identity");
						super.prepare();
					}
				};
			}
		});

//		RequestQueue queue = Netroid.newRequestQueue(getApplicationContext(), null);
//		mDownloader = new FileDownloader(queue, 2) {
//			@Override
//			public FileDownloadRequest buildRequest(String storeFilePath, String url) {
//				return new FileDownloadRequest(storeFilePath, url) {
//					@Override
//					public void prepare() {
//						addHeader("Accept-Encoding", "identity");
//						super.prepare();
//					}
//				};
//			}
//		};
	}
	
    private void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}
    
    ////通过uri获取文件名称；
    private String getFileName (String url) {
    	Uri uri = Uri.parse(url);
    	String name = "";
		if (uri != null) {
	        List<String> paths = uri.getPathSegments();
	        name = paths == null || paths.isEmpty() ? "null" : paths.get(paths.size() - 1);
	    }
		
		return name;
    }
    
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.i(TAG, "*****onStartCommand***** begin mDownloadList.size() "+mDownloadList.size());
		if (intent == null) {
			return START_NOT_STICKY;
		}

//		在7.0以上的手机需要动态申请权限，由于服务启动之前还没有完成权限申请，导致文件夹创建失败，所以需要在这里再判断一遍 2018-3-15
		File downloadDir = new File(mSaveDirPath);
		if (!downloadDir.exists())
		{
			boolean issuccessmkdir = downloadDir.mkdirs();
			Log.i(TAG, "onStartCommand: issuccessmkdir "+issuccessmkdir);
		}

		int action = intent.getIntExtra(Constants.KEY_DOWNLOAD_ACTION, -1);
		DownloadEntry entry = (DownloadEntry) intent.getSerializableExtra(Constants.KEY_DOWNLOAD_ENTRY);
		if(entry != null && dataChanger.containsDownloadEntry(entry.url)){

			DownloadEntry tempentry = dataChanger.queryDownloadEntryByUrl(entry.url);
			entry = tempentry == null?entry:tempentry;
//			Log.i(TAG, "onStartCommand: 11 "+action +"	resource name "+entry.name);
		}

		Log.i(TAG, "onStartCommand:action "+action);
		switch(action){
			case Constants.KEY_DOWNLOAD_ACTION_ADD:
				addDownload(entry);
				break;

			case Constants.KEY_DOWNLOAD_ACTION_PAUSE:
				pauseDownload(entry);
				break;

			case Constants.KEY_DOWNLOAD_ACTION_RESUME:
				addDownload(entry);
				break;

			case Constants.KEY_DOWNLOAD_ACTION_CANCEL:
				cancelDownload(entry);
				break;

			case Constants.KEY_DOWNLOAD_ACTION_PAUSE_ALL:
				pauseAllDownload();
				break;

			case Constants.KEY_DOWNLOAD_ACTION_RECOVER_ALL:
				recoverAllDownload();
				break;
			default:
				break;

		}

		Log.i(TAG, "*****onStartCommand***** over mDownloadList.size() "+mDownloadList.size());
		return super.onStartCommand(intent, flags, startId);
	}

    /**
     * 全部开始下载
     */
	private void recoverAllDownload()
	{
		DownloadEntry entry;
		Iterator iterator_1 = mDownloadingTasks.keySet().iterator();
		while (iterator_1.hasNext()) {
			String key = (String) iterator_1.next();
			DownloadTask task = mDownloadingTasks.get(key);

			task.controller.resume();
			entry = task.getEntry();
			entry.status = DownloadStatus.downloading;
			DataChanger.getInstance(getApplication()).updateStatus(entry);
		}
	}

    /**
     * 取消下载
     */
	public void cancelDownload(DownloadEntry entry)
	{
		DownloadTask task = mDownloadingTasks.get(entry.url);
		if(task != null)
		{
			Trace.d("DownloadService==>pauseDownload#####pause downloading task"
					+ "***Task Size:" + mDownloadingTasks.size()
					+ "***Waiting Queue:" + mWaitingQueue.size());
			task.controller.discard();
//			entry.status = DownloadStatus.pause;
			Log.i(TAG, "cancelDownload: entry.status "+entry.status);
		}
	}

    /**
     * 全部暂停下载，暂停下载并更新数据库状态
     */

	private void pauseAllDownload() {
//		while(mWaitingQueue.iterator().hasNext()){
//			DownloadEntry entry = mWaitingQueue.poll();
//			entry.status = DownloadStatus.pause;
//			DataChanger.getInstance(getApplication()).updateStatus(entry);
//		}
//
//		for (Map.Entry<String, DownloadTask> entry : mDownloadingTasks.entrySet()) {
//			entry.getValue().pause();
//		}
//		mDownloadingTasks.clear();
		DownloadEntry entry;

		Iterator iterator_1 = mDownloadingTasks.keySet().iterator();
		while (iterator_1.hasNext()) {
			String key = (String) iterator_1.next();
			DownloadTask task = mDownloadingTasks.get(key);


			task.controller.pause();
			entry = task.getEntry();
			entry.status = DownloadStatus.pause;
			DataChanger.getInstance(getApplication()).updateStatus(entry);
		}

	}

	private void pauseDownload(DownloadEntry entry) {
		Log.i(TAG, "pauseDownload: entry.name "+entry.name);
//		DownloadTask task = mDownloadingTasks.remove(entry.url);
		DownloadTask task = mDownloadingTasks.get(entry.url);
		if(task != null)
		{
			Trace.d("DownloadService==>pauseDownload#####pause downloading task"
					+ "***Task Size:" + mDownloadingTasks.size()
					+ "***Waiting Queue:" + mWaitingQueue.size());
			task.controller.pause();
			entry.status = DownloadStatus.pause;
			DataChanger.getInstance(getApplication()).updateStatus(entry);
			Log.i(TAG, "pauseDownload: entry.status "+entry.status);
		}else{
			Log.i(TAG, "pauseDownload: task is null");
//			mWaitingQueue.remove(entry);
//			entry.status = DownloadStatus.pause;
//			DataChanger.getInstance(getApplication()).updateStatus(entry);
//			Trace.d("DownloadService==>pauseDownload#####pause waiting queue!"
//					+ "***Task Size:" + mDownloadingTasks.size()
//					+ "***Waiting Queue:" + mWaitingQueue.size());
		}

	}

	private void checkDownloadPath(DownloadEntry entry) {
		Trace.d("DownloadService==>checkDownloadPath()");
		File file = new File(mSaveDirPath+ entry.url.substring(entry.url.lastIndexOf("/") + 1));
		if(file != null && !file.exists()){
			entry.reset();
			Trace.d("DownloadService==>checkDownloadPath()#####" + entry.name + "'s cache is not exist, restart download!");
		}
	}

    /**
     * 开始下载
     */
	private void addDownload(final DownloadEntry entry) {

		Log.i(TAG, "addDownload: ");
		checkDownloadPath(entry);
		if(isDownloadEntryRepeted(entry)){
			Log.i(TAG, "addDownload: resume");
			mDownloadingTasks.get(entry.url).controller.resume();
			return ;
		}

		final DownloadTask task = new DownloadTask(entry);

//		mTaskList.add(task);
		task.controller = Netroid.getFileDownloader().add(mSaveDirPath + entry.name, entry.url, new Listener<Void>()
		{

			private int notifyId = mNotifyId;
			private int count = 0;

			@Override
			public void onPreExecute() {
				super.onPreExecute();
				mNotifyId++;
//				NotificationUtil.getInstance(getApplicationContext()).showNotification(getApplicationContext(),notifyId,"下载中...",entry.name+"("+ Formatter.formatFileSize(getApplicationContext(), entry.totalLength) +")");
				NotificationUtil.showNotification(getApplicationContext(),notifyId,"下载中...",entry.name+"("+ Formatter.formatFileSize(getApplicationContext(), entry.totalLength) +")");
//				NotificationUtil.showNotificationProgress(getApplicationContext());
				entry.status = DownloadStatus.downloading;
				entry.downloadpath = mSaveDirPath+entry.name;
				Log.i(TAG, "onPreExecute: entry.url "+entry.url);
			}

			@Override
			public void onSuccess(Void aVoid) {
				Log.i(TAG, "onSuccess: ");
				mDownloadingTasks.remove(entry.url);
				entry.status = DownloadStatus.done;
				entry.percent = 100;
				entry.downloadpath = mSaveDirPath+entry.name;
				DataChanger.getInstance(getApplication()).updateStatus(entry);
				NotificationUtil.changeNotificationText(getApplicationContext(),notifyId,"下载完成",entry.name+"("+ Formatter.formatFileSize(getApplicationContext(), entry.totalLength) +")");

			}

			@Override
			public void onFinish() {
				super.onFinish();
//				NotificationUtil.removeNotification(getApplicationContext(),notifyId);
				Log.i(TAG, "onFinish: ");
			}

			@Override
			public void onError(NetroidError error) {
				super.onError(error);
				mDownloadingTasks.remove(entry.url);//有待测试
				entry.status = DownloadStatus.pause;
				DataChanger.getInstance(getApplication()).updateStatus(entry);
				Toast.makeText(mContext,"下载出错，请检查网络连接~",Toast.LENGTH_SHORT).show();
				NotificationUtil.changeNotificationText(getApplicationContext(),notifyId,"下载失败",entry.name);
				Log.i(TAG, "onError: ");
			}

			@Override
			public void onProgressChange(long fileSize, long downloadedSize) {
				//点击暂停以后，有时这个函数还没有结束回调，导致下载状态写入错误(状态本该暂停，却是正在下载)
//				Log.i(TAG, "onProgressChange: fileSize "+entry.name+"	 downloadedSize "+downloadedSize+"  "+entry.status+"  "+task.controller.getStatus());
//				super.onProgressChange(fileSize, downloadedSize);
				Log.i(TAG, "onProgressChange: "+task.controller.getStatus());

				if (task.controller.getStatus() == FileDownloader.DownloadController.STATUS_PAUSE)
				{
					entry.status = DownloadStatus.pause;
				}
				else if(task.controller.getStatus() == FileDownloader.DownloadController.STATUS_DISCARD)
				{
					DBController.getInstance(mContext).deleteByUrl(entry.url);
					mDownloadingTasks.remove(entry.url);
					return;
				}


				long stamp = System.currentTimeMillis();
				//防止下载进度回调过快
				if(stamp - lastStamp  >= 500 || fileSize == downloadedSize){
					lastStamp = stamp;
					entry.currentLength = (int)downloadedSize;
					entry.totalLength = (int)fileSize;
					int percent = (int)(entry.currentLength * 100l / entry.totalLength);
					entry.percent = percent;
					DataChanger.getInstance(getApplication()).updateStatus(entry);

					String notifymessage = Formatter.formatFileSize(getApplicationContext(), entry.currentLength) + "/"
							+ Formatter.formatFileSize(getApplicationContext(), entry.totalLength)
							+" "+entry.percent+"%";
					//2.5秒后更新通知栏进度，否则听不到声音
					count++;
					if (count >= 5)
					  NotificationUtil.updateNotificationProcess(getApplicationContext(),notifyId,"下载中...",entry.name+"("+notifymessage +")");

				}

			}
		});
		entry.status = DownloadStatus.waiting;
		mDownloadingTasks.put(entry.url, task);
		DataChanger.getInstance(getApplication()).updateStatus(entry);

//		if(mDownloadingTasks.size() >= DownloadConfig.getInstance().getMax_download_tasks()){
//			mWaitingQueue.offer(entry);
//			entry.status = DownloadStatus.waiting;
//			DataChanger.getInstance(getApplication()).updateStatus(entry);
//			Trace.d("DownloadService==>addDownload#####bigger than max_tasks"
//					+ "***Task Size:" + mDownloadingTasks.size()
//					+ "***Waiting Queue:" + mWaitingQueue.size());
//		}else{
//			Trace.d("DownloadService==>addDownload#####start tasks"
//					+ "***Task Size:" + mDownloadingTasks.size()
//					+ "***Waiting Queue:" + mWaitingQueue.size());
//			startDownload(entry);
//		}
	}

	private boolean isDownloadEntryRepeted(DownloadEntry entry){
		if(mDownloadingTasks.get(entry.url) != null){
			Trace.d("DownlaodService==>isDownloadEntryRepeted()##### The downloadEntry is in downloading tasks!!");
			return true;
		}

//		if(mWaitingQueue.contains(entry)){
//			Trace.d("DownlaodService==>isDownloadEntryRepeted()##### The downloadEntry is in waiting queue!!");
//			return true;
//		}
		return false;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.i(TAG, "***DownloadService Destroyed***");
		mDownloadingTasks.clear();
		mDownloadingTasks = null;
//		mNotificationManager.cancelAll();
//		mDownloader.clearAll();
		mDownloadList.clear();
		mDownloadList = null;
//		EventBus.getDefault().unregister(this);
		stopSelf();
		Netroid.destroy();
		super.onDestroy();
	}

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
