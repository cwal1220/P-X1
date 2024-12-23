package com.inspeco.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.util.Log;

import com.inspeco.X1.HomeView.P1Model;
import com.inspeco.X1.R;
import com.serenegiant.widget.TouchPoint;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class P1 {
    private String TAG = "bobopro-P1";

    // public static final int db_real = 0;
    // public static final int db_max = 0;
    // public static final int db_avg = 0;

    public P1Model p1Model = null;

    public int deviceWidth = 0;
    public int deviceHeight = 0;
    public boolean tempCamReady = false;

    public String db_str = "--.-";
    public String sen_str = "";
    public String vol_str = "";

    public String lati = "";
    public String longi = "";

    public Float fLati = 0f;
    public Float fLongi = 0f;

    public Float scrXcenter = 0f;
    public Float scrOndo = 0f;
    public Float scrHumi = 0f;
    public Float[] scrTouch = new Float[24];

    public boolean isDeviceAttatched = false;
    public int wifiLevel = 0;

    private int width = 0;
    private int height = 0;

    public int camMode = Consts.CAM_ONDO;
    public int camType = Consts.MODE_CAM_NOR;
    public boolean isMute = false;

    public boolean isRecording= false;

    public float imsiCheckOndo;
    public float imsiMixOppa;
    //public float imsiCheckMaxOndo;
    //public float imsiCheckMinOndo;

    public float ondo = 0f;
    public float humi = 0f;

    private CamBitmap camBitmap;

    private int calcCount = 0;

    public int mode = 0;
    public int vol = 0;
    public int sen = 0;

    public int db = 0;
    public float db_f = 0;
    public float avrDb;
    public float peakDb;
    public X1 x1;

    public long lastWaveTime = 0;
    public short[] waveSignal = new short[512];
    public short[] waveSignal2 = new short[512];
    public byte[] waveAudio = new byte[1024];
    public byte[] waveAudio2 = new byte[1024];
    public ArrayList<WaveBuf> inputWaveList;
    public ArrayList<WaveBufDisp> outputWaveList;

    public int alarmCnt; // 알람 카운트 체크용
    public boolean isPlayingAlarm = false;

    public boolean ondoSelectMode = false;
    public boolean arrangeMode = false;
    public float scaleFactor = 1.0f;
    public float vRatio = 1.0f;
    public float hRatio = 1.0f;
    public float xOrg = 0.0f;
    public float yOrg = 0.0f;
    public CopyOnWriteArrayList<TouchPoint> touchPointList;


    public class WaveBuf {
        long waveTime = 0;
        short[] wave = new short[512];
    }

    public class WaveBufDisp {
        long waveTime = 0;
        short[] wave = new short[25];
    }


    //System.arraycopy(packet, pos, wave, 0, 1024);
    private static class LazyHolder {
        public static final P1 uniqueInstance = new P1();
    }

    public static P1 getInstance() {
        return P1.LazyHolder.uniqueInstance;
    }

    private P1() {
        camBitmap = CamBitmap.getInstance();
    }




    public void playShutter(Context aContext ) {
        MediaPlayer mediaPlayer = MediaPlayer.create(aContext, R.raw.shutter);
        mediaPlayer.setLooping(false);
        mediaPlayer.start();
    }


        /**
         * 모드별 그리기 메인
         */
    public void drawP1Data(Canvas bitcanvas, int aWidth, int aHeight) {

        if (deviceWidth==1200) {
            width = aWidth+20;
            height = aHeight;

            // 중앙 960, 355
        } else {
            width = aWidth - 140;
            height = aHeight;
        }

        if (camMode == Consts.CAM_ONDO) {
            drawOndo(bitcanvas);
        } else if (camMode == Consts.CAM_MIX) {
            drawMix(bitcanvas);
        } else if (camMode == Consts.CAM_WEBCAM) {
            drawWebCam(bitcanvas);
        }

    }





    public void makeWaveform(int x1, int y1, int width, int height, short[] buffer, float[] waveformPoints) {
        float lastX = -1;
        float lastY = -1;
        int pointIndex = 0;
        int centerY = height / 2;
        int bufferLen = buffer.length;
        //float max = Short.MAX_VALUE;
        float max = Short.MAX_VALUE;
        db_f = (float) db / 100f;
        if (db_f < 1.0) {
            db_f = 1.0f;
        }
        float curDbv = db_f / 41.0f;
        float maxV = 0f;
        //
        for (int x = 0; x < bufferLen; x++) {
            float sampleV = buffer[x] / max;
            if (sampleV > maxV) {
                maxV = sampleV;
            }
        }

        float calcR = 1.0f;
        if (curDbv > 0) {
            if (maxV > curDbv) {
                calcR = curDbv / maxV;
            }
        }

        //Log.d("bobopro", "waveform "+Integer.toString(width) + ", "+Integer.toString(bufferLen));
        for (int x = 0; x < width; x++) {
            int index = (int) (((float) x / width) * bufferLen);
            float samplev = buffer[index] / max;
            //short sample = buffer[index];

            float y = centerY - ((samplev) * calcR * centerY);

            if (lastX != -1) {
                waveformPoints[pointIndex++] = x1 + lastX;
                waveformPoints[pointIndex++] = y1 + lastY;
                waveformPoints[pointIndex++] = x1 + x;
                waveformPoints[pointIndex++] = y1 + y;
            }
            lastX = x;
            lastY = y;
        }

    }

    public void drawP1IconInfo(Canvas bitcanvas, Paint paint) {
        String str;

        paint.setStrokeWidth(1);
        paint.setTextSize(46);
        paint.setColor(Color.WHITE);
        int xPos = 1920 - 270;

        scrOndo = ondo;
        scrHumi = humi;
        bitcanvas.drawBitmap(camBitmap.mIconGps, null, new Rect(xPos, 80, xPos + 36, 80 + 52), null);
        str = "OK";
        bitcanvas.drawText(str, xPos + 60, 80 + 40, paint);

        bitcanvas.drawBitmap(camBitmap.mIconOndo, null, new Rect(xPos, 160, xPos + 36, 160 + 52), null);
        str = String.format("%.1f°", ondo) + Cfg.p1_cGiho;;
        bitcanvas.drawText(str, xPos + 60, 160 + 40, paint);

        bitcanvas.drawBitmap(camBitmap.mIconHumi, null, new Rect(xPos, 240, xPos + 36, 240 + 52), null);
        str = String.format("%.1f", humi) + "%";
        bitcanvas.drawText(str, xPos + 60, 240 + 40, paint);
    }


    public void drawP1Wave(Canvas bitcanvas, Paint paint, Integer xPos, Integer yPos) {
        int waveCellWidth = 940;
        int waveCellHeight = 220;
        float[] waveformPoints = new float[waveCellWidth * 4];
        paint.setStrokeWidth(2);
        paint.setColor(Color.CYAN);
        makeWaveform(xPos, yPos, waveCellWidth, waveCellHeight, waveSignal, waveformPoints);
        bitcanvas.drawLines(waveformPoints, paint);
    }





    /**
     * 열화상 모드 그리기
     */
    public void drawOndo(Canvas bitcanvas) {

        width = 1920;
        height = 1080;
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        String str;

        //bitcanvas.drawColor(Color.BLACK);
        bitcanvas.drawColor(0, PorterDuff.Mode.CLEAR);


        paint.setColor(Color.BLACK);
        bitcanvas.drawRect(0, height-180, width, height, paint);

        if (!tempCamReady) return;




        try {
            float[] hsv_value = new float[3];
            hsv_value[1] = 1.0f;
            hsv_value[2] = 1.0f;
            float minOndo = x1.min1;
            float maxOndo = x1.max1;
            int minOndoX = 0, minOndoY = 0;
            int maxOndoX = 0, maxOndoY = 0;
            float minVal = 99, maxVal = -99;


            float pointGapX = - 98;
            float pointGapY = - 98;
            // 갤텝이 아닌 핸드폰이면 왼쪽으로
            if (deviceWidth==1200) {
                pointGapX = -90;
                pointGapY = -90;
            }


            float ondoDiff = (maxOndo - minOndo);
            float[] mOndoBuf = new float[256 * 194];

            System.arraycopy(x1.ondoBuf, 0, mOndoBuf, 0, 256 * 192);

            //중앙점 온도 계산
            float pointx = 905f;
            float pointy = 500f;

            int ondoViewWidth = (int) ((float) width * 1f);
            int ondoViewHeight = (int) ((float) height * 1f);

            //Log.d("bobopro", "point "+Float.toString(pointx) + ", "+Float.toString(pointy));

            float indexX = (pointx);
            indexX = (indexX / ondoViewWidth)*255;

            float indexY = (pointy);
            indexY = (indexY / ondoViewHeight)*192;

            int ondoIndex = (int)indexX+((int)indexY*256);
//
            float pointMax = -20.0f;
            for(int i=0; i<20; i++){
                int y = i*256;
                for( int j=0; j<16; j++) {
                    if (pointMax<mOndoBuf[ondoIndex+y+j]) { pointMax = mOndoBuf[ondoIndex+y+j]; }
                }
            }
            x1.center = pointMax+Cfg.ondo_offSet;
            scrXcenter = x1.center;
            // 중앙 테스트용으로 칠하기
//        for(int i=0; i<20; i++){
//            int y = i*256;
//            for( int j=0; j<16; j++) { mOndoBuf[ondoIndex+y+j] = 80; }
//        }

            isPlayingAlarm = false;
            int touchIdx = 0;
            // 터치포인트 온도 계산
            if (touchPointList !=null) {
                for (TouchPoint touchPoint : touchPointList) {
                    //photoPaint.setTextSize(20);

                    pointx = touchPoint.x * 2000 + pointGapX + 23;
                    pointy = touchPoint.y * 1080 + pointGapY + 23;

                    //Log.d("bobopro", "point "+Float.toString(pointx) + ", "+Float.toString(pointy));

                    indexX = (pointx);
                    indexX = (indexX / ondoViewWidth)*255;
                    if (indexX>256-11) { indexX = 255-11; }
                    indexX -= 1;
                    if ( indexX<0 ) {indexX = 0;}

                    indexY = (pointy);
                    indexY = (indexY / ondoViewHeight)*192;
                    if ( indexY<0 ) {indexY = 0;}
                    if (indexY>192-11) { indexY = 192-11; }
                    ondoIndex = (int)indexX+((int)indexY*256);

                    pointMax = -20f;
                    for(int i=0; i<8; i++){
                        int y = i*256;
                        if (pointMax<mOndoBuf[ondoIndex+y]) { pointMax = mOndoBuf[ondoIndex+y]; }
                        if (pointMax<mOndoBuf[ondoIndex+y+1]) { pointMax = mOndoBuf[ondoIndex+y+1]; }
                        if (pointMax<mOndoBuf[ondoIndex+y+2]) { pointMax = mOndoBuf[ondoIndex+y+2]; }
                        if (pointMax<mOndoBuf[ondoIndex+y+3]) { pointMax = mOndoBuf[ondoIndex+y+3]; }
                        if (pointMax<mOndoBuf[ondoIndex+y+4]) { pointMax = mOndoBuf[ondoIndex+y+4]; }
                        if (pointMax<mOndoBuf[ondoIndex+y+5]) { pointMax = mOndoBuf[ondoIndex+y+5]; }
                        if (pointMax<mOndoBuf[ondoIndex+y+6]) { pointMax = mOndoBuf[ondoIndex+y+6]; }
                        if (pointMax<mOndoBuf[ondoIndex+y+7]) { pointMax = mOndoBuf[ondoIndex+y+7]; }
                        // if (pointMax<mOndoBuf[ondoIndex+y+8]) { pointMax = mOndoBuf[ondoIndex+y+8]; }
                    }
                    // 최고점 온도
                    //pointMax = pointMax;
                    // 중앙점 온도
//                pointMax += mOndoBuf[ondoIndex+3 * 3*256];
//                pointMax += mOndoBuf[ondoIndex+4 * 3*256];
//                pointMax += mOndoBuf[ondoIndex+3 * 4*256];
//                pointMax += mOndoBuf[ondoIndex+4 * 4*256];
//                // 중심 온도 2
//                pointMax += mOndoBuf[ondoIndex+2 * 2*256];
//                pointMax += mOndoBuf[ondoIndex+5 * 2*256];
//                pointMax += mOndoBuf[ondoIndex+2 * 5*256];
//                pointMax += mOndoBuf[ondoIndex+5 * 5*256];

//                touchPoint.ondo = pointMax / 16;
                    touchPoint.ondo = pointMax+Cfg.ondo_offSet;
                    scrTouch[touchIdx] = touchPoint.ondo;
                    touchIdx++;

                    if (Cfg.p1_alram_set == true) {
                        if (Cfg.p1_alram_icon) {
                            if (touchPoint.ondo > Cfg.p1_alram_ondo) {
                                isPlayingAlarm = true;
                            }
                        }
                    }

//                mOndoBuf[ondoIndex] = maxOndo; mOndoBuf[ondoIndex+1] = maxOndo;
//                mOndoBuf[ondoIndex+2] = maxOndo; mOndoBuf[ondoIndex+3] = maxOndo;
//                mOndoBuf[ondoIndex+4] = maxOndo; mOndoBuf[ondoIndex+5] = maxOndo;
//                mOndoBuf[ondoIndex+6] = maxOndo; mOndoBuf[ondoIndex+7] = maxOndo;
//                mOndoBuf[ondoIndex+8] = maxOndo;
                }
            }







//        Bitmap backbit = Bitmap.createBitmap(256, 192, Bitmap.Config.ARGB_8888);
//        Canvas offscreen = new Canvas(backbit);
//        offscreen.drawColor(0, PorterDuff.Mode.CLEAR);
//        //offscreen.drawColor(Color.BLUE);
            Paint pnt = new Paint();
            pnt.setAntiAlias(false);

            // 터치 포인트 그리기...
            paint.setColor(Color.WHITE);
            paint.setTextSize(50);
            char aChar = 'A';
            if (touchPointList != null) {
                for (TouchPoint touchPoint : touchPointList) {
                    pointx = touchPoint.x * 2000 + pointGapX;
                    pointy = touchPoint.y * 1080 + pointGapY;
                    bitcanvas.drawBitmap(camBitmap.mOndoPointer, pointx, pointy, null);
                    pointx -= 40f;
                    pointy -= 20f;
                    str = String.format("%c%s", aChar, Cfg.getOndoFCNorGiho(touchPoint.ondo, "°"+Cfg.p1_cGiho)) ;
                    bitcanvas.drawText(str, pointx, pointy, paint);
                    aChar++;
                }
            }

            // 전체 영역에서 최고 및 최저 온도 계산
            for (int y = 0; y < 192; y++) {
                for (int x = 0; x < 256; x++) {
                    float value = mOndoBuf[y * 256 + x];
                    if (value < minVal) {
                        minOndoX = x;
                        minOndoY = y;
                        minVal = value;
                    }
                    if (value > maxVal) {
                        maxOndoX = x;
                        maxOndoY = y;
                        maxVal = value;
                    }
                }
            }

            minOndoX = (minOndoX * 1920) / 256;
            minOndoY = (minOndoY * 1080) / 192;
            maxOndoX = (maxOndoX * 1920) / 256;
            maxOndoY = (maxOndoY * 1080) / 192;

            str = String.format("MIN:%s%s" ,(int)Cfg.getOndoFC(minOndo), "°"+Cfg.p1_cGiho) ;
            bitcanvas.drawText(str, minOndoX + 10, minOndoY, paint);
            bitcanvas.drawRect(minOndoX, minOndoY, minOndoX + 10, minOndoY + 10, paint);

            str = String.format("Max:%s%s" ,(int)Cfg.getOndoFC(maxOndo), "°"+Cfg.p1_cGiho) ;
            bitcanvas.drawText(str, maxOndoX + 10, maxOndoY, paint);
            bitcanvas.drawRect(maxOndoX, maxOndoY, maxOndoX + 10, maxOndoY + 10, paint);

            Log.d("chan dra min", "min:" + minOndoX + "," + minOndoY);
            Log.d("chan dra max", "max:" + maxOndoX + "," + maxOndoY);


            /////////////////////////////////////////

            int xPos = (width - 100) / 2;
            int yPos = 480;

            pnt.setAntiAlias(true);
            pnt.setFilterBitmap(true);
            if (camType == Consts.MODE_CAM_NOR) {
                // 온도캠 일반모드
                // 타켓 필드 그리기 - 온도용
                bitcanvas.drawBitmap(camBitmap.mOndoTarget, null, new Rect(xPos, yPos, xPos + 100, yPos + 100), pnt);

                int panelTop = height - 106;
                int lineStart =   320;
                int lineEnd = lineStart+1000;
                paint.setStrokeWidth(5);
                paint.setColor(Color.WHITE);
                paint.setTextSize(75);

                bitcanvas.drawText("MIN", lineStart + 70, panelTop, paint);
                bitcanvas.drawText("TAR", lineStart + 424, panelTop, paint);
                bitcanvas.drawText("MAX", lineStart + 772, panelTop, paint);

                int pannelLine2 = panelTop+25;
                bitcanvas.drawLine(lineStart, pannelLine2, lineEnd, pannelLine2, paint);

                int pannelLine3 = panelTop + 85;

                int cellWidth = 280;
                paint.setTextSize(65);

                float av = (x1.min1 - minOndo) / ondoDiff;
                if (av < 0) av = 0;
                av = av * 255;
                av = 255 - av;
                hsv_value[0] = av;
                paint.setColor(Color.HSVToColor(255, hsv_value));

                str = Cfg.getOndoFCNorGiho(x1.min1, "°"+Cfg.p1_cGiho);
                xPos = (int) ((cellWidth - paint.measureText(str)) / 2);
                bitcanvas.drawText(str, lineStart + xPos, pannelLine3, paint);

                av = (x1.center - minOndo) / ondoDiff;
                if (av < 0) av = 0;
                av = av * 255;
                av = 255 - av;
                hsv_value[0] = av;
                paint.setColor(Color.HSVToColor(255, hsv_value));


                str = Cfg.getOndoFCNorGiho(x1.center, "°"+Cfg.p1_cGiho);
                xPos = (int) ((cellWidth - paint.measureText(str)) / 2);
                bitcanvas.drawText(str, lineStart +360 + xPos, pannelLine3, paint);

                av = (x1.max1 - minOndo) / ondoDiff;
                if (av < 0) av = 0;
                av = av * 255;
                av = 255 - av;
                hsv_value[0] = av;
                paint.setColor(Color.HSVToColor(255, hsv_value));

                str = Cfg.getOndoFCNorGiho(x1.max1, "°"+Cfg.p1_cGiho);
                xPos = (int) ((cellWidth - paint.measureText(str)) / 2);
                bitcanvas.drawText(str, lineEnd - cellWidth + xPos, pannelLine3, paint);

                drawP1IconInfo(bitcanvas, paint);

                // 온도 그래프 (일반)
                xPos = (width-160);
                yPos = 340;
                bitcanvas.drawBitmap(camBitmap.mSpectrum, null, new Rect(xPos, yPos, xPos + 30, yPos + 450), pnt);

                cellWidth=50;
                paint.setTextSize(30);
                paint.setStrokeWidth(1);

                str = Cfg.getOndoFCNor0(x1.max1);
                xPos = (int)((cellWidth - paint.measureText(str)) / 2);
                bitcanvas.drawText(str, width- 120 + xPos, yPos+35, paint);

                str = Cfg.getOndoFCNor0((x1.min1 + x1.max1)/2f) ;
                xPos = (int) ((cellWidth - paint.measureText(str)) / 2);
                bitcanvas.drawText(str, width- 120 + xPos, yPos+222, paint);

                str = Cfg.getOndoFCNor0(x1.min1);
                //Log.i(TAG, str);
                xPos = (int) ((cellWidth - paint.measureText(str)) / 2);
                bitcanvas.drawText(str, width- 120 + xPos, yPos+434, paint);

                paint.setStrokeWidth(3);
                bitcanvas.drawLine(width- 120 + 25, yPos+35+25, width- 120 + 25, yPos + 185, paint);
                bitcanvas.drawLine(width- 120 + 25, yPos+222+25, width- 120 + 25, yPos + 380, paint);

            } else {
                // 온도캠 확장모드
                // 온도 그래프 (확장)
                xPos = (width-100);
                yPos = 80;
                bitcanvas.drawBitmap(camBitmap.mSpectrum, null, new Rect(xPos, yPos, xPos + 30, yPos + 750), pnt);

                int cellWidth=60;
                paint.setTextSize(32);
                paint.setStrokeWidth(1);

                str = Cfg.getOndoFCNor0(x1.max1);
                xPos = (int)((cellWidth - paint.measureText(str)) / 2);
                bitcanvas.drawText(str, width- 170 + xPos, yPos+35, paint);

                str = Cfg.getOndoFCNor0((x1.min1 + x1.max1)/2);
                xPos = (int) ((cellWidth - paint.measureText(str)) / 2);
                bitcanvas.drawText(str, width- 170 + xPos, yPos+350, paint);

                str = Cfg.getOndoFCNor0(x1.min1);
                xPos = (int) ((cellWidth - paint.measureText(str)) / 2);
                bitcanvas.drawText(str, width- 170 + xPos, yPos+734, paint);

                paint.setStrokeWidth(3);
                bitcanvas.drawLine(width- 125 - 16, yPos+35+25, width- 125 - 16, yPos + 305, paint);
                bitcanvas.drawLine(width- 125 - 16, yPos+350+25, width- 125 - 16, yPos + 690, paint);
            }
        } catch ( Exception e){
            Log.d("bobopro drawOne", "except "+e.getLocalizedMessage());

            int panelTop = height - 106;
            int lineStart =  50;
            paint.setStrokeWidth(3);
            paint.setColor(Color.WHITE);
            paint.setTextSize(35);

            bitcanvas.drawText(e.getLocalizedMessage(), lineStart , panelTop, paint);
        }



    }






// ▷℃℃℉℉
    /**
     * 실화상 + 온도,  복합 모드 그리기
     */
    public void drawMix(Canvas bitcanvas) {

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        String str;
        int minOndoX = 0, minOndoY = 0;
        int maxOndoX = 0, maxOndoY = 0;
        float minVal = 99, maxVal = -99;

        bitcanvas.drawColor(0, PorterDuff.Mode.CLEAR);

        if (!tempCamReady) {
            paint.setColor(Color.BLACK);
            bitcanvas.drawRect(0, height-190, width, height, paint);
            return;
        }

        try {
            float[] hsv_value = new float[3];
            hsv_value[1] = 1.0f;
            hsv_value[2] = 1.0f;
            float minOndo = Cfg.cam3_min120Ondo;
            if (Cfg.ondo_extMode) {
                minOndo = Cfg.cam3_min600Ondo;
            }
            //float showOndo = Cfg.cam3_checkOndo;
            float checkMinOndo = Cfg.cam3_checkOndo;

            if (ondoSelectMode) {
                checkMinOndo = imsiCheckOndo;
            }
            float maxOndo = Cfg.cam3_max120Ondo;
            //paint.setStyle( Paint.Style.FILL );

            if (Cfg.ondo_extMode) {
                maxOndo = Cfg.cam3_max600Ondo;
                if (Cfg.cam3_max600Auto) {
                    maxOndo = x1.max1;
                }
                if (Cfg.cam3_min600Auto) {
                    minOndo = x1.min1;
                }
            } else {
                if (Cfg.cam3_max120Auto) {
                    maxOndo = x1.max1;
                }
                if (Cfg.cam3_min120Auto) {
                    minOndo = x1.min1;
                }
            }


            if (Float.isNaN(minOndo)) {
                minOndo = 0;
            }

            float ondoDiff = (maxOndo - minOndo);
            float[] mOndoBuf = new float[256 * 194];

            System.arraycopy(x1.ondoBuf, 0, mOndoBuf, 0, 256 * 192);

            int ondoViewWidth = (int) ((float) 1280 * hRatio);
            int ondoViewHeight = (int) ((float) 1024 * vRatio);

            float pointGapX = -98f;
            float pointGapY = -98f;


            // 갤텝
            if (deviceWidth==1200) {
                pointGapX = -90f;
                pointGapY = -90f;
            }

            //중앙점 온도 계산

            float pointx = 905f;
            float pointy = 520f;

            //Log.d("bobopro", "point "+Float.toString(pointx) + ", "+Float.toString(pointy));

            float indexX = (pointx - xOrg);
            indexX = (indexX / ondoViewWidth)*255;

            float indexY = (pointy - yOrg);
            indexY = (indexY / ondoViewHeight)*192;

            int ondoIndex = (int)indexX+((int)indexY*256);
//
            float pointMax = -20.0f;
            for(int i=0; i<15; i++){
                int y = i*256;
                for( int j=0; j<15; j++) {
                    if (pointMax<mOndoBuf[ondoIndex+y+j]) { pointMax = mOndoBuf[ondoIndex+y+j]; }
                }
            }
            x1.center = pointMax+Cfg.ondo_offSet;
            scrXcenter = x1.center;
            // 중앙 테스트용으로 칠하기
            //        for(int i=0; i<15; i++){
            //            int y = i*256;
            //            for( int j=0; j<15; j++) {
            //                mOndoBuf[ondoIndex+y+j] = x1.center;
            //            }
            //        }
            int touchIdx = 0;
            if (touchPointList !=null) {
                for (TouchPoint touchPoint : touchPointList) {
                    //photoPaint.setTextSize(20);

                    pointx = touchPoint.x * 2000 + pointGapX + 23;
                    pointy = touchPoint.y * 1080 + pointGapY + 23;

                    //Log.d("bobopro", "point "+Float.toString(pointx) + ", "+Float.toString(pointy));

                    indexX = (pointx - xOrg);
                    indexX = (indexX / ondoViewWidth)*255;
                    if (indexX>ondoViewWidth-11) { indexX = 255-11; }
                    indexX -= 1;
                    if ( indexX<0 ) {indexX = 0;}

                    indexY = (pointy - yOrg);
                    indexY = (indexY / ondoViewHeight)*192;
                    if ( indexY<0 ) {indexY = 0;}
                    if (indexY>ondoViewHeight-11) { indexY = 192-11; }
                    ondoIndex = (int)indexX+((int)indexY*256);

                    isPlayingAlarm = false;
                    pointMax = -20.0f;
                    for(int i=0; i<8; i++){
                        int y = i*256;
                        if (pointMax<mOndoBuf[ondoIndex+y]) { pointMax = mOndoBuf[ondoIndex+y]; }
                        if (pointMax<mOndoBuf[ondoIndex+y+1]) { pointMax = mOndoBuf[ondoIndex+y+1]; }
                        if (pointMax<mOndoBuf[ondoIndex+y+2]) { pointMax = mOndoBuf[ondoIndex+y+2]; }
                        if (pointMax<mOndoBuf[ondoIndex+y+3]) { pointMax = mOndoBuf[ondoIndex+y+3]; }
                        if (pointMax<mOndoBuf[ondoIndex+y+4]) { pointMax = mOndoBuf[ondoIndex+y+4]; }
                        if (pointMax<mOndoBuf[ondoIndex+y+5]) { pointMax = mOndoBuf[ondoIndex+y+5]; }
                        if (pointMax<mOndoBuf[ondoIndex+y+6]) { pointMax = mOndoBuf[ondoIndex+y+6]; }
                        if (pointMax<mOndoBuf[ondoIndex+y+7]) { pointMax = mOndoBuf[ondoIndex+y+7]; }
                        // if (pointMax<mOndoBuf[ondoIndex+y+8]) { pointMax = mOndoBuf[ondoIndex+y+8]; }
                    }
                    // 최고점 온도
                    //pointMax = pointMax;
                    // 중앙점 온도
//                pointMax += mOndoBuf[ondoIndex+3 * 3*256];
//                pointMax += mOndoBuf[ondoIndex+4 * 3*256];
//                pointMax += mOndoBuf[ondoIndex+3 * 4*256];
//                pointMax += mOndoBuf[ondoIndex+4 * 4*256];
//                // 중심 온도 2
//                pointMax += mOndoBuf[ondoIndex+2 * 2*256];
//                pointMax += mOndoBuf[ondoIndex+5 * 2*256];
//                pointMax += mOndoBuf[ondoIndex+2 * 5*256];
//                pointMax += mOndoBuf[ondoIndex+5 * 5*256];

//                touchPoint.ondo = pointMax / 16;
                    touchPoint.ondo = pointMax + Cfg.ondo_offSet;
                    scrTouch[touchIdx] = touchPoint.ondo;
                    touchIdx++;
                    if (Cfg.p1_alram_set == true) {
                        if (Cfg.p1_alram_icon) {
                            if (touchPoint.ondo > Cfg.p1_alram_ondo) {
                                isPlayingAlarm = true;
                            }
                        }
                    }

//                mOndoBuf[ondoIndex] = maxOndo; mOndoBuf[ondoIndex+1] = maxOndo;
//                mOndoBuf[ondoIndex+2] = maxOndo; mOndoBuf[ondoIndex+3] = maxOndo;
//                mOndoBuf[ondoIndex+4] = maxOndo; mOndoBuf[ondoIndex+5] = maxOndo;
//                mOndoBuf[ondoIndex+6] = maxOndo; mOndoBuf[ondoIndex+7] = maxOndo;
//                mOndoBuf[ondoIndex+8] = maxOndo;
                }
            }


            Bitmap backbit = Bitmap.createBitmap(256, 192, Bitmap.Config.ARGB_8888);
            Canvas offscreen = new Canvas(backbit);
            offscreen.drawColor(0, PorterDuff.Mode.CLEAR);
            //offscreen.drawColor(Color.BLUE);
            Paint pnt = new Paint();
            pnt.setAntiAlias(false);

            int alpha =  (int)(Cfg.cam3_mixOppa * 2.55);
            if (ondoSelectMode) {
                alpha = (int)(imsiMixOppa * 2.55);;
            }

            int xr = (int) xOrg;
            int yr = (int) yOrg;

            float yPer = (ondoViewHeight / 256f);
            //Log.d("bobopro", "yPer "+Float.toString(yPer));

            for (int i = 0; i < ((256) * 192); i++) {
                float x, y;
                //if ((mOndoBuf[i] >= checkMinOndo) && (mOndoBuf[i] <= checkMaxOndo)) {
                if (mOndoBuf[i] >= checkMinOndo) {
                    if (!Float.isNaN(mOndoBuf[i])) {
                        x = ((float) i % 256);
                        y = ((float) i / 256);

                        float cky =  ( yPer * y ) + yr;
                        if (cky<650f) {
                            if ( (x > 0)  && (y > 0) ) {
                                float av = (mOndoBuf[i] - minOndo) / ondoDiff;
                                if (av < 0) av = 0;
                                if (Cfg.cam3_colorMode == Consts.PaletteRainbow) {
                                    av = av * 240;
                                    av = 240 - av;
                                    hsv_value[0] = av;
                                    pnt.setColor(Color.HSVToColor(alpha, hsv_value));
                                } else if (Cfg.cam3_colorMode == Consts.PaletteAmber) {
                                    av = av * 80;
                                    av = 80 - av;
                                    hsv_value[0] = av;
                                    pnt.setColor(Color.HSVToColor(alpha, hsv_value));
                                } else {
                                    av = av * 255;
                                    pnt.setColor(Color.argb(alpha, av, av, av));
                                }

                                offscreen.drawPoint(x, y, pnt);
                            }


                        } // endif chky

                    }
                }
            }

//

            if ((arrangeMode) || (ondoSelectMode) ) {
                pnt.setFilterBitmap(false);
                pnt.setDither(true);
            } else {
                pnt.setFilterBitmap(true);
                pnt.setDither(false);
            }

            bitcanvas.drawBitmap(backbit, null, new Rect(xr, yr, (xr + ondoViewWidth), (yr + ondoViewHeight)), pnt);

            paint.setColor(Color.BLACK);
            bitcanvas.drawRect(0, height-190, width, height, paint);


            if ((arrangeMode==false) && (ondoSelectMode==false) ) {

                paint.setColor(Color.WHITE);
                paint.setTextSize(50);
                char aChar = 'A';
                //Log.d("bobopro", "aaa "+Float.toString(pointGapX));
                if (touchPointList != null) {
                    for (TouchPoint touchPoint : touchPointList) {
                        pointx = (touchPoint.x * 2100) + pointGapX;
                        pointy = (touchPoint.y * 1080) + pointGapY;
                        bitcanvas.drawBitmap(camBitmap.mOndoPointer, pointx, pointy, null);
                        pointx -= 40f;
                        pointy -= 20f;
                        str = String.format("%c%s", aChar, Cfg.getOndoFCNorGiho(touchPoint.ondo, "°"+Cfg.p1_cGiho)) ;
                        bitcanvas.drawText(str, pointx, pointy, paint);
                        aChar++;
                    }
                }


                // 전체 영역에서 최고 및 최저 온도 계산
                for (int y = 0; y < 192; y++) {
                    for (int x = 0; x < 256; x++) {
                        float value = mOndoBuf[y * 256 + x];
                        if (value < minVal) {
                            minOndoX = x;
                            minOndoY = y;
                            minVal = value;
                        }
                        if (value > maxVal) {
                            maxOndoX = x;
                            maxOndoY = y;
                            maxVal = value;
                        }
                    }
                }

                minOndoX = (minOndoX * 1920) / 256;
                minOndoY = (minOndoY * 1080) / 192;
                maxOndoX = (maxOndoX * 1920) / 256;
                maxOndoY = (maxOndoY * 1080) / 192;

                str = String.format("MIN:%s%s" ,(int)Cfg.getOndoFC(minOndo), "°"+Cfg.p1_cGiho) ;
                bitcanvas.drawText(str, minOndoX + 10, minOndoY, paint);
                bitcanvas.drawRect(minOndoX, minOndoY, minOndoX + 10, minOndoY + 10, paint);

                str = String.format("Max:%s%s" ,(int)Cfg.getOndoFC(maxOndo), "°"+Cfg.p1_cGiho) ;
                bitcanvas.drawText(str, maxOndoX + 10, maxOndoY, paint);
                bitcanvas.drawRect(maxOndoX, maxOndoY, maxOndoX + 10, maxOndoY + 10, paint);

                Log.d("chan dra min", "min:" + minOndoX + "," + minOndoY);
                Log.d("chan dra max", "max:" + maxOndoX + "," + maxOndoY);

                int xPos = (1920 - 100) / 2;
                int yPos = 480;


                if (camType == Consts.MODE_CAM_NOR) {
                    // 타켓 필드 그리기 - 온도용
                    pnt.setAntiAlias(true);
                    bitcanvas.drawBitmap(camBitmap.mMixTarget, null, new Rect(xPos, yPos, xPos + 100, yPos + 100), pnt);

                    paint.setAntiAlias(true);
                    paint.setFilterBitmap(true);
                    paint.setAlpha(180);

                    int panelTop = height - 122;
                    int lineStart =   840;
                    int lineEnd = lineStart+580;
                    paint.setStrokeWidth(5);
                    paint.setColor(Color.WHITE);
                    paint.setTextSize(60);

                    bitcanvas.drawText("TAR", lineStart + 60, panelTop-5, paint);
                    bitcanvas.drawText("MAX", lineStart + 378, panelTop-5, paint);

                    int pannelLine2 = panelTop+15;
                    bitcanvas.drawLine(lineStart, pannelLine2, lineEnd, pannelLine2, paint);

                    int pannelLine3 = panelTop + 80;

                    int cellWidth = 280;
                    paint.setTextSize(70);

                    float av = (x1.center - minOndo) / ondoDiff;
                    if (av < 0) av = 0;
                    av = av * 255;
                    av = 255 - av;
                    hsv_value[0] = av;
                    paint.setColor(Color.HSVToColor(255, hsv_value));

                    str = Cfg.getOndoFCNorGiho(x1.center, "°"+Cfg.p1_cGiho);
                    xPos = (int) ((cellWidth - paint.measureText(str)) / 2);
                    bitcanvas.drawText(str, lineStart + xPos-5, pannelLine3, paint);

                    av = (x1.max1 - minOndo) / ondoDiff;
                    if (av < 0) av = 0;
                    av = av * 255;
                    av = 255 - av;
                    hsv_value[0] = av;
                    paint.setColor(Color.HSVToColor(255, hsv_value));

                    str = Cfg.getOndoFCNorGiho(x1.max1, "°"+Cfg.p1_cGiho);
                    xPos = (int) ((cellWidth - paint.measureText(str)) / 2);
                    bitcanvas.drawText(str, lineStart +320 + xPos, pannelLine3, paint);


                    pannelLine3 = panelTop + 45;
                    //paint.setStrokeWidth(6*scaleFactor);
                    //paint.setStyle(Paint.Style.FILL_AND_STROKE);

                    paint.setStrokeWidth(5);
                    paint.setTextSize(65);
                    paint.setColor(Color.WHITE);

                    str = "REALTIME";
                    bitcanvas.drawText(str, 60.0f, pannelLine3, paint);

                    //db=3922;

                    //센터 메터 ( 0~ 20까지 )
                    xPos = (1920 - 256) / 2;
                    yPos = 480-22; // 256-212
                    int idx = db / 200;

                    if (idx<0) idx = 0;
                    if (idx>20) idx = 20;

                    if ((arrangeMode==false) && (ondoSelectMode==false) ) {
                        if (idx>0) {
                            Bitmap aBit = camBitmap.lvCenter.get(idx);
                            bitcanvas.drawBitmap(aBit, null, new Rect(xPos-128, yPos-128, xPos +256+128, yPos+256+128), paint);
                        }
                    }

                    idx = db / 100;
                    if (idx<0) idx = 0;
                    if (idx>30) idx = 30;

                    // 우측 메터
                    if (idx>0) {
                        paint.setAlpha(255);
                        idx = db / 100;
                        if (idx<0) idx = 0;
                        if (idx>30) idx = 30;
                        if ((arrangeMode==false) && (ondoSelectMode==false) ) {
                            Bitmap aBit = camBitmap.lvMeter.get(idx);
                            xPos = 1920 - 230;
                            bitcanvas.drawBitmap(aBit, null, new Rect(xPos, 236, xPos + 48, 236 + 511), paint);
                            paint.setStyle(Paint.Style.FILL);
                        }
                    }

                    switch (idx) {
                        case 1:  paint.setColor(Color.rgb(230, 234, 0 ));  break;
                        case 2:  paint.setColor(Color.rgb(230, 227, 0 ));  break;
                        case 3:  paint.setColor(Color.rgb(230, 221, 0 ));  break;
                        case 4:  paint.setColor(Color.rgb(229, 215, 0 ));  break;
                        case 5:  paint.setColor(Color.rgb(229, 207, 0 ));  break;
                        case 6:  paint.setColor(Color.rgb(227, 200, 0 ));  break;
                        case 7:  paint.setColor(Color.rgb(226, 190, 0 ));  break;
                        case 8:  paint.setColor(Color.rgb(225, 183, 0 ));  break;
                        case 9:  paint.setColor(Color.rgb(224, 176, 16 ));  break;
                        case 10:  paint.setColor(Color.rgb(223, 166, 20 ));  break;
                        case 11:  paint.setColor(Color.rgb(222, 158, 22 ));  break;
                        case 12:  paint.setColor(Color.rgb(221, 148, 23 ));  break;
                        case 13:  paint.setColor(Color.rgb(221, 141, 25 ));  break;
                        case 14:  paint.setColor(Color.rgb(220, 133, 27 ));  break;
                        case 15:  paint.setColor(Color.rgb(218, 123, 27 ));  break;
                        case 16:  paint.setColor(Color.rgb(218, 114, 27 ));  break;
                        case 17:  paint.setColor(Color.rgb(215, 108, 29 ));  break;
                        case 18:  paint.setColor(Color.rgb(213, 103, 29 ));  break;
                        case 19:  paint.setColor(Color.rgb(210, 99, 31 ));  break;
                        case 20:  paint.setColor(Color.rgb(208, 95, 32 ));  break;
                        case 21:  paint.setColor(Color.rgb(206, 90, 33 ));  break;
                        case 22:  paint.setColor(Color.rgb(204, 85, 34 ));  break;
                        case 23:  paint.setColor(Color.rgb(202, 80, 34 ));  break;
                        case 24:  paint.setColor(Color.rgb(200, 76, 34 ));  break;
                        case 25:  paint.setColor(Color.rgb(200, 73, 35 ));  break;
                        case 26:  paint.setColor(Color.rgb(200, 70, 35 ));  break;
                        case 27:  paint.setColor(Color.rgb(200, 65, 36 ));  break;
                        case 28:  paint.setColor(Color.rgb(200, 56, 36 ));  break;
                        case 29:  paint.setColor(Color.rgb(200, 50, 37 ));  break;
                        case 30:  paint.setColor(Color.rgb(200, 45, 37 ));  break;
                        default: paint.setColor(Color.rgb(53, 53, 53 ));  break;
                    }


                    //db_str = "35.3Db";

                    //paint.setStrokeWidth(6*scaleFactor);
                    //paint.setStyle(Paint.Style.FILL_AND_STROKE);

                    cellWidth=350;
                    paint.setStrokeWidth(7);
                    paint.setTextSize(115);
                    if (idx != 0) {
                        str = db_str+"dB";
                    } else {
                        str = db_str;
                    }
                    xPos = (int) ((cellWidth - paint.measureText(str)) / 2);
                    bitcanvas.drawText(str, 410+xPos, pannelLine3+15, paint);




                    if ((arrangeMode==false) && (ondoSelectMode==false) ) {
                        paint.setColor(Color.WHITE);
                        drawP1IconInfo(bitcanvas, paint);
                        // 온도 그래프
                        xPos = (1920-165);
                        yPos = 300;
                        bitcanvas.drawBitmap(camBitmap.mSpectrum, null, new Rect(xPos+4, yPos, xPos+4 + 30, yPos + 450), pnt);

                        cellWidth=50;
                        paint.setTextSize(30);
                        paint.setStrokeWidth(1);
                        str = Cfg.getOndoFCNor0(maxOndo);
                        xPos = (int)((cellWidth - paint.measureText(str)) / 2);
                        bitcanvas.drawText(str, 1920- 120 + xPos, yPos+35, paint);

                        str = Cfg.getOndoFCNor0((minOndo + maxOndo)/2f);
                        xPos = (int) ((cellWidth - paint.measureText(str)) / 2);
                        bitcanvas.drawText(str, 1920- 120 + xPos, yPos+222, paint);

                        str = Cfg.getOndoFCNor0(minOndo);
                        xPos = (int) ((cellWidth - paint.measureText(str)) / 2);
                        bitcanvas.drawText(str, 1920- 120 + xPos, yPos+434, paint);

                        paint.setStrokeWidth(3);
                        bitcanvas.drawLine(1920- 120 + 25, yPos+35+25, 1920- 120 + 25, yPos + 185, paint);
                        bitcanvas.drawLine(1920- 120 + 25, yPos+222+25, 1920- 120 + 25, yPos + 380, paint);

                    }

//                if (!arrangeMode) {
//                    drawP1Wave(bitcanvas, paint, 620, pannelLine3 - 140);
//                }

                } else {
                    // 확장모드

                    // 온도 그래프
                    xPos = (width-100);
                    yPos = 100;
                    bitcanvas.drawBitmap(camBitmap.mSpectrum, null, new Rect(xPos-5, yPos, xPos-5 + 30, yPos + 750), pnt);

                    int cellWidth=60;
                    paint.setTextSize(32);
                    paint.setStrokeWidth(1);

                    str = Cfg.getOndoFCNor0(maxOndo);
                    xPos = (int)((cellWidth - paint.measureText(str)) / 2);
                    bitcanvas.drawText(str, 1920- 170 + xPos, yPos+35, paint);


                    str = Cfg.getOndoFCNor0((minOndo + maxOndo)/2);
                    xPos = (int) ((cellWidth - paint.measureText(str)) / 2);
                    bitcanvas.drawText(str, 1920- 170 + xPos, yPos+350, paint);

                    str = Cfg.getOndoFCNor0(minOndo);
                    xPos = (int) ((cellWidth - paint.measureText(str)) / 2);
                    bitcanvas.drawText(str, 1920- 170 + xPos, yPos+734, paint);

                    paint.setStrokeWidth(3);
                    bitcanvas.drawLine(1920- 125 - 16, yPos+35+25, 1920- 125 - 16, yPos + 305, paint);
                    bitcanvas.drawLine(1920- 125 - 16, yPos+350+25, 1920- 125 - 16, yPos + 690, paint);

                }

            }

        } catch ( Exception e){
            Log.d("bobopro drawOne", "except "+e.getLocalizedMessage());

            int panelTop = height - 106;
            int lineStart =  50;
            paint.setStrokeWidth(3);
            paint.setColor(Color.WHITE);
            paint.setTextSize(35);

            bitcanvas.drawText(e.getLocalizedMessage(), lineStart , panelTop, paint);
        }



    } // 복합 모드 그리기  end







    // 실화상 모드
    @SuppressLint("DefaultLocale")
    public void drawWebCam(Canvas bitcanvas) {

        Paint paint = new Paint();
        String str;
        bitcanvas.drawColor(0, PorterDuff.Mode.CLEAR);

        paint.setColor(Color.BLACK);
        bitcanvas.drawRect(0, height-190, width, height, paint);


        if (camType == Consts.MODE_CAM_NOR) {
            // 일반모드
            bitcanvas.drawBitmap(camBitmap.mTargetField, null, new Rect(450, 240, 1920 - 450, 1080 - 230), null);

            paint.setAntiAlias(true);
            paint.setFilterBitmap(true);
            paint.setAlpha(180);

            int panelTop = height - 122;

            int pannelLine3 = panelTop + 45;
            //paint.setStrokeWidth(6*scaleFactor);
            //paint.setStyle(Paint.Style.FILL_AND_STROKE);

            paint.setStrokeWidth(5);
            paint.setTextSize(65);
            paint.setColor(Color.WHITE);

            str = "REALTIME";
            bitcanvas.drawText(str, 60.0f, pannelLine3, paint);


            //센터 메터 ( 0~ 20까지 )
            int xPos = (1920 - 256) / 2;
            int yPos = 400;
            int idx = db / 200;

            if (idx<0) idx = 0;
            if (idx>20) idx = 20;

            if (idx>0) {
                Bitmap aBit = camBitmap.lvCenter.get(idx);
                bitcanvas.drawBitmap(aBit, null, new Rect(xPos-64, yPos-64, xPos +256+128, yPos+256+128), paint);
            }

            // 우측 메터
            if (idx>0) {
                paint.setAlpha(255);
                idx = db / 100;
                if (idx<0) idx = 0;
                if (idx>30) idx = 30;
                Bitmap aBit = camBitmap.lvMeter.get(idx);
                xPos =  1920 - 230;
                bitcanvas.drawBitmap(aBit, null, new Rect(xPos, 286, xPos+48, 286+511), paint);
                paint.setStyle(Paint.Style.FILL);
            }
            idx = db / 100;
            if (idx<0) idx = 0;
            if (idx>30) idx = 30;
            switch (idx) {
                case 1:  paint.setColor(Color.rgb(230, 234, 0 ));  break;
                case 2:  paint.setColor(Color.rgb(230, 227, 0 ));  break;
                case 3:  paint.setColor(Color.rgb(230, 221, 0 ));  break;
                case 4:  paint.setColor(Color.rgb(229, 215, 0 ));  break;
                case 5:  paint.setColor(Color.rgb(229, 207, 0 ));  break;
                case 6:  paint.setColor(Color.rgb(227, 200, 0 ));  break;
                case 7:  paint.setColor(Color.rgb(226, 190, 0 ));  break;
                case 8:  paint.setColor(Color.rgb(225, 183, 0 ));  break;
                case 9:  paint.setColor(Color.rgb(224, 176, 16 ));  break;
                case 10:  paint.setColor(Color.rgb(223, 166, 20 ));  break;
                case 11:  paint.setColor(Color.rgb(222, 158, 22 ));  break;
                case 12:  paint.setColor(Color.rgb(221, 148, 23 ));  break;
                case 13:  paint.setColor(Color.rgb(221, 141, 25 ));  break;
                case 14:  paint.setColor(Color.rgb(220, 133, 27 ));  break;
                case 15:  paint.setColor(Color.rgb(218, 123, 27 ));  break;
                case 16:  paint.setColor(Color.rgb(218, 114, 27 ));  break;
                case 17:  paint.setColor(Color.rgb(215, 108, 29 ));  break;
                case 18:  paint.setColor(Color.rgb(213, 103, 29 ));  break;
                case 19:  paint.setColor(Color.rgb(210, 99, 31 ));  break;
                case 20:  paint.setColor(Color.rgb(208, 95, 32 ));  break;
                case 21:  paint.setColor(Color.rgb(206, 90, 33 ));  break;
                case 22:  paint.setColor(Color.rgb(204, 85, 34 ));  break;
                case 23:  paint.setColor(Color.rgb(202, 80, 34 ));  break;
                case 24:  paint.setColor(Color.rgb(200, 76, 34 ));  break;
                case 25:  paint.setColor(Color.rgb(200, 73, 35 ));  break;
                case 26:  paint.setColor(Color.rgb(200, 70, 35 ));  break;
                case 27:  paint.setColor(Color.rgb(200, 65, 36 ));  break;
                case 28:  paint.setColor(Color.rgb(200, 56, 36 ));  break;
                case 29:  paint.setColor(Color.rgb(200, 50, 37 ));  break;
                case 30:  paint.setColor(Color.rgb(200, 45, 37 ));  break;
                default: paint.setColor(Color.rgb(53, 53, 53 ));  break;
            }


            //db_str = "35.3dB";

            //paint.setStyle(Paint.Style.FILL_AND_STROKE);

            int cellWidth=350;
            paint.setStrokeWidth(7);
            paint.setTextSize(115);
            if (idx != 0) {
                str = db_str+"dB";
            } else {
                str = db_str;
            }

            xPos = (int) ((cellWidth - paint.measureText(str)) / 2);
            bitcanvas.drawText(str, 415+xPos, pannelLine3+15, paint);

            drawP1Wave(bitcanvas, paint, 880, panelTop - 88);

            paint.setStrokeWidth(7);
            paint.setColor(Color.WHITE);
            drawP1IconInfo(bitcanvas, paint);



        } else {
            // 확장모드
            // 타켓 필드 그리기
            //bitcanvas.drawBitmap(camBitmap.mTargetField, null, new Rect(450, 90, 1920 - 450, 1080 - 380), null);

//            int panelTop = 1080 - 280;
//            paint.setStrokeWidth(5);
//            paint.setTextSize(90);
//            paint.setColor(Color.WHITE);
//            str = "REALTIME";  // "°F";
//            bitcanvas.drawText(str, 760.0f, panelTop, paint);
//            bitcanvas.drawLine(64, panelTop + 24, 1920 - 64, panelTop + 24, paint);
//
//            int pannelLine2 = panelTop + 115;
//            paint.setTextSize(80);
//            bitcanvas.drawText("VOL", 64, pannelLine2, paint);
//            bitcanvas.drawText("AVG", 64 + 274, pannelLine2, paint);
//
//            bitcanvas.drawLine(64, pannelLine2 + 30, 64 + 430, pannelLine2 + 30, paint);
//
//            bitcanvas.drawText("MAX", 1920 - 64 - 430, pannelLine2, paint);
//            bitcanvas.drawText("SEN", 1920 - 64 - 160, pannelLine2, paint);
//            bitcanvas.drawLine(1920 - 64 - 430, pannelLine2 + 30, 1920 - 64, pannelLine2 + 30, paint);
//
//            int pannelLine3 = pannelLine2 + 115;
//
//            paint.setTextSize(90);
//            str = db_str;
//            int xPos = (int) ((400 - paint.measureText(str)) / 2);
//            bitcanvas.drawText(str, 960 - 200 + xPos, pannelLine2, paint);
//
//            int cellWidth = 150;
//            paint.setTextSize(80);
//
//            str = String.format("%d", vol) + "%";
//            xPos = (int) ((cellWidth - paint.measureText(str)) / 2);
//            bitcanvas.drawText(str, 64 + xPos, pannelLine3, paint);
//
//            str = String.format("%.1f", (float) avrDb / 100f);
//            xPos = (int) ((cellWidth - paint.measureText(str)) / 2);
//            bitcanvas.drawText(str, 64 + xPos + cellWidth + 130, pannelLine3, paint);
//
//            str = String.format("%.1f", (float) peakDb / 100f);
//            xPos = (int) ((cellWidth - paint.measureText(str)) / 2);
//            bitcanvas.drawText(str, 1920 - 64 - 430 + xPos, pannelLine3, paint);
//
//            str = String.format("%d", sen);
//            xPos = (int) ((cellWidth - paint.measureText(str)) / 2);
//            bitcanvas.drawText(str, 1920 - 64 - 160 + xPos, pannelLine3, paint);

            bitcanvas.drawBitmap(camBitmap.mTargetField, null, new Rect(450, 240, 1920 - 450, 1080 - 230), null);

            paint.setAntiAlias(true);
            paint.setFilterBitmap(true);
            paint.setAlpha(180);

            int panelTop = height - 122;

            int pannelLine3 = panelTop + 45;
            //paint.setStrokeWidth(6*scaleFactor);
            //paint.setStyle(Paint.Style.FILL_AND_STROKE);

            paint.setStrokeWidth(5);
            paint.setTextSize(65);
            paint.setColor(Color.WHITE);

            str = "REALTIME";
            bitcanvas.drawText(str, 60.0f, pannelLine3, paint);


            //센터 메터 ( 0~ 20까지 )
            int idx = db / 200;

            idx = db / 100;
            if (idx<0) idx = 0;
            if (idx>30) idx = 30;
            switch (idx) {
                case 1:  paint.setColor(Color.rgb(230, 234, 0 ));  break;
                case 2:  paint.setColor(Color.rgb(230, 227, 0 ));  break;
                case 3:  paint.setColor(Color.rgb(230, 221, 0 ));  break;
                case 4:  paint.setColor(Color.rgb(229, 215, 0 ));  break;
                case 5:  paint.setColor(Color.rgb(229, 207, 0 ));  break;
                case 6:  paint.setColor(Color.rgb(227, 200, 0 ));  break;
                case 7:  paint.setColor(Color.rgb(226, 190, 0 ));  break;
                case 8:  paint.setColor(Color.rgb(225, 183, 0 ));  break;
                case 9:  paint.setColor(Color.rgb(224, 176, 16 ));  break;
                case 10:  paint.setColor(Color.rgb(223, 166, 20 ));  break;
                case 11:  paint.setColor(Color.rgb(222, 158, 22 ));  break;
                case 12:  paint.setColor(Color.rgb(221, 148, 23 ));  break;
                case 13:  paint.setColor(Color.rgb(221, 141, 25 ));  break;
                case 14:  paint.setColor(Color.rgb(220, 133, 27 ));  break;
                case 15:  paint.setColor(Color.rgb(218, 123, 27 ));  break;
                case 16:  paint.setColor(Color.rgb(218, 114, 27 ));  break;
                case 17:  paint.setColor(Color.rgb(215, 108, 29 ));  break;
                case 18:  paint.setColor(Color.rgb(213, 103, 29 ));  break;
                case 19:  paint.setColor(Color.rgb(210, 99, 31 ));  break;
                case 20:  paint.setColor(Color.rgb(208, 95, 32 ));  break;
                case 21:  paint.setColor(Color.rgb(206, 90, 33 ));  break;
                case 22:  paint.setColor(Color.rgb(204, 85, 34 ));  break;
                case 23:  paint.setColor(Color.rgb(202, 80, 34 ));  break;
                case 24:  paint.setColor(Color.rgb(200, 76, 34 ));  break;
                case 25:  paint.setColor(Color.rgb(200, 73, 35 ));  break;
                case 26:  paint.setColor(Color.rgb(200, 70, 35 ));  break;
                case 27:  paint.setColor(Color.rgb(200, 65, 36 ));  break;
                case 28:  paint.setColor(Color.rgb(200, 56, 36 ));  break;
                case 29:  paint.setColor(Color.rgb(200, 50, 37 ));  break;
                case 30:  paint.setColor(Color.rgb(200, 45, 37 ));  break;
                default: paint.setColor(Color.rgb(53, 53, 53 ));  break;
            }


            //db_str = "35.3dB";

            //paint.setStyle(Paint.Style.FILL_AND_STROKE);

            int cellWidth=350;
            paint.setStrokeWidth(7);
            paint.setTextSize(115);
            if (idx != 0) {
                str = db_str+"dB";
            } else {
                str = db_str;
            }
            int xPos = (int) ((cellWidth - paint.measureText(str)) / 2);
            bitcanvas.drawText(str, 410+xPos, pannelLine3+15, paint);


            drawP1Wave(bitcanvas, paint, 880, panelTop - 88);

        }

    }



}