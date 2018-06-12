package com.net.download.dao;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.widget.ImageView;

import com.vincestyling.netroid.ExecutorDelivery;
import com.vincestyling.netroid.Network;
import com.vincestyling.netroid.Request;
import com.vincestyling.netroid.RequestPerformer;
import com.vincestyling.netroid.RequestQueue;
import com.vincestyling.netroid.cache.DiskCache;
import com.vincestyling.netroid.stack.HttpClientStack;
import com.vincestyling.netroid.stack.HttpStack;
import com.vincestyling.netroid.stack.HurlStack;
import com.vincestyling.netroid.toolbox.BasicNetwork;
import com.vincestyling.netroid.toolbox.FileDownloader;
import com.vincestyling.netroid.toolbox.ImageLoader;
import com.vincestyling.netroid.widget.NetworkImageView;

import org.apache.http.protocol.HTTP;

import java.util.concurrent.Executor;

//import com.netroid.Network;
//import com.netroid.RequestQueue;
//import com.netroid.cache.DiskCache;
//import com.netroid.stack.HttpClientStack;
//import com.netroid.stack.HttpStack;
//import com.netroid.stack.HurlStack;
//import com.netroid.toolbox.BasicNetwork;


public class Netroid {


	public static final String USER_AGENT = "netroid_sample";
	private RequestQueue mRequestQueue;
	private ImageLoader mImageLoader;
	private Network mNetwork;
	private DiskCache mDiskCache;
	private FileDownloader mFileDownloader;

	private Netroid() {
        /* cannot be instantiated */
	}

	private static Netroid mInstance;

	public static void init(DiskCache cache) {
		mInstance = new Netroid();

		mInstance.mDiskCache = cache;

		HttpStack stack;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			stack = new HurlStack(USER_AGENT, null);
		} else {
			// Prior to Gingerbread, HttpUrlConnection was unreliable.
			// See: http://android-developers.blogspot.com/2011/09/androids-http-clients.html
			stack = new HttpClientStack(USER_AGENT);
		}

		mInstance.mNetwork = new BasicNetwork(stack, HTTP.UTF_8);
		int poolSize = RequestQueue.DEFAULT_NETWORK_THREAD_POOL_SIZE;
		mInstance.mRequestQueue = new RequestQueue(mInstance.mNetwork, poolSize, cache);
		mInstance.mRequestQueue.start();
	}

	public static RequestQueue getRequestQueue() {
		if (mInstance.mRequestQueue != null) {
			return mInstance.mRequestQueue;
		} else {
			throw new IllegalStateException("RequestQueue not initialized");
		}
	}

	public static void add(Request request) {
		getRequestQueue().add(request);
	}

	/**
	 * Perform given request as blocking mode. Note make sure won't invoke on main thread.
	 */
	public static void perform(Request request) {
		// you might want to keep the ExecutorDelivery instance as Field, but it's
		// cheap constructing every time, depends how often you use this way.
		RequestPerformer.perform(request, Netroid.getNetwork(), new ExecutorDelivery(new Executor() {
			@Override
			public void execute(Runnable command) {
				// invoke run() directly.
				command.run();
			}
		}));
	}

	public static void setImageLoader(ImageLoader imageLoader) {
		mInstance.mImageLoader = imageLoader;
	}

	public static ImageLoader getImageLoader() {
		if (mInstance.mImageLoader != null) {
			return mInstance.mImageLoader;
		} else {
			throw new IllegalStateException("ImageLoader not initialized");
		}
	}

	public static Network getNetwork() {
		if (mInstance.mNetwork != null) {
			return mInstance.mNetwork;
		} else {
			throw new IllegalStateException("Network not initialized");
		}
	}

	public static DiskCache getDiskCache() {
		return mInstance.mDiskCache;
	}

	public static void setFileDownloder(FileDownloader downloder) {
		mInstance.mFileDownloader = downloder;
	}

	public static FileDownloader getFileDownloader() {
		if (mInstance.mFileDownloader != null) {
			return mInstance.mFileDownloader;
		} else {
			throw new IllegalStateException("FileDownloader not initialized");
		}
	}

	public static void displayImage(String url, ImageView imageView, int defaultImageResId, int errorImageResId) {
		ImageLoader.ImageListener listener = ImageLoader.getImageListener(imageView, defaultImageResId, errorImageResId);
		getImageLoader().get(url, listener, 0, 0);
	}

	public static void displayImage(String url, ImageView imageView) {
		displayImage(url, imageView, 0, 0);
	}

	public static void displayImage(String url, NetworkImageView imageView, int defaultImageResId, int errorImageResId) {
		imageView.setDefaultImageResId(defaultImageResId);
		imageView.setErrorImageResId(errorImageResId);
		imageView.setImageUrl(url, getImageLoader());
	}

	public static void displayImage(String url, NetworkImageView imageView) {
		displayImage(url, imageView, 0, 0);
	}

	public static void destroy() {
		if (mInstance.mRequestQueue != null) {
			mInstance.mRequestQueue.stop();
			mInstance.mRequestQueue = null;
		}

		if (mInstance.mFileDownloader != null) {
			mInstance.mFileDownloader.clearAll();
			mInstance.mFileDownloader = null;
		}

		mInstance.mNetwork = null;
		mInstance.mImageLoader = null;
		mInstance.mDiskCache = null;
	}

    /**
     * Creates a default instance of the worker pool and calls {@link RequestQueue#start()} on it.
     * @param context A {@link Context} to use for creating the cache dir.
     * @return A started {@link RequestQueue} instance.
     */
    public static RequestQueue newRequestQueue(Context context, DiskCache cache) {
		int poolSize = RequestQueue.DEFAULT_NETWORK_THREAD_POOL_SIZE;

		HttpStack stack;
		String userAgent = "netroid/0";
		try {
			String packageName = context.getPackageName();
			PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
			userAgent = packageName + "/" + info.versionCode;
		} catch (NameNotFoundException e) {
		}
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			stack = new HurlStack(userAgent, null);
		} else {
			// Prior to Gingerbread, HttpUrlConnection was unreliable.
			// See: http://android-developers.blogspot.com/2011/09/androids-http-clients.html
			stack = new HttpClientStack(userAgent);
		}

		Network network = new BasicNetwork(stack, HTTP.UTF_8);
		RequestQueue queue = new RequestQueue(network, poolSize, cache);
		queue.start();

        return queue;
    }


}
