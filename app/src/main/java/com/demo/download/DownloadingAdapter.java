package com.demo.download;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lancoo.download.R;
import com.net.download.DownloadEntry;
import com.net.download.DownloadManager;
import com.net.download.DownloadStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Frank on 2018/3/13.
 */

class DownloadingAdapter extends BaseAdapter {

    private ViewHolder holder;
    private ArrayList<DownloadEntry> arrlistentry;
    private int myPostion;
    private int resid = R.drawable.res_type_01;
    private Context context;
    private static final String TAG = "DownloadAdapter";
    public static List<Boolean> isSelected;
    private boolean mbIsEdit = false;

    public DownloadingAdapter(ArrayList<DownloadEntry> arrlistentry, Context context)
    {
        this.arrlistentry = arrlistentry;
        this.context = context;
        isSelected = new ArrayList<Boolean>();
        if (null != arrlistentry) {
            for (int i = 0; i < arrlistentry.size(); i ++) {
                isSelected.add(false);
            }
        }
    }

    private void setMyPostion(int postion)
    {
        myPostion = postion;
    }

    @Override
    public int getCount() {
        return arrlistentry != null ? arrlistentry.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return arrlistentry.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final  int position, View convertView, ViewGroup parent) {

        if (convertView == null || convertView.getTag() == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.listview_item_download, null);
            holder = new ViewHolder();
            holder.ivopreate = convertView.findViewById(R.id.iv_downloadoperate);
            holder.statusText = (TextView) convertView.findViewById(R.id.tv_resourcename);
            holder.progressBar = convertView.findViewById(R.id.progressBar);
            holder.restypeicon = convertView.findViewById(R.id.iv_restype);
            holder.checkBox = convertView.findViewById(R.id.id_downloaded_check_box);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final DownloadEntry entry = arrlistentry.get(position);

        holder.statusText.setText(entry.toString());
        holder.progressBar.setProgress(entry.percent);
        holder.restypeicon.setBackgroundResource(resid+entry.fileType-1);
        setBtntext(entry);
        holder.ivopreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: "+arrlistentry.get(position).name+"	entry.status "+entry.status);
                if(entry.status == DownloadStatus.idle){
                    DownloadManager.getInstance(context).add(entry);
                }else if(entry.status == DownloadStatus.downloading){
                    DownloadManager.getInstance(context).pause(entry);
                }else if(entry.status == DownloadStatus.pause) {
                    DownloadManager.getInstance(context).resume(entry);
                }
                else if(entry.status == DownloadStatus.waiting)
                {
//						holder.ivopreate.setText("等待");
                    Toast.makeText(context,"等待中，请稍候...",Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.checkBox.setChecked(isSelected.get(position));
        holder.checkBox.setTag(position);
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                // TODO Auto-generated method stub
                int position = (Integer)buttonView.getTag();
                isSelected.set(position, isChecked);
            }
        });

        if (mbIsEdit) {
            holder.ivopreate.setVisibility(View.GONE);
            holder.checkBox.setVisibility(View.VISIBLE);
        }
        else {
            holder.ivopreate.setVisibility(View.VISIBLE);
            holder.checkBox.setVisibility(View.GONE);
        }
        
        return convertView;
    }

    public void setEditMode (boolean bIsEdit) {
        mbIsEdit = bIsEdit;
    }

    private void setBtntext(DownloadEntry entry)
    {
        if(entry.status == DownloadStatus.idle){
            holder.ivopreate.setBackgroundResource(R.drawable.download_start);
        }else if(entry.status == DownloadStatus.downloading){
            holder.ivopreate.setBackgroundResource(R.drawable.download_pause);
        }else if(entry.status == DownloadStatus.pause) {
            holder.ivopreate.setBackgroundResource(R.drawable.download_start);
        }
        else if(entry.status == DownloadStatus.waiting)
        {
            holder.ivopreate.setBackgroundResource(R.drawable.download_waiting);
        }
        else if(entry.status == DownloadStatus.done){
            holder.ivopreate.setBackgroundResource(R.drawable.download_file_open_to);
        }
    }

   public class ViewHolder {
        CheckBox checkBox;
        ImageView ivopreate;
        TextView statusText;
        ProgressBar progressBar;
        ImageView restypeicon;
    }
}