package com.net.download.dao;//package com.lancoo.downlaod.dao;
//
//import android.annotation.TargetApi;
//import android.app.Notification;
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.content.Intent;
//import android.os.Build;
//import android.support.v4.app.NotificationCompat.Builder;
//import android.text.format.Formatter;
//import android.util.Log;
//import android.widget.RemoteViews;
//
//import com.lancoo.afterclassondemand.servies.DownloadService;
//import com.lancoo.afterclassondemand.ui.DownloadActivity;
//import com.lancoo.downlaod.R;
//
//
//public class MyNotification {
//
//	private static final String TAG = "***MyNotification***";
//
//	private Notification mNotification;
//	private int mNotifyId = 0;
//	private NotificationManager mNotificationManager;
//	private DownloadTask mTask;
//	private DownloadService mService;
//
//
//	public MyNotification(DownloadService service, NotificationManager manager, DownloadTask task) {
//		mNotificationManager = manager;
//		mNotifyId = task.notifyId;
//		Log.i(TAG, "***mNotifyId = " + mNotifyId + "***");
//		mService = service;
//		mTask = task;
//
//		setUpNotification();
//	}
//
//	// 通知栏
//    /**
//     * 创建通知
//     */
//    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
//	private void setUpNotification() {
//    	Log.i(TAG, "******setUpNotification********");
//        int icon = R.drawable.res_type_01 + mTask.resFlag -1;
//        Builder mBuilder = new Builder(mService);
//
//        RemoteViews contentView = new RemoteViews(mService.getPackageName(), R.layout.notification_download_layout);
//        contentView.setImageViewResource(R.id.id_notification_res_icon, icon);
//        contentView.setTextViewText(R.id.id_notification_download_text, mTask.storeFileName + " 正在下载...");
//        contentView.setTextViewText(R.id.id_notification_received_progress_num, "0KB/0KB");
//        contentView.setTextViewText(R.id.id_notification_received_progress_percent, 0+"%");
//        contentView.setProgressBar(R.id.id_notification_download_progress_bar, 100, 0, false);
//
//        mBuilder.setContent(contentView)
//        .setSmallIcon(icon)
//        .setContentText("")
//        .setContentIntent(getDefalutIntent(PendingIntent.FLAG_UPDATE_CURRENT))
////        .setContentIntent(intent_start)
//        .setWhen(System.currentTimeMillis())// 通知产生的时间，会在通知信息里显示
//        .setTicker("开始下载")
//        .setPriority(Notification.PRIORITY_DEFAULT)// 设置该通知优先级
//        .setOngoing(true);
//        mNotification = mBuilder.build();
//        mNotification.flags = Notification.FLAG_ONGOING_EVENT;
//		mNotificationManager.notify(mNotifyId, mNotification);
//    }
//
//    public PendingIntent getDefalutIntent(int flags){
//
//    	Intent intent = new Intent(mService, DownloadActivity.class);
//    	intent.putExtra("download_state", 1);
//    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        PendingIntent pendingIntent= PendingIntent.getActivity(mService, 1, intent, flags);
//        return pendingIntent;
//    }
//
//    //下载中。。。
//    public void downloading(long file_size, long download_size) {
//    		int mProgress = (int)(download_size*100/file_size);
//    		mNotification.contentView.setProgressBar(R.id.id_notification_download_progress_bar, 100, mProgress, false);
//    		mNotification.contentView.setTextViewText(R.id.id_notification_received_progress_percent, mProgress+"%");
//    		mNotification.contentView.setTextViewText(R.id.id_notification_received_progress_num,
//    				Formatter.formatFileSize(mService, download_size)+"/"+ Formatter.formatFileSize(mService, file_size));
//    		mNotificationManager.notify(mNotifyId, mNotification);
//    }
//
//
//    //下载结束。。。
//    public void downloaded() {
////    	mNotification.flags = Notification.FLAG_AUTO_CANCEL;
////        mNotification.contentView = null;
////        Intent intent = new Intent(mService, DownloadActivity.class);
////        // 告知已完成
////        intent.putExtra("download_state", 0);
////        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////        // 更新参数,注意flags要使用FLAG_UPDATE_CURRENT
////        PendingIntent contentIntent = PendingIntent.getActivity(mService, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
////        mNotification.setLatestEventInfo(mService, "下载完成", mTask.storeFileName + " 已下载完毕", contentIntent);
//
//        int icon = R.drawable.res_type_01 + mTask.resFlag -1;
//        Intent intent = new Intent(mService, DownloadActivity.class);
//        // 告知已完成
//        intent.putExtra("download_state", 0);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        PendingIntent contentIntent = PendingIntent.getActivity(mService, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN  )
//        {
//            Notification.Builder builder = new Notification.Builder(mService)
//                    .setAutoCancel(true)
//                    .setContentTitle("下载完成")
//                    .setContentText(mTask.storeFileName + " 已下载完毕")
//                    .setContentIntent(contentIntent)
//                    .setSmallIcon(icon)
//                    .setWhen(System.currentTimeMillis())
//                    .setOngoing(true);
//            Notification notification=builder.getNotification();
//            mNotificationManager.notify(mNotifyId, notification);
//        }
//        else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
//        {
//            Notification notification = new Notification.Builder(mService)
//                    .setAutoCancel(true)
//                    .setContentTitle("下载完成")
//                    .setContentText(mTask.storeFileName + " 已下载完毕")
//                    .setContentIntent(contentIntent)
//                    .setSmallIcon(icon)
//                    .setWhen(System.currentTimeMillis())
//                    .build();
//            mNotificationManager.notify(mNotifyId, notification);
//        }
//
//    }
//
//}
