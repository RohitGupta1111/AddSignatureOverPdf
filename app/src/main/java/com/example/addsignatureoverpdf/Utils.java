package com.example.addsignatureoverpdf;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class Utils {
    private static final String TAG = "Utils";

    private static Utils sInstance = null;

    private Utils() {
    }

    public static Utils getInstace() {
        if (sInstance == null) {
            sInstance = new Utils();
        }

        return sInstance;
    }

    public File[] getFilesFromDir(String directory) {
        File dirFiles = new File(directory);
        File[] allFiles = dirFiles.listFiles();
        return allFiles;
    }

    public Bitmap getBitmapFromPdfFile(File file) {
        ParcelFileDescriptor fileDescriptor = null;
        Bitmap renderedBitmap = null;
        try {
            fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);

            PdfRenderer pdfRenderer = new PdfRenderer(fileDescriptor);
            renderedBitmap = createBitmapfromRenderer(pdfRenderer,0);
            pdfRenderer.close();
            fileDescriptor.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return renderedBitmap;
    }

    public Bitmap createBitmapfromRenderer(PdfRenderer pdfRenderer,int pageNumber) {
        Bitmap renderBitmap = null;
        try {
            PdfRenderer.Page page = pdfRenderer.openPage(pageNumber);
            Log.d(TAG, "height and width "+page.getWidth() + " -- " + page.getHeight());
            Log.d(TAG, "createBitmapfromRenderer: "+ ConvertDPItoPixel(page.getWidth()) + " -- " +  ConvertDPItoPixel(page.getHeight()));
            renderBitmap = Bitmap.createBitmap(ConvertDPItoPixel(Math.min(page.getWidth(),612)), ConvertDPItoPixel(Math.min(792,page.getHeight())), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(renderBitmap);
            canvas.drawColor(Color.WHITE);
            canvas.drawBitmap(renderBitmap, 0, 0, null);
            page.render(renderBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            page.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return renderBitmap;
    }

    private int ConvertDPItoPixel(int pixelvalue) {
        int newPixelvalue  = (pixelvalue / 72) * 200 ;
        return newPixelvalue;
    }

    public File copyFileFromUri(Uri uri, Context context) {
        FileOutputStream fos = null;
        InputStream in = null;
        File file = null;
        try {
            in = context.getContentResolver().openInputStream(uri);
            file = new File(context.getCacheDir(),"temp.pdf");
            fos = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                fos.write(buf,0,len);
            }
            return file;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if ( fos != null ) {
                    fos.close();
                }

                // If you want to close the "in" InputStream yourself then remove this
                // from here but ensure that you close it yourself eventually.
                in.close();
            }
            catch ( IOException e ) {
                e.printStackTrace();
            }
        }
        return file;
    }


//    private void generateBitmapTest() {
//        Bitmap currBitmap = fileBitmaps.get(currentPage);
//        Bitmap over = overBitmap.get(0);
//        getScaleofDrawnImage(currBitmap);
//        double currHeight = currBitmap.getHeight() * scaleY;
//        double currWidth = currBitmap.getWidth() * scaleX;
//        Log.d(TAG, "currHeight: " + currHeight);
//        Log.d(TAG, "currWidth: " + currWidth);
//        double heightLess = (mainPdfView.getHeight() - currHeight)/2.0;
//        double widthLess = (mainPdfView.getWidth() - currWidth) / 2.0;
//        double overX = overPdfView.getX();
//        double overY = overPdfView.getY();
//        double finalOverX = overX - widthLess;
//        double finalOverY = overY - heightLess;
//        Log.d(TAG, "finalOverX: " + finalOverX);
//        Log.d(TAG, "finalOverY: " + finalOverY);
//        double ratioWidth = overPdfView.getWidth()/currWidth;
//        double ratioHeight = overPdfView.getHeight()/currHeight;
//        Bitmap scaledBitmap = Bitmap.createScaledBitmap(over, (int)(currBitmap.getWidth() * ratioWidth), (int) (currBitmap.getHeight() * ratioHeight),false);
//        Bitmap bmOverlay =  Bitmap.createBitmap(currBitmap.getWidth(),currBitmap.getHeight(),currBitmap.getConfig());
//        Canvas canvas = new Canvas(bmOverlay);
//        canvas.drawBitmap(currBitmap,0,0,null);
//        canvas.drawBitmap(scaledBitmap,(float) finalOverX/scaleX,(float)finalOverY/scaleY,null);
//        mainPdfView.setImageBitmap(bmOverlay);
//        overPdfView.setVisibility(View.INVISIBLE);
//        stretchView.setVisibility(View.INVISIBLE);
//    }

//    public void shareFiles () {
//        String path1  = "storage/emulated/0/Download/book.pdf";
//        String path2  = "storage/emulated/0/Download/Frontend_developer.pdf";
//        Intent sharingIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
//        ArrayList<Uri> files = new ArrayList<>();
//        ArrayList<String> paths = new ArrayList<>();
//        paths.add(path1);
//        paths.add(path2);
//        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Here are some files.");
//        for(String path : paths) {
//            files.add(Uri.parse(path));
//            Log.d(TAG,files.get(files.size()-1).toString());
//        }
//
//        sharingIntent.setType("*/*");
//        sharingIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
//        //startActivity(Intent.createChooser(sharingIntent, "Share using"));
//    }
}