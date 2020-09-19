package com.example.addsignatureoverpdf;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ChooseFileTypeDialog.FileTypeDialogListener {

    private static final String TAG = "MainActivity";

    private RecyclerView fileListRecyclerView;
    private FloatingActionButton editPdfFab;
    private ChooseFileTypeDialog fileTypeDialog;
    private Utils mUtils;
    private FileListAdapter fileListAdapter;
    private ArrayList<FileDetails> fileDetailsList;
    private String appFilesDir = null;
    private final int FILE_REQUEST_CODE  = 100;

    private int imageFileType = 0;
    private int pdFileType = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editPdfFab = findViewById(R.id.edit_pdf_fab);
        fileListRecyclerView = findViewById(R.id.main_recycler_view);
        editPdfFab.setOnClickListener(this);
        mUtils = Utils.getInstace();
        appFilesDir = String.valueOf(getExternalFilesDir(null));
        File[] allfiles = mUtils.getFilesFromDir(appFilesDir);
        fileDetailsList = updateUIwithFiles(allfiles);
        fileListAdapter = new FileListAdapter(fileDetailsList);
        fileListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        fileListRecyclerView.setAdapter(fileListAdapter);
        PreviewLoaderThread previewLoaderThread = new PreviewLoaderThread(fileDetailsList,appFilesDir,fileListAdapter);
        previewLoaderThread.start();
    }

    private ArrayList<FileDetails> updateUIwithFiles(File []allFiles) {
        ArrayList<FileDetails> fileDetailsArrayList = new ArrayList<>();
        if(allFiles!=null) {
            for(int i=0;i<allFiles.length;i++) {
                fileDetailsArrayList.add(new FileDetails(allFiles[i].getName(),new Date(allFiles[i].lastModified()),null));
            }
        }

        return fileDetailsArrayList;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.edit_pdf_fab:
                FragmentManager fm = getSupportFragmentManager();
                fileTypeDialog = ChooseFileTypeDialog.newInstance(getString(R.string.select_file_type));
                fileTypeDialog.show(fm,"fragment_edit_name");
                break;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri uri;
            if(data!=null) {
                uri = data.getData();
                Intent i = new Intent(MainActivity.this,EditorActivity.class);
                i.putExtra("uriselected",uri.toString());
                startActivity(i);
            }
        }
    }


    @Override
    public void onDialogClicked(int id) {
        //fileTypeDialog.dismiss();
        openFileExplorer(id);
    }
}

//    Uri uri;
//if (data != null) {
//        uri = data.getData();
//
//        File file = copyFileFromUri(uri);
//
//        ParcelFileDescriptor fileDescriptor = null;
//        try {
//        fileDescriptor = ParcelFileDescriptor.open(file,ParcelFileDescriptor.MODE_READ_ONLY);
//
//        PdfRenderer pdfRenderer = new PdfRenderer(fileDescriptor) ;
//        final int PageCount = pdfRenderer.getPageCount();
//        PdfRenderer.Page page = pdfRenderer.openPage(0);
//        Bitmap bitmap = Bitmap.createBitmap(page.getWidth(),page.getHeight(),Bitmap.Config.ARGB_8888) ;
//        Canvas canvas = new Canvas(bitmap);
//        canvas.drawColor(Color.WHITE);
//        canvas.drawBitmap(bitmap, 0, 0, null);
//        page.render(bitmap,null,null,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
//        page.close();
//        pdfRenderer.close();
//        fileDescriptor.close();
//        Log.d(TAG,"bitmap created");
//        Log.d(TAG,"bitmap set");
//        }  catch (IOException e) {
//        e.printStackTrace();
//        }
//
//
//        }