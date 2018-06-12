package com.demo.download;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.lancoo.download.R;
import com.net.download.DownloadEntry;
import com.net.download.DownloadManager;
import com.net.download.DownloadStatus;
import com.net.download.db.DBController;
import com.net.download.notify.DataWatcher;

import java.util.ArrayList;
import java.util.List;

//import io.julian.appchooser.AppChooser;

//import io.julian.appchooser.AppChooser;

/**
 * 
 * @author shuwoom
 * @email 294299195@qq.com
 * @date 2015-9-2
 * @update 2015-9-2
 * @des Test download list.
 */
public class ListActivity extends AppCompatActivity implements OnClickListener{
	
	private ArrayList<DownloadEntry> downloadEntries = new ArrayList<DownloadEntry>();
	private ArrayList<DownloadEntry> downloadcompeleteEntries = new ArrayList<DownloadEntry>();
	
	
	private Button pauseAllBtn;
	private Button recoverAllBtn;
	private ListView listView,listViedownload;
    private DownloadAdapter adapter,adapter_downlaod;
	private static final String TAG = "ListActivity";
	
	private DataWatcher dataWatcher = new DataWatcher() {

		@Override
		public void onDataChanged(DownloadEntry downloadEntry) {
			int index = downloadEntries.indexOf(downloadEntry);
			if(index != -1){
				downloadEntries.remove(index);
				if (downloadEntry.status != DownloadStatus.done) //如果没有下载完成
				{
					downloadEntries.add(index, downloadEntry);
				}
				else  //如果下载完成
				{
					downloadcompeleteEntries.add(downloadEntry);
					adapter_downlaod.updateData();
					adapter_downlaod.notifyDataSetChanged();
				}
				adapter.notifyDataSetChanged();
			}
		}
	};
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);

		init();

		
	}

	private void init()
	{
		checkPermission();

		pauseAllBtn = (Button)findViewById(R.id.pause_all_btn);
		recoverAllBtn = (Button)findViewById(R.id.recover_all_btn);
		listView = (ListView) findViewById(R.id.listview);
		listViedownload = (ListView) findViewById(R.id.listview_downloaded);


		DownloadEntry entry = new DownloadEntry();
		entry.fileType = 1;
		entry.name = "神偷奶爸.rmvb";
		entry.url = "http://172.16.52.55:50/%E7%A5%9E%E5%81%B7%E5%A5%B6%E7%88%B8_DVD[www.77vcd.com].rmvb";
		downloadEntries.add(entry);

//		entry = new DownloadEntry();
//		entry.name = "战狼.rmvb";
//		entry.fileType = 1;
//		entry.url = "http://172.16.52.114:50/[%E7%94%B5%E5%BD%B1%E5%A4%A9%E5%A0%82www.dy2018.com]%E6%88%98%E7%8B%BCHD%E4%B8%AD%E8%8B%B1%E5%8F%8C%E5%AD%97.rmvb";
//		downloadEntries.add(entry);
//
//		entry = new DownloadEntry();
//		entry.name = "X战警.rmvb";
//		entry.fileType = 1;
//		entry.url = "http://172.16.52.114:50/[www.d9vod.com]%E6%9C%BA%E6%A2%B0%E6%88%98%E8%AD%A6DVDscr%E4%B8%AD%E5%AD%97.rmvb";
//		downloadEntries.add(entry);
//
//		entry = new DownloadEntry();
//		entry.name = "西游记之大闹天宫.rmvb";
//		entry.fileType = 1;
//		entry.url = "http://172.16.52.114:50/[www.shoubow.com]%E8%A5%BF%E6%B8%B8%E8%AE%B0%E4%B9%8B%E5%A4%A7%E9%97%B9%E5%A4%A9%E5%AE%ABDVD.rmvb";
//		downloadEntries.add(entry);
//
//		entry = new DownloadEntry();
//		entry.name = "2.flv";
//		entry.fileType = 1;
//		entry.url = "http://172.16.52.114:50/2.flv";
//		downloadEntries.add(entry);
//
		entry = new DownloadEntry();
		entry.name = "1_1.jpg";
		entry.fileType = 7;
		entry.url = "http://172.16.52.55:50/1_1.jpg";
		downloadEntries.add(entry);

//		entry = new DownloadEntry();
//		entry.name = "个人收入证明.doc";
//		entry.fileType = 8;
//		entry.url = "http://172.16.52.114:50/%E4%B8%AA%E4%BA%BA%E6%94%B6%E5%85%A5%E8%AF%81%E6%98%8E.doc";
//		downloadEntries.add(entry);


//		downloadEntries = DBController.getInstance(getApplicationContext()).querydownloadnotCompelete();

		//已完成
		downloadcompeleteEntries = DBController.getInstance(getApplicationContext()).queryAll();
		adapter_downlaod = new DownloadAdapter(downloadcompeleteEntries,getApplicationContext());
		listViedownload.setAdapter(adapter_downlaod);

//		for (int i = 0;i < downloadcompeleteEntries.size();i++)
//		{
////			if (downloadcompeleteEntries.get(i).name.equals(dw))
//		}

//		downloadEntries = DBController.getInstance(getApplicationContext()).querydownloadnotCompelete();
		adapter = new DownloadAdapter(downloadEntries,getApplicationContext());
		listView.setAdapter(adapter);

		listViedownload.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				Log.i(TAG, "onItemClick: i"+i+"		"+downloadcompeleteEntries.get(i).downloadpath);
				showFile(downloadcompeleteEntries.get(i).downloadpath);
			}
		});

		pauseAllBtn.setOnClickListener(this);
		recoverAllBtn.setOnClickListener(this);
	}
	
	@Override
    protected void onResume() {
        super.onResume();
		Log.i(TAG, "onResume: ");
		DownloadManager.getInstance(this).addObserver(dataWatcher);
    }
	
	@Override
    protected void onPause() {
        super.onPause();
		Log.i(TAG, "onPause: ");
		DownloadManager.getInstance(this).removeObserver(dataWatcher);
    }
	
	private void showFile(String path) {
//		AppChooser.from(ListActivity.this)
//				.file(new File(path))
////				.excluded(excluded)
////				.requestCode(REQUEST_CODE_OPEN_FILE)
//				.load();
	}



	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.pause_all_btn:
			DownloadManager.getInstance(ListActivity.this).pauseAll();
//			Intent intent = new Intent(ListActivity.this,DownloadActivity.class);
//			intent.putExtra("list",downloadEntries);

//			startActivity(intent);
			break;
		case R.id.recover_all_btn:
//			DownloadManager.getInstance(ListActivity.this).recoverAll();
			DBController.getInstance(getApplicationContext()).deleteDownlaoded();
			break;
		}
	}

	final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
	public void checkPermission() {
		if(Build.VERSION.SDK_INT >= 23) {
			List<String> permissionStrs = new ArrayList<>();
			int hasWriteSdcardPermission =
					ContextCompat.checkSelfPermission(
							ListActivity.this,
							Manifest.permission.WRITE_EXTERNAL_STORAGE);
			if(hasWriteSdcardPermission !=
					PackageManager.PERMISSION_GRANTED) {
				permissionStrs.add(
						Manifest.permission.WRITE_EXTERNAL_STORAGE
				);
			}

			int hasCameraPermission = ContextCompat.checkSelfPermission(
					ListActivity.this,
					Manifest.permission.CAMERA);
			if(hasCameraPermission != PackageManager.PERMISSION_GRANTED) {
				permissionStrs.add(Manifest.permission.CAMERA);
			}
			String[] stringArray = permissionStrs.toArray(new String[0]);
			if (permissionStrs.size() > 0) {
				requestPermissions(stringArray,
						REQUEST_CODE_ASK_PERMISSIONS);
				return;
			}
		}
	}

	//权限设置后的回调函数，判断相应设置，requestPermissions传入的参数为几个权限，则permissions和grantResults为对应权限和设置结果
	int perssioncount = 0;
	@Override
	public void onRequestPermissionsResult(int requestCode,
										   @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch(requestCode) {
			case REQUEST_CODE_ASK_PERMISSIONS :
				Log.i(TAG, "onRequestPermissionsResult: grantResults "+permissions[perssioncount++]);
				//可以遍历每个权限设置情况
				if(grantResults[0]== PackageManager.PERMISSION_GRANTED) {
					//这里写你需要相关权限的操作

				}else{
					Toast.makeText(ListActivity.this,
							"权限没有开启",Toast.LENGTH_SHORT).show();
				}
		}
		super.onRequestPermissionsResult(requestCode, permissions,
				grantResults);
	}

}
