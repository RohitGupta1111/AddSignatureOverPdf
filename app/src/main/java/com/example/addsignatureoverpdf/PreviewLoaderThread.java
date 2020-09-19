package com.example.addsignatureoverpdf;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;

public class PreviewLoaderThread extends Thread {

    private static final String TAG = PreviewLoaderThread.class.getSimpleName();

    private ArrayList<FileDetails> fileDetailsArrayList;
    private Handler mMainHandler;
    private String appFilesDirectory;
    private Utils mUtils;
    FileListAdapter fileListAdapter;
    private String messageObjectKey = "IndexBitmap";

    public PreviewLoaderThread (ArrayList<FileDetails> fileDetailsArrayList, String appFilesDirectory, final FileListAdapter fileListAdapter) {
        this.fileDetailsArrayList = fileDetailsArrayList;
        this.appFilesDirectory = appFilesDirectory;
        this.fileListAdapter = fileListAdapter;
        mUtils = Utils.getInstace();
        mMainHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                Log.d(TAG,msg.obj + "");
                Bundle bundle = msg.getData();
                int index = bundle.getInt(messageObjectKey);
                fileListAdapter.notifyItemChanged(index);
            }
        };
    }

    @Override
    public void run() {
        super.run();
        File[] allFiles = mUtils.getFilesFromDir(appFilesDirectory);
        if(allFiles != null) {
            for(int i=0;i<allFiles.length;i++) {
                Bitmap currentBitmap = mUtils.getBitmapFromPdfFile(allFiles[i]);
                Bitmap resized = ThumbnailUtils.extractThumbnail(currentBitmap, 800, 800);
                if(currentBitmap!=null) {
                    if(fileDetailsArrayList!=null && i < fileDetailsArrayList.size() ) {
                        fileDetailsArrayList.get(i).setFileBitmap(resized);
                        Message msg = getMessage(i);
                        mMainHandler.sendMessage(msg);
                    }
                }
            }
        }
    }

    private Message getMessage (int i) {
        Message msg = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putInt(messageObjectKey,i);
        msg.setData(bundle);
        return msg;
    }
}
