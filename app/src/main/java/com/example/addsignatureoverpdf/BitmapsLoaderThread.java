package com.example.addsignatureoverpdf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class BitmapsLoaderThread extends Thread {

    private static final String TAG = "BitmapsLoaderThread";
    
    private Handler looperThreadHandler;
    private Uri fileUri;
    private ArrayList<Bitmap> bitmapArrayList;
    private Context context;
    private Utils mUtils;
    private boolean isRunning;

    BitmapsLoaderThread(Handler looperThreadHandler, String fileUri, ArrayList<Bitmap> bitmapArrayList, Context context) {
        this.looperThreadHandler = looperThreadHandler;
        this.fileUri = Uri.parse(fileUri);
        this.bitmapArrayList = bitmapArrayList;
        this.context = context;
        mUtils = Utils.getInstace();
        this.isRunning = true;
    }
    @Override
    public void run() {
        deleteCacheDir();
        Log.d(TAG, "run: ");
        File currentFile = mUtils.copyFileFromUri(fileUri,context);
        ParcelFileDescriptor fileDescriptor = null;
        Bitmap renderedBitmap;
        try {
            fileDescriptor = ParcelFileDescriptor.open(currentFile, ParcelFileDescriptor.MODE_READ_ONLY);

            PdfRenderer pdfRenderer = new PdfRenderer(fileDescriptor);
            final int pageCount = pdfRenderer.getPageCount();
            Log.d(TAG, "pageCount: " + pageCount);
            sendMessagetoLooperThread(Constants.MSG_PAGE_COUNT,Constants.PAGE_COUNT_KEY,pageCount);
            for(int i=0;i<pageCount && isRunning;i++) {
                renderedBitmap = mUtils.createBitmapfromRenderer(pdfRenderer,i);
                    bitmapArrayList.add(renderedBitmap);


                Log.d(TAG,bitmapArrayList.size() + "size");
                sendMessagetoLooperThread(Constants.MSG_PAGE_BITMAP_LOADED,Constants.BITMAP_PAGE_KEY,i);
            }

            pdfRenderer.close();
            fileDescriptor.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG,"size--" + bitmapArrayList.size()  );
    }

    private void deleteCacheDir() {

    }

    public void stopThread() {
        this.isRunning = false;
    }

    private void sendMessagetoLooperThread(int what,String key , int cnt) {
        Message msg = Message.obtain();
        msg.what = what;
        Bundle bundle = new Bundle();
        bundle.putInt(key,cnt);
        msg.setData(bundle);
        if(isRunning) {
            looperThreadHandler.sendMessage(msg);
        }

    }

}
