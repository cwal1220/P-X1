package com.inspeco.data;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;

public class X1 {

    private CamBitmap camBitmap;
    public P1 p1;

    private static class LazyHolder { public static final X1 uniqueInstance = new X1();}
    public static X1 getInstance() {
        return LazyHolder.uniqueInstance;
    }
    private X1() {
        camBitmap = CamBitmap.getInstance();
    }

    public boolean isDeviceAttatched = false;

    private int width = 0;
    private int height = 0;
    public int ondoWidth = 0;
    public int ondoHeight = 0;

    public float max1 = 0.0f;
    public float min1 = 0.0f;

    public float ondoX = 0f;
    public float ondoY = 0f;

    public float center = 0.0f;
    public float[] ondoBuf = new float[640 * 512 + 10];

//
//    public void drawX1Data(Canvas bitcanvas, int aWidth, int aHeight, int camType) {
//
//        bitcanvas.drawColor(0, PorterDuff.Mode.CLEAR);
//
//        width = aWidth;
//        height = aHeight;
//        Paint paint = new Paint();
//        paint.setStyle( Paint.Style.FILL );
//        String str;
//
//        if ( camType== Consts.CAM_ONDO )  {
//            // 212  106
//            int xPos = (width - 212) / 2;
//            int yPos = 250;
//            bitcanvas.drawBitmap(camBitmap.mOndoTarget, null, new Rect(xPos,yPos, xPos+212, yPos+212), null);
//
//            int panelTop = height - 250;
//            paint.setStrokeWidth(5);
//            paint.setTextSize(90);
//            paint.setColor(Color.WHITE);
//            str = "TEMP";  // "°F";
//            bitcanvas.drawText(str, 590.0f, panelTop, paint);
//            bitcanvas.drawLine(364, panelTop+24, width-364, panelTop+24, paint );
//
//            int pannelLine2 = panelTop+115;
//            paint.setTextSize(80);
//            bitcanvas.drawText("TAR", 374, pannelLine2, paint);
//
//            bitcanvas.drawText("MAX", width-374-180, pannelLine2, paint);
//
//            int pannelLine3 = pannelLine2+90;
//            int cellWidth = 160;
//            paint.setTextSize(70);
//
//            str = String.format( "%.1f°C", center);
//            xPos =  (int)((cellWidth-paint.measureText(str))/2);
//            bitcanvas.drawText(str, 374+xPos, pannelLine3, paint);
//
//            str = String.format( "%.1f°C", max1);
//            xPos =  (int)((cellWidth-paint.measureText(str))/2);
//            bitcanvas.drawText(str, width-374-170+xPos, pannelLine3, paint);
//
//            str = String.format( "%.1f", min1);
//            bitcanvas.drawText(str, width-450, 200, paint);
//
//            str = String.format( "%.1f", max1);
//            bitcanvas.drawText(str, width-450, 300, paint);
//
//
//        }
//
//
//    }
//
//




}
