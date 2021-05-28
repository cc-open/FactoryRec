package com.example.factoryrec.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.util.Log;

import com.example.factoryrec.R;
import com.example.factoryrec.app.Fragment_Result;
import com.example.factoryrec.app.MainActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PdfCreator {

    private MainActivity mActivity;
    private ProductItem mItem;
    private boolean mShowLogo, mShowTitle, mShowFooter, mShowWatermark;
    private final int mScreenWidth = 720;
    private final int mScreenHeight = 1480;

    private int mBitmapWidth = 70;
    private int mBitmapHeight = 70;

    private int mPageWidth = 1600;
    private int mPageHeight = 2400;

    public PdfCreator(MainActivity activity, Fragment_Result result) {
        mActivity = activity;
        mItem = result.getItem();
//        mScreenWidth = mActivity.getWindowManager().getDefaultDisplay().getWidth();
//        mScreenHeight = mActivity.getWindowManager().getDefaultDisplay().getHeight();
//        Log.i("cc", "mScreenWidth = " + mScreenWidth + ", mScreenHeight = " + mScreenHeight);
    }

    public void generatePdf(boolean showLogo, boolean showTitle, boolean showFooter, boolean showWatermark) {
        mShowLogo = showLogo;
        mShowTitle = showTitle;
        mShowFooter = showFooter;
        mShowWatermark = showWatermark;
        PdfDocument document = new PdfDocument();//1.建立PdfDocument
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(mScreenWidth, mScreenHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);//2.建立新的page

//        drawPdfCanvas(page.getCanvas());
        drawPdf(page.getCanvas());
        document.finishPage(page);

        String pdfName = mItem.getCustomer() + " " + mItem.getMachineType() + " " + mItem.getBadPhenom2() + "不良解析报告" + getTime() + ".pdf";
        String path = mActivity.getApplicationContext().getExternalFilesDir(null) + "/" + getTime() + "/" + pdfName;
//        String path = mActivity.getApplicationContext().getExternalFilesDir(null) + "/table1.pdf";
        Log.i("cc", "path = " + path);
        System.out.println(path);
        File file = new File(path);
        if (!file.exists()) {
            if (file.mkdirs()) {
                System.out.println(1);
            } else
                System.out.println(0);
        }
        if (file.exists()) {
            file.delete();
        }
        try {
            document.writeTo(new FileOutputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        document.close();
    }

    private void drawBitmap(Canvas canvas, Paint paint, Rect dst, BitmapFactory.Options options, String pathName) {
        WeakReference<Bitmap> wfb = new WeakReference<>(BitmapFactory.decodeFile(pathName, options));
        Bitmap bitmap = wfb.get();
        scaleAndDrawBitmap(canvas, paint, dst, bitmap, 0.3);
        bitmap.recycle();
        wfb.clear();
    }

    private void scaleAndDrawBitmap(Canvas canvas, Paint paint, Rect dst, Bitmap bitmap, double coe) {
        int bitmapW = (int) (bitmap.getWidth() * coe);
        int bitmapH = (int) (bitmap.getHeight() * coe);
        WeakReference<Bitmap> nwfb = new WeakReference<>(Bitmap.createScaledBitmap(bitmap, bitmapW, bitmapH, false));
        Bitmap newBM = nwfb.get();
        canvas.drawBitmap(newBM, null, dst, paint);
        newBM.recycle();
        nwfb.clear();
    }

    private void drawPdf(Canvas canvas) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Typeface typeface = Typeface.create("微软雅黑", Typeface.NORMAL);
        paint.setTypeface(typeface);
        paint.setAntiAlias(true); // 抗锯齿
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        //绘制home背景格式
        drawHomeBackground(canvas, paint, options);
        drawLogoAndroidTitle(canvas, paint, options);
        drawListText(canvas, paint);
        drawHomeText(canvas, paint);
        drawBadPic(canvas, paint, options);
        drawWatermark(canvas, paint, mItem.getFooter());
    }

    private void drawHomeBackground(Canvas canvas, Paint paint, BitmapFactory.Options options) {
        WeakReference<Bitmap> wfb = new WeakReference<>(BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.bg1, options));
        Bitmap bitmap = wfb.get();
        Log.i("cc", "bitmap.width = " + bitmap.getWidth() + ", bitmap.height = " + bitmap.getHeight());
        Rect dst = new Rect(20, 35, mScreenWidth - 20, (mScreenWidth - 70)  * bitmap.getHeight() / bitmap.getWidth() + 35);
        Log.i("cc", "mScreenWidth = " + mScreenWidth + ", bottom = " + (mScreenWidth - 160)  * bitmap.getHeight() / bitmap.getWidth());
        canvas.drawBitmap(bitmap, null, dst, paint);
        wfb.clear();
        bitmap.recycle();
    }

    private void drawLogoAndroidTitle(Canvas canvas, Paint paint, BitmapFactory.Options options) {
        int x = 30;
        int y = 42;
        int logoWidth = 0;
        boolean logoDrawed = false;
        if (mShowLogo && mItem.getLogo_Pic() != null) {
            WeakReference<Bitmap> wfb = new WeakReference<>(BitmapFactory.decodeFile(mItem.getLogo_Pic(), options));
            Bitmap bitmap = wfb.get();
            logoWidth = 35 * bitmap.getWidth() / bitmap.getHeight();
            Rect dst = new Rect(x, y, x + logoWidth, y + 35);
            scaleAndDrawBitmap(canvas, paint, dst, bitmap, 0.3);
            logoDrawed = true;
        }
        if (mShowTitle && mItem.getTitle() != null) {
            paint.setTextSize(25);
            paint.setFakeBoldText(true);
            canvas.drawText(mItem.getTitle(), x + (logoDrawed ? logoWidth : 0) + 25, y + 27, paint);
            paint.setTextSize(10);
            paint.setFakeBoldText(false);
        }
    }

    private void drawListText(Canvas canvas, Paint paint) {
        paint.setTextSize(17f);
        paint.setFakeBoldText(true);
        drawText("客户", 70, 102, canvas, paint);
        drawText("SN", 75, 131, canvas, paint);
        drawText("不良大类", 55, 160, canvas, paint);
        drawText("发生时间", 55, 188, canvas, paint);
        drawText("机种", 445, 102, canvas, paint);
        drawText("发生站点", 430, 131, canvas, paint);
        drawText("不良现象", 430, 160, canvas, paint);
        drawText("不良位置", 430, 188, canvas, paint);
        drawText("Phenomenal Description", 50, 216, canvas, paint);
        drawText("Failure Analysis", 50, 399, canvas, paint);
        drawText("Root cause", 50, 1082, canvas, paint);
        paint.setTextSize(15.5f);
        drawText("1." + mItem.getConfirm_1(), 58, 427, canvas, paint);
        drawText("2." + mItem.getConfirm_2(), 58, 637, canvas, paint);
        drawText("3." + mItem.getConfirm_3(), 58, 848, canvas, paint);
        paint.setTextSize(14.2f);
        paint.setFakeBoldText(false);
    }

    private void drawHomeText(Canvas canvas, Paint paint) {
        drawText(mItem.getCustomer(), 170, 101, canvas, paint);                   //客户
        drawText(mItem.getSN(), 170, 130, canvas, paint);                         //sn
        drawText(mItem.getBadPhenom(), 170, 159, canvas, paint);                  //不良大类
        drawText(mItem.getOccDate(), 170, 187, canvas, paint);                    //发生时间
        drawText(mItem.getMachineType(), 545, 101, canvas, paint);                //机种
        drawText(mItem.getOccSite(), 545, 130, canvas, paint);                    //发生站点
        drawText(mItem.getBadPhenom2(), 545, 159, canvas, paint);                 //不良现象
        drawText(mItem.getBadPosition(), 545, 187, canvas, paint);                //不良位置
        drawText("小结：" + mItem.getDisplayText(), 58, 610, canvas, paint);                //外观确认
        drawText("小结：" + mItem.getOMText(), 58, 820, canvas, paint);                     //OM检查
        drawText("小结：" + mItem.getSignalText(), 58, 1053, canvas, paint);                //讯号量测确认
        drawText(mItem.getConclusion(), 58, 1110, canvas, paint);                    //结论
        if (mShowFooter) {
            drawText(mItem.getFooter(), 600, 1160, canvas, paint);                   //页脚
        }
    }

    private void drawText(String str, int x, int y, Canvas canvas, Paint paint) {
        if (str != null) {
            canvas.drawText(str, x, y, paint);
        }
    }

    private void drawDisplayText(String text, Canvas canvas, Paint paint) {
        String[] str = text.split("\n");
        for (int i = 0; i < str.length; i++) {
            canvas.drawText(str[i], 105, 280 + i * 30, paint);
        }
    }

    private void drawBadPic(Canvas canvas, Paint paint, BitmapFactory.Options options) {
        int x = 120;
        int badPicWidth = 225;
        int badPicHeight = 145;
        //首页不良图片
        ArrayList homeBitmapList = (ArrayList) mItem.getHome_BadPic();
        drawBadPic(canvas, paint, options, homeBitmapList, x, 228, badPicWidth, badPicHeight);
        //外观不良图片
        ArrayList displayBitmapList = (ArrayList) mItem.getDisplay_BadPic();
        drawBadPic(canvas, paint, options, displayBitmapList, x, 440, badPicWidth, badPicHeight);
        //OM不良图片
        ArrayList OMBitmapList = (ArrayList) mItem.getOM_BadPic();
        drawBadPic(canvas, paint, options, OMBitmapList, x, 650, badPicWidth, badPicHeight);
        //讯号量测不良图片
        ArrayList signalBitmapList = (ArrayList) mItem.getSignal_BadPic();
        drawBadPic(canvas, paint, options, signalBitmapList, x, 870, badPicWidth, badPicHeight);
    }

    private void drawBadPic(Canvas canvas, Paint paint, BitmapFactory.Options options, List list, int x, int y, int picWidth, int picHeight) {
        if (list != null && list.size() != 0) {
            if (list.size() == 1) {
                Rect dst = new Rect(255, y, 255 + picWidth, y + picHeight);
                drawBitmap(canvas, paint, dst, options, (String)list.get(0));
            } else {
                for (int i = 0; i < list.size(); i++) {
                    Rect dst = new Rect(x + (picWidth + 30) * i, y, x + (picWidth + 30) * i + picWidth, y + picHeight);
                    drawBitmap(canvas, paint, dst, options, (String)list.get(i));
                }
            }
        }
    }

    private void drawWatermark(Canvas canvas, Paint paint, String text) {
        if (mShowWatermark && text != null) {
            paint.setARGB(0x20, 0, 0, 0);//设置水印颜色
            paint.setTextSize(40f);//设置水印字体大小
            canvas.rotate(-20);
            canvas.drawText(text, 50, 470, paint);
            canvas.drawText(text, 0, 940, paint);
            canvas.rotate(0);
            paint.setTextSize(14.2f);
        }
    }

    private String getTime() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH)+1;
        String monS = "" + month;
        if (month < 10) {
            monS = "0" + monS;
        }
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String dayS = "" + day;
        if (day < 10) {
            dayS = "0" + dayS;
        }
        return "" + year + monS + dayS;
    }

}
