package com.example.addsignatureoverpdf;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.util.Pair;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

public class EditorActivity extends AppCompatActivity implements View.OnTouchListener,ChooseFileTypeDialog.FileTypeDialogListener {

    private static final String TAG = "EditorActivity";

    private String fileUri;
    private ImageView overPdfView,stretchView,mainPdfView;
    private boolean isCurrentStretching = false;
    private boolean isCurrentMoving = false;
    private int currWidth,xDeltaforMoving,yDeltaforMoving;
    private int firstXforStretch,firstYforStretch;
    private EditLooperThread looperThread;
    private ArrayList<Bitmap> fileBitmaps,overBitmap;
    private Handler editMainHandler;
    private int currentPage=0,totalPage,pageLoaded;
    private final int imageFileType = 0;
    private final int pdFileType = 1;
    private final int FILE_REQUEST_CODE  = 101;
    private float scaleX,scaleY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        fileUri = getIntent().getStringExtra("uriselected");
        Log.d(TAG,fileUri.toString());
        mainPdfView = findViewById(R.id.main_pdf_view);
        overPdfView = findViewById(R.id.over_pdf_view);
        stretchView = findViewById(R.id.stretch_view);
        overPdfView.setOnTouchListener(this);
        stretchView.setOnTouchListener(this);
        fileBitmaps = new ArrayList<>();

        editMainHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                Log.d(TAG, "handleMessage: ");
                Bundle bundle = msg.getData();
                switch (msg.what) {
                    case Constants.MSG_PAGE_COUNT:
                        totalPage = bundle.getInt(Constants.PAGE_COUNT_KEY);
                        Log.d(TAG, "totalPage: "+ totalPage);
                        break;
                    case Constants.MSG_PAGE_BITMAP_LOADED:
                        pageLoaded = bundle.getInt(Constants.BITMAP_PAGE_KEY);
                        Log.d(TAG, "filebitmapsize:" + fileBitmaps.size());
                        Log.d(TAG, "currentPage: " + currentPage + " pageloaded: " + pageLoaded);
                        if(currentPage == pageLoaded && pageLoaded < fileBitmaps.size()) {
                            Log.d(TAG, "main bitmap set");
                            mainPdfView.setImageBitmap(fileBitmaps.get(pageLoaded));
                            getScaleofDrawnImage(fileBitmaps.get(pageLoaded));
                        }
                        break;
                    case Constants.MSG_TRANSPARENT_BITMAP_LOADED:
                        Log.d(TAG, "over bitmap received: ");
                        overPdfView.setVisibility(View.VISIBLE);
                        stretchView.setVisibility(View.VISIBLE);
                        overPdfView.setImageBitmap(overBitmap.get(0));
                        Log.d(TAG, "bitmap getWidth:" + overPdfView.getWidth());
                        Log.d(TAG, "bitmap getHeight:" + overPdfView.getHeight());
                        break;
                }
            }
        };

        looperThread = new EditLooperThread(editMainHandler,fileUri,fileBitmaps,EditorActivity.this);
        looperThread.start();
        Message message = new Message();
        message.what = Constants.MSG_RENDER_BITMAP_FROM_URI;
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        looperThread.mLooperHandler.sendMessage(message);
    }


    private void getScaleofDrawnImage(Bitmap bitmap) {
        Matrix imageMatrix = mainPdfView.getImageMatrix();
        float []f = new float[9];
        imageMatrix.getValues(f);
        scaleX = f[Matrix.MSCALE_X];
        scaleY = f[Matrix.MSCALE_Y];

        Log.d(TAG, "scaleX: " + scaleX);
        Log.d(TAG, "scaleY: " + scaleY);
        Log.d(TAG, "imageview height : " + mainPdfView.getHeight());
        Log.d(TAG, "imageview width: " + mainPdfView.getWidth());
        Log.d(TAG, "intrinsic width: " + mainPdfView.getDrawable().getIntrinsicWidth());
        Log.d(TAG, "intrinsic height: " + mainPdfView.getDrawable().getIntrinsicHeight());
        Log.d(TAG, "bitmap width: "+ bitmap.getWidth());
        Log.d(TAG, "bitmap height:" + bitmap.getHeight());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_sign_file:
                FragmentManager fm = getSupportFragmentManager();
                ChooseFileTypeDialog fileTypeDialog = ChooseFileTypeDialog.newInstance(getString(R.string.select_file_type));
                fileTypeDialog.show(fm,"fragment_edit_name");
                break;
            case R.id.edit_done:
        }

        return true;
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        looperThread.mLooperHandler.getLooper().quit();
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final int x = (int)event.getRawX();
        final int y = (int) event.getRawY();
        if(v.getId() == R.id.stretch_view) {
            isCurrentStretching = true;
            isCurrentMoving = false;
        } else if (v.getId() == R.id.over_pdf_view) {
            isCurrentMoving = true;
            isCurrentStretching = false;
        } else {
            isCurrentMoving = false;
            isCurrentStretching = false;
        }
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    if (isCurrentStretching) {
                        firstXforStretch = x;
                        firstYforStretch = y;
                        Log.d(TAG,"firstX-->"+firstXforStretch);
                        currWidth = overPdfView.getLayoutParams().width;
                        Log.d(TAG,"currWidth-->"+currWidth);
                    } else if(isCurrentMoving) {
                        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) v.getLayoutParams();
                        Log.d(TAG,"firstX-->" + (int)event.getX());
                        Log.d(TAG,"firstY-->" + (int)event.getY());
                        Log.d(TAG,"leftMargin-->" + overPdfView.getLeft());
                        Log.d(TAG,"topMargin" + overPdfView.getTop());
                        xDeltaforMoving = (int)event.getX() ;
                        yDeltaforMoving = (int)event.getY() ;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if(isCurrentStretching) {
                        int diffX;
                        boolean isDecSize;
                        Log.d(TAG,"X-->" + x);
                        if(firstXforStretch<=x) {
                            diffX = x -firstXforStretch;
                            isDecSize = false;
                        } else {
                            diffX = firstXforStretch - x;
                            isDecSize = true;
                        }
                        Log.d(TAG,"diffX-->" + diffX);
                        if(isDecSize) {
                            overPdfView.getLayoutParams().width = currWidth - diffX;
                        } else {
                            overPdfView.getLayoutParams().width = currWidth + diffX;
                        }
                        overPdfView.requestLayout();
                        break;
                    } else if (isCurrentMoving) {
//                        int temp = (int) (event.getX() - xDeltaforMoving);
//                        int temp1 = (int) (event.getY() - yDeltaforMoving);
//                        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) overPdfView.getLayoutParams();
//                        layoutParams.leftMargin = layoutParams.leftMargin + temp;
//                        layoutParams.topMargin = layoutParams.topMargin + temp1;
//                        layoutParams.rightMargin = -250;
//                        layoutParams.bottomMargin = -250;
//                        overPdfView.setLayoutParams(layoutParams);
//                        Log.d(TAG,"xPos-->" + (x - xDeltaforMoving));
//                        Log.d(TAG,"yPos-->"+(y - yDeltaforMoving));
                        int temp = (int) (event.getX() - xDeltaforMoving);
                        int temp1 = (int) (event.getY() - yDeltaforMoving);
                        overPdfView.setX(v.getX() + temp);
                        overPdfView.setY(v.getY() + temp1);
                        Log.d(TAG, "setX: " + v.getX() + temp);
                        Log.d(TAG, "setY" + v.getY() + temp1);
                        stretchView.setX(stretchView.getX() + temp);
                        stretchView.setY(stretchView.getY() + temp1);
                        overPdfView.invalidate();
                    }

                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
        return true;
    }

    @Override
    public void onDialogClicked(int id) {
        openFileExplorer(id);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri;
        if(requestCode == FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            uri = data.getData();
            overBitmap = new ArrayList<>();
            looperThread.setTransparentThreadArgs(uri.toString(),overBitmap);
            Message message = Message.obtain();
            message.what = Constants.MSG_TRANSPARENT_BITMAP;
            looperThread.mLooperHandler.sendMessage(message);
        }
    }

    private void openFileExplorer (int type) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        if(type == imageFileType) {
            intent.setType("image/*");
        } else if(type == pdFileType) {
            intent.setType("application/pdf");
        }

        startActivityForResult(intent,FILE_REQUEST_CODE);
    }
}
