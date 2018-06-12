package com.net.download;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by Frank on 2018/3/12.
 */

@DatabaseTable(tableName = "downloadentry")
public class DownloadEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    @DatabaseField(id = true)
    public String url;

    @DatabaseField
    public String name;

    @DatabaseField
    public int currentLength;

    @DatabaseField
    public int totalLength;

    @DatabaseField
    public DownloadStatus status = DownloadStatus.idle;

    @DatabaseField
    public boolean isSupportRange;

    @DatabaseField(dataType = DataType.SERIALIZABLE)
    public HashMap<Integer, Integer> ranges;

    @DatabaseField
    public int percent;

    @DatabaseField
    public String downloadpath;

    @DatabaseField
    public int fileType;



    public DownloadEntry(String url) {
        this.url = url;
    }

    public DownloadEntry() {}

    @Override
    public String toString() {
        return name + " is " + status.name() + " with " + currentLength + "/" + totalLength + " " + percent +"%";
//		return name + "==" + percent
//				+ "%==" + status.name();
    }

    @Override
    public boolean equals(Object o) {
        return o.hashCode() == this.hashCode();
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }

    public void reset() {
        currentLength = 0;
        percent = 0;
        ranges = null;
//        String path = DownloadConfig.DOWNLOAD_PATH + name;
//        String path = DownloadConfig.DOWNLOAD_PATH + name;
//        File file = new File(path);
//        if(file.exists()){
//            file.delete();
//        }
    }



}
