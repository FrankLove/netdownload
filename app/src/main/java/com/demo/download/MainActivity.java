package com.demo.download;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.lancoo.download.R;
import com.net.download.DownloadEntry;
import com.net.download.DownloadManager;
import com.net.download.notify.DataWatcher;


/**
 * 
 * @author shuwoom
 * @email 294299195@qq.com
 * @date 2015-9-2
 * @update 2015-9-2
 * @des Test one download task.
 */
public class MainActivity extends Activity implements OnClickListener{
	
	private final String url = "http://172.16.52.93:50/%E7%A5%9E%E5%81%B7%E5%A5%B6%E7%88%B8_DVD[www.77vcd.com].rmvb";
	private DownloadEntry entry = new DownloadEntry(url);
	
	private Button addBtn;
	private Button cancelBtn;
	private Button pauseBtn;
	private Button resumeBtn;
	
	private TextView showText;
	
	private DataWatcher dataWatcher = new DataWatcher() {

		@Override
		public void onDataChanged(DownloadEntry data) {
			entry = data;
			showText.setText(entry.toString());
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		entry.name = "x三国.apk";
		
		showText = (TextView)findViewById(R.id.show_text);
		addBtn = (Button)findViewById(R.id.add_btn);
		cancelBtn = (Button)findViewById(R.id.cancel_btn);
		pauseBtn = (Button)findViewById(R.id.pause_btn);
		resumeBtn = (Button)findViewById(R.id.resume_btn);
		
		addBtn.setOnClickListener(this);
		cancelBtn.setOnClickListener(this);
		pauseBtn.setOnClickListener(this);
		resumeBtn.setOnClickListener(this);
		
	}
	
	@Override
    protected void onResume() {
        super.onResume();
		DownloadManager.getInstance(this).addObserver(dataWatcher);
    }
	
	@Override
    protected void onPause() {
        super.onPause();
        DownloadManager.getInstance(this).removeObserver(dataWatcher);
    }

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.add_btn:
			DownloadManager.getInstance(MainActivity.this).add(entry);
			break;
		case R.id.pause_btn:
//			DownloadManager.getInstance(MainActivity.this).pause(entry);
//			Intent intent = new Intent(MainActivity.this,DownloadActivity.class);
//			startActivity(intent);
			break;
		case R.id.resume_btn:
//			DownloadManager.getInstance(MainActivity.this).resume(entry);
			break;
		case R.id.cancel_btn:
//			DownloadManager.getInstance(MainActivity.this).cancel(entry);
			break;
		}
	}

}
