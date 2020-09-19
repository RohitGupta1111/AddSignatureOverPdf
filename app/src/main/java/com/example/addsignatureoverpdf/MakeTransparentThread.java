package com.example.addsignatureoverpdf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

public class MakeTransparentThread extends Thread {

    private static final String TAG = "MakeTransparentThread";

    private ArrayList<Bitmap> transparentBitmap;
    private String fileUri;
    private Utils mUtils;
    private Context context;
    private Handler looperHandler;
    int firstI = Integer.MAX_VALUE;
    int firstJ = Integer.MAX_VALUE;
    int lastI = Integer.MIN_VALUE;
    int lastJ = Integer.MIN_VALUE;

    MakeTransparentThread (String fileUri, ArrayList<Bitmap> transparentBitmap, Context context, Handler looperHandler) {
        this.fileUri = fileUri;
        this.transparentBitmap = transparentBitmap;
        mUtils = Utils.getInstace();
        this.context = context;
        this.looperHandler = looperHandler;
    }

    @Override
    public void run() {
        Log.d(TAG,"thread running");
        File imageFile = mUtils.copyFileFromUri(Uri.parse(fileUri),context);
        Bitmap originalBitmap = BitmapFactory.decodeFile(imageFile.getPath());
        transparentBitmap.add(getTransparentBitmap(originalBitmap));
        Message message = Message.obtain();
        message.what = Constants.MSG_TRANSPARENT_BITMAP_LOADED;
        Log.d(TAG, "over bitmap set");
        looperHandler.sendMessage(message);
    }

//    public Bitmap getTransparentBitmap(Bitmap originalBitmap) {
//        int scaledwidth = 300;
//        int new_height = findScaledRatioHeight(originalBitmap,scaledwidth);
//        Bitmap scaled_bitmap = Bitmap.createScaledBitmap(originalBitmap,scaledwidth,new_height,false);
//        Bitmap ntrans_bitmap = scaled_bitmap.copy( Bitmap.Config.ARGB_8888 , true);
//        ntrans_bitmap.setHasAlpha(true);
//        Log.d("MergerActivity",String.valueOf(ntrans_bitmap.getWidth()));
//        Log.d("MergerActivity",String.valueOf(ntrans_bitmap.getHeight()));
//        ColorPixel[][] diffArray = generateDiffImage(ntrans_bitmap,ntrans_bitmap.getHeight(),ntrans_bitmap.getWidth());
//        Bitmap transparentBitmap = setTransparency(ntrans_bitmap,diffArray,ntrans_bitmap.getHeight(),ntrans_bitmap.getWidth());
//        if(checkValidforCrop(transparentBitmap)) {
//            Log.d(TAG, "crop imaage");
//            transparentBitmap = Bitmap.createBitmap(transparentBitmap, firstI,firstJ,lastI - firstI,lastJ - firstJ);
//        }
//        return transparentBitmap;
//    }

    public Bitmap getTransparentBitmap(Bitmap originalBitmap) {
        int scaledwidth = 600;
        int new_height = findScaledRatioHeight(originalBitmap,scaledwidth);
        Bitmap scaled_bitmap = Bitmap.createScaledBitmap(originalBitmap,scaledwidth,new_height,false);
        ColorPixel[][] diffArray1 = generateDiffImage(scaled_bitmap,scaled_bitmap.getHeight(),scaled_bitmap.getWidth());
        cropBitmap(scaled_bitmap,diffArray1,scaled_bitmap.getHeight(),scaled_bitmap.getWidth());
        Bitmap croppedBitmap = originalBitmap;

        if(checkValidforCrop(scaled_bitmap)) {
            Log.d(TAG,"Cropping the image");
            double scaledWidthRatio = Double.valueOf(originalBitmap.getWidth()) / Double.valueOf(scaledwidth);
            double scaledHeightRatio = Double.valueOf(originalBitmap.getHeight())/ Double.valueOf(new_height);
            firstI = (int)(scaledWidthRatio * firstI);
            lastI = (int) (scaledWidthRatio*lastI);
            firstJ = (int) (scaledHeightRatio * firstJ);
            lastJ = (int) (scaledHeightRatio * lastJ);
            croppedBitmap = Bitmap.createBitmap(originalBitmap, firstI,firstJ,lastI - firstI,lastJ - firstJ);
        }

        new_height = findScaledRatioHeight(croppedBitmap,scaledwidth);
        scaled_bitmap = Bitmap.createScaledBitmap(croppedBitmap,scaledwidth,new_height,false);
        Bitmap ntrans_bitmap = scaled_bitmap.copy( Bitmap.Config.ARGB_8888 , true);
        ntrans_bitmap.setHasAlpha(true);
        Log.d("MergerActivity",String.valueOf(ntrans_bitmap.getWidth()));
        Log.d("MergerActivity",String.valueOf(ntrans_bitmap.getHeight()));
        ColorPixel[][] diffArray = generateDiffImage(ntrans_bitmap,ntrans_bitmap.getHeight(),ntrans_bitmap.getWidth());
        Bitmap transparentBitmap = setTransparency(ntrans_bitmap,diffArray,ntrans_bitmap.getHeight(),ntrans_bitmap.getWidth());

        return transparentBitmap;
    }

    boolean checkValidforCrop(Bitmap bitmap) {
        Log.d(TAG, firstI + " --" + lastI + "--" + firstJ + "--" + lastJ);
        int cropWidth = lastI - firstI;
        int cropHeight = lastJ - firstJ;
        if(firstI > 0 && lastI<=bitmap.getWidth() && firstJ > 0 && lastJ <= bitmap.getHeight() && cropHeight > 0 && cropWidth  >0 ) {
            return true;
        }

        return false;
    }

    private int findScaledRatioHeight(Bitmap bitmap,int scaledwidth){
        double aspectratio =  Double.valueOf(bitmap.getWidth())/Double.valueOf(bitmap.getHeight());
        int newHeight = (int)(scaledwidth / aspectratio) ;
        return newHeight;
    }

    public ColorPixel[][] generateDiffImage(Bitmap bitmap,int height,int width)  {
        ColorPixel diffArray[][] = new ColorPixel[width][height];
        ColorPixel topleft = getpixelvalue(bitmap,0,0);
        Log.d("TAG","alpha--"+topleft.alpha+"red--"+topleft.red+"green--"+topleft.green+"blue--"+topleft.blue);
        for(int i=0;i<width;i++){
            for(int j=0;j<height;j++){
                ColorPixel pixel = getpixelvalue(bitmap,i,j);
                int red = topleft.red - pixel.red;
                int green = topleft.green - pixel.green;
                int blue = topleft.blue - pixel.blue;
                ColorPixel newColorPixel = new ColorPixel(red,green,blue,255);
                diffArray[i][j] = newColorPixel;
                //diffArray[i][j] = new ColorPixel(0,0,0,255);
//                    diffArray[i][j].red = red;
//                    diffArray[i][j].blue = blue;
//                    diffArray[i][j].green = green;
            }
        }
        return diffArray;
    }

    public ColorPixel getpixelvalue(Bitmap bitmap,int x,int y){

        int pixel = bitmap.getPixel(x,y);
        int alpha = (pixel>>24) & 0xff;
        int red = (pixel>>16) & 0xff;
        int green = (pixel>>8) & 0xff;
        int blue = (pixel) & 0xff;
        ColorPixel color = new ColorPixel(red,green,blue,alpha);
        return color;
    }

    public Bitmap setTransparency(Bitmap bitmap,ColorPixel[][] diffArray,int height,int width){
        for(int i=0;i<width;i++){
            for(int j=0;j<height;j++){
                ColorPixel diffvalue = diffArray[i][j];

                boolean r = (diffvalue.red<25) && (diffvalue.red>-25);
                boolean g = (diffvalue.green<25) && (diffvalue.green>-25);
                boolean b = (diffvalue.blue<25) && (diffvalue.blue>-25);

                if(r && g && b){

                    ColorPixel currentPixel = getpixelvalue(bitmap,i,j);
                    int a = 0;
                    int updatedPixel = (a & 0xff) << 24 | (currentPixel.red & 0xff) << 16 | (currentPixel.green & 0xff) << 8 | (currentPixel.blue & 0xff);
                    //      int updatedPixel  = (a<<24) | (0<<16) | (255<<8) | (0);
                    bitmap.setPixel(i,j,updatedPixel);
                } else {
                    firstI = Math.min(i,firstI);
                    firstJ = Math.min(j,firstJ);
                    lastI = Math.max(i,lastI);
                    lastJ = Math.max(j,lastJ);
                }
            }
        }

        return bitmap;
    }

    public void cropBitmap(Bitmap bitmap,ColorPixel[][] diffArray,int height,int width){
        for(int i=0;i<width;i++){
            for(int j=0;j<height;j++){
                ColorPixel diffvalue = diffArray[i][j];

                boolean r = (diffvalue.red<25) && (diffvalue.red>-25);
                boolean g = (diffvalue.green<25) && (diffvalue.green>-25);
                boolean b = (diffvalue.blue<25) && (diffvalue.blue>-25);

                if(!r || !g || !b) {
                    firstI = Math.min(i, firstI);
                    firstJ = Math.min(j, firstJ);
                    lastI = Math.max(i, lastI);
                    lastJ = Math.max(j, lastJ);
                }
            }
        }

        return;
    }
}
