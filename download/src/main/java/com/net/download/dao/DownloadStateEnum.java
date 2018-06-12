package com.net.download.dao;

/**
 * Created by Frank on 2018/3/12.
 */

public class DownloadStateEnum {

    //0表示未开始下载
    public static final int undownLoad = 0;
    //1表示已下载完成
    public static final int downloadComplete = 1;
    //2表示已开始下载
    public static final int downInProgress = 2;
    //3表示下载暂停
    public static final int downLoadPause = 3;

}
