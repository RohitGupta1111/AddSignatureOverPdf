package com.example.addsignatureoverpdf;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class EditLooperThread extends Thread {

    private static final String TAG = "EditLooperThread";

    public Handler mLooperHandler;
    private Handler editMainHandler;
    private String fileUri,transUri;
    ArrayList<Bitmap> transBitmap;
    private ArrayList<Bitmap> bitmapArrayList;
    private Context context;
    private Utils mUtils;
    private BitmapsLoaderThread bitmapsLoaderThread = null;
    private MakeTransparentThread makeTransparentThread = null;

    EditLooperThread(Handler editMainHandler, String fileUri, ArrayList<Bitmap> bitmapArrayList, Context context) {
        this.editMainHandler = editMainHandler;
        this.fileUri = fileUri;
        this.bitmapArrayList = bitmapArrayList;
        this.context = context;
        mUtils = Utils.getInstace();
    }

    @Override
    public void run() {
        Looper.prepare();
        mLooperHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                //super.handleMessage(msg);
                Message message = Message.obtain();
                Bundle bundle = new Bundle();
                switch(msg.what) {
                    case Constants.MSG_RENDER_BITMAP_FROM_URI:
                        //apply wait here instead of while
                        Log.d(TAG, "handleMessage: messsage");
                        bitmapsLoaderThread = new BitmapsLoaderThread(mLooperHandler,fileUri,bitmapArrayList,context);
                        bitmapsLoaderThread.start();
                        break;
                    case  Constants.MSG_PAGE_BITMAP_LOADED:
                        Log.d(TAG, "handleMessage: BITMAP_LOADED");
                        message.what = Constants.MSG_PAGE_BITMAP_LOADED;
                        Log.d(TAG,msg.getData().getInt(Constants.BITMAP_PAGE_KEY) + "bitmap_loaded");
                        bundle.putInt(Constants.BITMAP_PAGE_KEY,msg.getData().getInt(Constants.BITMAP_PAGE_KEY));
                        message.setData(bundle);
                        editMainHandler.sendMessage(message);
                        break;
                    case Constants.MSG_PAGE_COUNT:
                        Log.d(TAG, "handleMessage: PAGE_COUNT");
                        message.what = Constants.MSG_PAGE_COUNT;
                        Log.d(TAG,msg.getData().getInt(Constants.PAGE_COUNT_KEY) + "pagecount");
                        bundle.putInt(Constants.PAGE_COUNT_KEY,msg.getData().getInt(Constants.PAGE_COUNT_KEY));
                        message.setData(bundle);
                        editMainHandler.sendMessage(message);
                        break;
                    case Constants.MSG_TRANSPARENT_BITMAP:
                        makeTransparentThread = new MakeTransparentThread(transUri,transBitmap,context,mLooperHandler);
                        makeTransparentThread.start();
                        break;
                    case Constants.MSG_TRANSPARENT_BITMAP_LOADED:
                        Log.d(TAG, "over bitmap mid");
                        message.what = Constants.MSG_TRANSPARENT_BITMAP_LOADED;
                        editMainHandler.sendMessage(message);
                        break;
                }
            }
        };
        Looper.loop();
    }

    public void setTransparentThreadArgs(String transUri,ArrayList<Bitmap> transBitmap) {
        this.transUri = transUri;
        this.transBitmap = transBitmap;
    }


}
