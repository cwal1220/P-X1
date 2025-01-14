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

    // CHAN 열화상 설정
    final int CAMERA_WIDTH = 256;
    final int CAMERA_HEIGHT = 192;
//    final int CAMERA_WIDTH = 640;
//    final int CAMERA_HEIGHT = 512;

    final int CENTER_TARGET_SIZE = 100;

    final int SCREEN_WIDTH = 1920;
    final int SCREEN_HEIGHT = 1080;

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

    int[][] g_plasma_colormap = new int[][] {
            {13, 8, 135},
            {16, 7, 136},
            {19, 7, 137},
            {22, 7, 138},
            {25, 6, 140},
            {27, 6, 141},
            {29, 6, 142},
            {32, 6, 143},
            {34, 6, 144},
            {36, 6, 145},
            {38, 5, 145},
            {40, 5, 146},
            {42, 5, 147},
            {44, 5, 148},
            {46, 5, 149},
            {47, 5, 150},
            {49, 5, 151},
            {51, 5, 151},
            {53, 4, 152},
            {55, 4, 153},
            {56, 4, 154},
            {58, 4, 154},
            {60, 4, 155},
            {62, 4, 156},
            {63, 4, 156},
            {65, 4, 157},
            {67, 3, 158},
            {68, 3, 158},
            {70, 3, 159},
            {72, 3, 159},
            {73, 3, 160},
            {75, 3, 161},
            {76, 2, 161},
            {78, 2, 162},
            {80, 2, 162},
            {81, 2, 163},
            {83, 2, 163},
            {85, 2, 164},
            {86, 1, 164},
            {88, 1, 164},
            {89, 1, 165},
            {91, 1, 165},
            {92, 1, 166},
            {94, 1, 166},
            {96, 1, 166},
            {97, 0, 167},
            {99, 0, 167},
            {100, 0, 167},
            {102, 0, 167},
            {103, 0, 168},
            {105, 0, 168},
            {106, 0, 168},
            {108, 0, 168},
            {110, 0, 168},
            {111, 0, 168},
            {113, 0, 168},
            {114, 1, 168},
            {116, 1, 168},
            {117, 1, 168},
            {119, 1, 168},
            {120, 1, 168},
            {122, 2, 168},
            {123, 2, 168},
            {125, 3, 168},
            {126, 3, 168},
            {128, 4, 168},
            {129, 4, 167},
            {131, 5, 167},
            {132, 5, 167},
            {134, 6, 166},
            {135, 7, 166},
            {136, 8, 166},
            {138, 9, 165},
            {139, 10, 165},
            {141, 11, 165},
            {142, 12, 164},
            {143, 13, 164},
            {145, 14, 163},
            {146, 15, 163},
            {148, 16, 162},
            {149, 17, 161},
            {150, 19, 161},
            {152, 20, 160},
            {153, 21, 159},
            {154, 22, 159},
            {156, 23, 158},
            {157, 24, 157},
            {158, 25, 157},
            {160, 26, 156},
            {161, 27, 155},
            {162, 29, 154},
            {163, 30, 154},
            {165, 31, 153},
            {166, 32, 152},
            {167, 33, 151},
            {168, 34, 150},
            {170, 35, 149},
            {171, 36, 148},
            {172, 38, 148},
            {173, 39, 147},
            {174, 40, 146},
            {176, 41, 145},
            {177, 42, 144},
            {178, 43, 143},
            {179, 44, 142},
            {180, 46, 141},
            {181, 47, 140},
            {182, 48, 139},
            {183, 49, 138},
            {184, 50, 137},
            {186, 51, 136},
            {187, 52, 136},
            {188, 53, 135},
            {189, 55, 134},
            {190, 56, 133},
            {191, 57, 132},
            {192, 58, 131},
            {193, 59, 130},
            {194, 60, 129},
            {195, 61, 128},
            {196, 62, 127},
            {197, 64, 126},
            {198, 65, 125},
            {199, 66, 124},
            {200, 67, 123},
            {201, 68, 122},
            {202, 69, 122},
            {203, 70, 121},
            {204, 71, 120},
            {204, 73, 119},
            {205, 74, 118},
            {206, 75, 117},
            {207, 76, 116},
            {208, 77, 115},
            {209, 78, 114},
            {210, 79, 113},
            {211, 81, 113},
            {212, 82, 112},
            {213, 83, 111},
            {213, 84, 110},
            {214, 85, 109},
            {215, 86, 108},
            {216, 87, 107},
            {217, 88, 106},
            {218, 90, 106},
            {218, 91, 105},
            {219, 92, 104},
            {220, 93, 103},
            {221, 94, 102},
            {222, 95, 101},
            {222, 97, 100},
            {223, 98, 99},
            {224, 99, 99},
            {225, 100, 98},
            {226, 101, 97},
            {226, 102, 96},
            {227, 104, 95},
            {228, 105, 94},
            {229, 106, 93},
            {229, 107, 93},
            {230, 108, 92},
            {231, 110, 91},
            {231, 111, 90},
            {232, 112, 89},
            {233, 113, 88},
            {233, 114, 87},
            {234, 116, 87},
            {235, 117, 86},
            {235, 118, 85},
            {236, 119, 84},
            {237, 121, 83},
            {237, 122, 82},
            {238, 123, 81},
            {239, 124, 81},
            {239, 126, 80},
            {240, 127, 79},
            {240, 128, 78},
            {241, 129, 77},
            {241, 131, 76},
            {242, 132, 75},
            {243, 133, 75},
            {243, 135, 74},
            {244, 136, 73},
            {244, 137, 72},
            {245, 139, 71},
            {245, 140, 70},
            {246, 141, 69},
            {246, 143, 68},
            {247, 144, 68},
            {247, 145, 67},
            {247, 147, 66},
            {248, 148, 65},
            {248, 149, 64},
            {249, 151, 63},
            {249, 152, 62},
            {249, 154, 62},
            {250, 155, 61},
            {250, 156, 60},
            {250, 158, 59},
            {251, 159, 58},
            {251, 161, 57},
            {251, 162, 56},
            {252, 163, 56},
            {252, 165, 55},
            {252, 166, 54},
            {252, 168, 53},
            {252, 169, 52},
            {253, 171, 51},
            {253, 172, 51},
            {253, 174, 50},
            {253, 175, 49},
            {253, 177, 48},
            {253, 178, 47},
            {253, 180, 47},
            {253, 181, 46},
            {254, 183, 45},
            {254, 184, 44},
            {254, 186, 44},
            {254, 187, 43},
            {254, 189, 42},
            {254, 190, 42},
            {254, 192, 41},
            {253, 194, 41},
            {253, 195, 40},
            {253, 197, 39},
            {253, 198, 39},
            {253, 200, 39},
            {253, 202, 38},
            {253, 203, 38},
            {252, 205, 37},
            {252, 206, 37},
            {252, 208, 37},
            {252, 210, 37},
            {251, 211, 36},
            {251, 213, 36},
            {251, 215, 36},
            {250, 216, 36},
            {250, 218, 36},
            {249, 220, 36},
            {249, 221, 37},
            {248, 223, 37},
            {248, 225, 37},
            {247, 226, 37},
            {247, 228, 37},
            {246, 230, 38},
            {246, 232, 38},
            {245, 233, 38},
            {245, 235, 39},
            {244, 237, 39},
            {243, 238, 39},
            {243, 240, 39},
            {242, 242, 39},
            {241, 244, 38},
            {241, 245, 37},
            {240, 247, 36},
            {240, 249, 33},
    };

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


    // Plasma 컬러 맵으로 온도 데이터를 Bitmap으로 변환
    private Bitmap createThermalBitmap(float[] data, float min, float max) {

        Bitmap bitmap = Bitmap.createBitmap(CAMERA_WIDTH, CAMERA_HEIGHT, Bitmap.Config.ARGB_8888);

//        // 데이터의 최소값과 최대값을 계산
//        // TODO: 최대/최소를 찾기 않고, 입력받아서 처리하도록 변경
//        float min = Float.MAX_VALUE;
//        float max = Float.MIN_VALUE;
//        for (int y=0; y<192; y++) {
//            for (int x=0; x<256; x++) {
//                if (data[(y*256 + x)] < min) min = data[(y*256 + x)];
//                if (data[(y*256 + x)] > max) max = data[(y*256 + x)];
//            }
//        }
        
        // 데이터를 픽셀로 변환
        for (int y = 0; y < CAMERA_HEIGHT; y++) {
            for (int x = 0; x < CAMERA_WIDTH; x++) {
                float normalized = (data[(y*CAMERA_WIDTH + x)] - min) / (max - min);
                int color = plasmaColorMap(normalized);
                bitmap.setPixel(x, y, color);
            }
        }

        return bitmap;
    }

    // plasma color map value return
    private int plasmaColorMap(float value) {
        value = Math.max(0, Math.min(1, value)); // value clamp

        // Plasma 컬러 맵 RGB 값 정의 (256 단계)
        int index = Math.min((int)(value * 255), 255);
        int[] rgb = g_plasma_colormap[index];

        return Color.rgb(rgb[0], rgb[1], rgb[2]);
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
        bitcanvas.drawRect(0, SCREEN_HEIGHT-180, SCREEN_WIDTH, SCREEN_HEIGHT, paint);

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
            float[] mOndoBuf = new float[CAMERA_WIDTH * CAMERA_HEIGHT];

            System.arraycopy(x1.ondoBuf, 0, mOndoBuf, 0, CAMERA_WIDTH * CAMERA_HEIGHT);

            //중앙점 온도 계산
            float pointx = (1920-CENTER_TARGET_SIZE) / 2;//905f;
            float pointy = ((1080-CENTER_TARGET_SIZE) / 2) + 56;//500f;

            int ondoViewWidth = SCREEN_WIDTH;
            int ondoViewHeight = SCREEN_HEIGHT;

            //Log.d("bobopro", "point "+Float.toString(pointx) + ", "+Float.toString(pointy));

            float indexX = (pointx);
            indexX = (indexX / ondoViewWidth)*(CAMERA_WIDTH-1);

            float indexY = (pointy);
            indexY = (indexY / ondoViewHeight)*CAMERA_HEIGHT;

            int ondoIndex = (int)indexX+((int)indexY*CAMERA_WIDTH);
            float pointMax = -20.0f;
            float pointMin = 999.0f;
            for(int i=0; i<(((float)CENTER_TARGET_SIZE/(float)SCREEN_HEIGHT)*(float)CAMERA_HEIGHT); i++){
                int y = i*CAMERA_WIDTH;
                for( int j=0; j<(((float)CENTER_TARGET_SIZE/(float)SCREEN_WIDTH)*(float)CAMERA_WIDTH); j++) {
                    if (pointMax<mOndoBuf[ondoIndex+y+j]) { pointMax = mOndoBuf[ondoIndex+y+j]; }
                }
            }
            x1.center = pointMax+Cfg.ondo_offSet;
            scrXcenter = x1.center;
            isPlayingAlarm = false;

            // 전체 영역에서 최고 및 최저 온도 계산
            int heightGap = (int)((180.0/SCREEN_HEIGHT) * CAMERA_HEIGHT);
            for (int y = 0; y < CAMERA_HEIGHT - heightGap; y++) {
                for (int x = 0; x < CAMERA_WIDTH; x++) {
                    float value = mOndoBuf[y * CAMERA_WIDTH + x];
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

            int touchIdx = 0;
            // 터치포인트 온도 계산
            if (touchPointList !=null) {
                for (TouchPoint touchPoint : touchPointList) {
                    //photoPaint.setTextSize(20);

                    pointx = touchPoint.x * 2000 + pointGapX + 23;
                    pointy = touchPoint.y * 1080 + pointGapY + 23;

                    //Log.d("bobopro", "point "+Float.toString(pointx) + ", "+Float.toString(pointy));

                    indexX = (pointx);
                    indexX = (indexX / ondoViewWidth)*(CAMERA_WIDTH-1);
                    if (indexX>CAMERA_WIDTH-11) { indexX = (CAMERA_WIDTH-1)-11; }
                    indexX -= 1;
                    if ( indexX<0 ) {indexX = 0;}

                    indexY = (pointy);
                    indexY = (indexY / ondoViewHeight)*CAMERA_HEIGHT;
                    if ( indexY<0 ) {indexY = 0;}
                    if (indexY>CAMERA_HEIGHT-11) { indexY = CAMERA_HEIGHT-11; }
                    ondoIndex = (int)indexX+((int)indexY*CAMERA_WIDTH);

                    pointMax = -20f;
                    pointMin = 999f;
                    for(int i=0; i<8; i++){
                        int y = i*CAMERA_WIDTH;
                        // 선택된 범위의 최저 온도 계산
                        if (pointMin>mOndoBuf[ondoIndex+y]) { pointMin = mOndoBuf[ondoIndex+y]; }
                        if (pointMin>mOndoBuf[ondoIndex+y+1]) { pointMin = mOndoBuf[ondoIndex+y+1]; }
                        if (pointMin>mOndoBuf[ondoIndex+y+2]) { pointMin = mOndoBuf[ondoIndex+y+2]; }
                        if (pointMin>mOndoBuf[ondoIndex+y+3]) { pointMin = mOndoBuf[ondoIndex+y+3]; }
                        if (pointMin>mOndoBuf[ondoIndex+y+4]) { pointMin = mOndoBuf[ondoIndex+y+4]; }
                        if (pointMin>mOndoBuf[ondoIndex+y+5]) { pointMin = mOndoBuf[ondoIndex+y+5]; }
                        if (pointMin>mOndoBuf[ondoIndex+y+6]) { pointMin = mOndoBuf[ondoIndex+y+6]; }
                        if (pointMin>mOndoBuf[ondoIndex+y+7]) { pointMin = mOndoBuf[ondoIndex+y+7]; }
                        // 선택된 범위의 최고 온도 게산
                        if (pointMax<mOndoBuf[ondoIndex+y]) { pointMax = mOndoBuf[ondoIndex+y]; }
                        if (pointMax<mOndoBuf[ondoIndex+y+1]) { pointMax = mOndoBuf[ondoIndex+y+1]; }
                        if (pointMax<mOndoBuf[ondoIndex+y+2]) { pointMax = mOndoBuf[ondoIndex+y+2]; }
                        if (pointMax<mOndoBuf[ondoIndex+y+3]) { pointMax = mOndoBuf[ondoIndex+y+3]; }
                        if (pointMax<mOndoBuf[ondoIndex+y+4]) { pointMax = mOndoBuf[ondoIndex+y+4]; }
                        if (pointMax<mOndoBuf[ondoIndex+y+5]) { pointMax = mOndoBuf[ondoIndex+y+5]; }
                        if (pointMax<mOndoBuf[ondoIndex+y+6]) { pointMax = mOndoBuf[ondoIndex+y+6]; }
                        if (pointMax<mOndoBuf[ondoIndex+y+7]) { pointMax = mOndoBuf[ondoIndex+y+7]; }
                    }
                    if(touchIdx == 0) {
                        minVal = pointMin;
                        maxVal = pointMax;
                    }

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
                }
            }

            if(Cfg.ondo_spanMode) {
                Bitmap thermalBitmap = createThermalBitmap(mOndoBuf, minVal, maxVal);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(thermalBitmap, SCREEN_WIDTH, SCREEN_HEIGHT, true);
                bitcanvas.drawBitmap(scaledBitmap, 0, 0, null);
                bitcanvas.drawRect(0, SCREEN_HEIGHT-180, SCREEN_WIDTH, SCREEN_HEIGHT, paint);
            }

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

            minOndoX = (minOndoX * SCREEN_WIDTH) / CAMERA_WIDTH;
            minOndoY = (minOndoY * SCREEN_HEIGHT) / CAMERA_HEIGHT;
            maxOndoX = (maxOndoX * SCREEN_WIDTH) / CAMERA_WIDTH;
            maxOndoY = (maxOndoY * SCREEN_HEIGHT) / CAMERA_HEIGHT;

            int xMinOffset = 0;
            int yMinOffset = 0;
            int xMaxOffset = 0;
            int yMaxOffset = 0;

            if(minOndoX > SCREEN_WIDTH - 200) {
                xMinOffset = -200;
            }

            if(minOndoY < 50) {
                yMinOffset = 50;
            }

            if(maxOndoX > SCREEN_WIDTH - 200) {
                xMaxOffset = -200;
            }

            if(maxOndoY < 50) {
                yMaxOffset = 50;
            }

            str = String.format("MIN:%s%s" ,(int)Cfg.getOndoFC(minOndo), "°"+Cfg.p1_cGiho) ;
            bitcanvas.drawText(str, minOndoX + 10 + xMinOffset, minOndoY + yMinOffset, paint);
            bitcanvas.drawRect(minOndoX, minOndoY, minOndoX + 10, minOndoY + 10, paint);

            str = String.format("Max:%s%s" ,(int)Cfg.getOndoFC(maxOndo), "°"+Cfg.p1_cGiho) ;
            bitcanvas.drawText(str, maxOndoX + 10 + xMaxOffset, maxOndoY + yMaxOffset, paint);
            bitcanvas.drawRect(maxOndoX, maxOndoY, maxOndoX + 10, maxOndoY + 10, paint);
            /////////////////////////////////////////

            int xPos = (SCREEN_WIDTH - CENTER_TARGET_SIZE) / 2;
            int yPos = ( (SCREEN_HEIGHT - CENTER_TARGET_SIZE) / 2) + 56;

            pnt.setAntiAlias(true);
            pnt.setFilterBitmap(true);
            if (camType == Consts.MODE_CAM_NOR) {
                // 온도캠 일반모드
                // 타켓 필드 그리기 - 온도용
                bitcanvas.drawBitmap(camBitmap.mOndoTarget, null, new Rect(xPos, yPos, xPos + CENTER_TARGET_SIZE, yPos + CENTER_TARGET_SIZE), pnt);

                int panelTop = SCREEN_HEIGHT - 106;
                int lineStart = 320;
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
                bitcanvas.drawText(str, lineStart + 360 + xPos, pannelLine3, paint);

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
                xPos = (SCREEN_WIDTH-160);
                yPos = 340;
                bitcanvas.drawBitmap(camBitmap.mSpectrum, null, new Rect(xPos, yPos, xPos + 30, yPos + 450), pnt);

                cellWidth=50;
                paint.setTextSize(30);
                paint.setStrokeWidth(1);

                if(Cfg.ondo_spanMode) {
                    str = Cfg.getOndoFCNor0(maxVal);
                    xPos = (int) ((cellWidth - paint.measureText(str)) / 2);
                    bitcanvas.drawText(str, SCREEN_WIDTH - 120 + xPos, yPos + 35, paint);

                    str = Cfg.getOndoFCNor0((minVal + maxVal) / 2f);
                    xPos = (int) ((cellWidth - paint.measureText(str)) / 2);
                    bitcanvas.drawText(str, SCREEN_WIDTH - 120 + xPos, yPos + 222, paint);

                    str = Cfg.getOndoFCNor0(minVal);
                    xPos = (int) ((cellWidth - paint.measureText(str)) / 2);
                    bitcanvas.drawText(str, SCREEN_WIDTH - 120 + xPos, yPos + 434, paint);
                } else {
                    str = Cfg.getOndoFCNor0(x1.max1);
                    xPos = (int) ((cellWidth - paint.measureText(str)) / 2);
                    bitcanvas.drawText(str, SCREEN_WIDTH - 120 + xPos, yPos + 35, paint);

                    str = Cfg.getOndoFCNor0((x1.min1 + x1.max1) / 2f);
                    xPos = (int) ((cellWidth - paint.measureText(str)) / 2);
                    bitcanvas.drawText(str, SCREEN_WIDTH - 120 + xPos, yPos + 222, paint);

                    str = Cfg.getOndoFCNor0(x1.min1);
                    xPos = (int) ((cellWidth - paint.measureText(str)) / 2);
                    bitcanvas.drawText(str, SCREEN_WIDTH - 120 + xPos, yPos + 434, paint);
                }

                paint.setStrokeWidth(3);
                bitcanvas.drawLine(SCREEN_WIDTH- 120 + 25, yPos+35+25, SCREEN_WIDTH- 120 + 25, yPos + 185, paint);
                bitcanvas.drawLine(SCREEN_WIDTH- 120 + 25, yPos+222+25, SCREEN_WIDTH- 120 + 25, yPos + 380, paint);

            } else {
                // 온도캠 확장모드
                // 온도 그래프 (확장)
                xPos = (SCREEN_WIDTH-100);
                yPos = 80;
                bitcanvas.drawBitmap(camBitmap.mSpectrum, null, new Rect(xPos, yPos, xPos + 30, yPos + 750), pnt);

                int cellWidth=60;
                paint.setTextSize(32);
                paint.setStrokeWidth(1);

                if(Cfg.ondo_spanMode) {
                    str = Cfg.getOndoFCNor0(maxVal);
                    xPos = (int) ((cellWidth - paint.measureText(str)) / 2);
                    bitcanvas.drawText(str, SCREEN_WIDTH - 170 + xPos, yPos + 35, paint);

                    str = Cfg.getOndoFCNor0((minVal + maxVal) / 2);
                    xPos = (int) ((cellWidth - paint.measureText(str)) / 2);
                    bitcanvas.drawText(str, SCREEN_WIDTH - 170 + xPos, yPos + 350, paint);

                    str = Cfg.getOndoFCNor0(minVal);
                    xPos = (int) ((cellWidth - paint.measureText(str)) / 2);
                    bitcanvas.drawText(str, SCREEN_WIDTH - 170 + xPos, yPos + 734, paint);
                } else {
                    str = Cfg.getOndoFCNor0(x1.max1);
                    xPos = (int) ((cellWidth - paint.measureText(str)) / 2);
                    bitcanvas.drawText(str, SCREEN_WIDTH - 170 + xPos, yPos + 35, paint);

                    str = Cfg.getOndoFCNor0((x1.min1 + x1.max1) / 2);
                    xPos = (int) ((cellWidth - paint.measureText(str)) / 2);
                    bitcanvas.drawText(str, SCREEN_WIDTH - 170 + xPos, yPos + 350, paint);

                    str = Cfg.getOndoFCNor0(x1.min1);
                    xPos = (int) ((cellWidth - paint.measureText(str)) / 2);
                    bitcanvas.drawText(str, SCREEN_WIDTH - 170 + xPos, yPos + 734, paint);
                }

                paint.setStrokeWidth(3);
                bitcanvas.drawLine(SCREEN_WIDTH- 125 - 16, yPos+35+25, SCREEN_WIDTH- 125 - 16, yPos + 305, paint);
                bitcanvas.drawLine(SCREEN_WIDTH- 125 - 16, yPos+350+25, SCREEN_WIDTH- 125 - 16, yPos + 690, paint);
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
            float[] mOndoBuf = new float[CAMERA_WIDTH * CAMERA_HEIGHT];

            System.arraycopy(x1.ondoBuf, 0, mOndoBuf, 0, CAMERA_WIDTH * CAMERA_HEIGHT);

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

            float pointx = (SCREEN_WIDTH - CENTER_TARGET_SIZE) / 2;//905f;
            float pointy = ((SCREEN_HEIGHT - CENTER_TARGET_SIZE) / 2) + 56;//520f;

            //Log.d("bobopro", "point "+Float.toString(pointx) + ", "+Float.toString(pointy));

            float indexX = (pointx - xOrg);
            indexX = (indexX / ondoViewWidth)*(CAMERA_WIDTH-1);

            float indexY = (pointy - yOrg);
            indexY = (indexY / ondoViewHeight)*CAMERA_HEIGHT;

            int ondoIndex = (int)indexX+((int)indexY*CAMERA_WIDTH);
//
            float pointMax = -20.0f;
            for(int i=0; i<(((float)CENTER_TARGET_SIZE/1080.0)*(float)CAMERA_HEIGHT); i++){
                int y = i*CAMERA_WIDTH;
                for( int j=0; j<(((float)CENTER_TARGET_SIZE/1920.0)*(float)CAMERA_WIDTH); j++) {
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
                    indexX = (indexX / ondoViewWidth)*(CAMERA_WIDTH-1);
                    if (indexX>ondoViewWidth-11) { indexX = CAMERA_WIDTH-11; }
                    indexX -= 1;
                    if ( indexX<0 ) {indexX = 0;}

                    indexY = (pointy - yOrg);
                    indexY = (indexY / ondoViewHeight)*CAMERA_HEIGHT;
                    if ( indexY<0 ) {indexY = 0;}
                    if (indexY>ondoViewHeight-11) { indexY = CAMERA_HEIGHT-11; }
                    ondoIndex = (int)indexX+((int)indexY*CAMERA_WIDTH);

                    isPlayingAlarm = false;
                    pointMax = -20.0f;
                    for(int i=0; i<8; i++){
                        int y = i*CAMERA_WIDTH;
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


            Bitmap backbit = Bitmap.createBitmap(CAMERA_WIDTH, CAMERA_HEIGHT, Bitmap.Config.ARGB_8888);
            Canvas offscreen = new Canvas(backbit);
            offscreen.drawColor(0, PorterDuff.Mode.CLEAR);
            //offscreen.drawColor(Color.BLUE);
            Paint pnt = new Paint();
            pnt.setAntiAlias(false);

            int alpha =  (int)(Cfg.cam3_mixOppa * 2.55);
            if (ondoSelectMode) {
                alpha = (int)(imsiMixOppa * 2.55);
            }

            int xr = (int) xOrg;
            int yr = (int) yOrg;

            float yPer = (ondoViewHeight / (float)CAMERA_WIDTH);
//            Log.d("chanchan", "yPer "+Float.toString(yPer) + Float.toString(ondoViewWidth) + Float.toString(ondoViewHeight));
//            1280x1024
            for (int i = 0; i < (CAMERA_WIDTH * CAMERA_HEIGHT); i++) {
                float x, y;
                //if ((mOndoBuf[i] >= checkMinOndo) && (mOndoBuf[i] <= checkMaxOndo)) {
                if (mOndoBuf[i] >= checkMinOndo) {
                    if (!Float.isNaN(mOndoBuf[i])) {
                        x = ((float) i % CAMERA_WIDTH);
                        y = ((float) i / CAMERA_WIDTH);

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
//            Bitmap roiBitmap = Bitmap.createBitmap(backbit, 25, 19, 256-(25*2), 192-(19*2));
//            bitcanvas.drawBitmap(roiBitmap, null, new Rect(xr, yr, (xr + 1920), (yr + 1080)), pnt);
//            Log.d("chanchan", "box "+ Float.toString(xr) + ", "+ Float.toString(yr) + ", "+ Float.toString(ondoViewWidth) + ", " + Float.toString(ondoViewHeight));

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
                int heightGap = (int)((190.0/height) * CAMERA_HEIGHT);
                for (int y = 0; y < CAMERA_HEIGHT - heightGap; y++) {
                    for (int x = 0; x < CAMERA_WIDTH; x++) {
                        float value = mOndoBuf[y * CAMERA_WIDTH + x];
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

                minOndoX = (minOndoX * ondoViewWidth) / CAMERA_WIDTH;
                minOndoY = (minOndoY * ondoViewHeight) / CAMERA_HEIGHT;
                maxOndoX = (maxOndoX * ondoViewWidth) / CAMERA_WIDTH;
                maxOndoY = (maxOndoY * ondoViewHeight) / CAMERA_HEIGHT;

                int xMinOffset = 0;
                int yMinOffset = 0;
                int xMaxOffset = 0;
                int yMaxOffset = 0;

                if(minOndoX > 1920 - 200) {
                    xMinOffset = -200;
                }

                if(minOndoY < 50) {
                    yMinOffset = 50;
                }

                if(maxOndoX > 1920 - 200) {
                    xMaxOffset = -200;
                }

                if(maxOndoY < 50) {
                    yMaxOffset = 50;
                }

                str = String.format("MIN:%s%s" ,(int)Cfg.getOndoFC(minOndo), "°"+Cfg.p1_cGiho) ;
                bitcanvas.drawText(str, minOndoX + 10 + xMinOffset, minOndoY + yMinOffset, paint);
                bitcanvas.drawRect(minOndoX, minOndoY, minOndoX + 10, minOndoY + 10, paint);

                str = String.format("Max:%s%s" ,(int)Cfg.getOndoFC(maxOndo), "°"+Cfg.p1_cGiho) ;
                bitcanvas.drawText(str, maxOndoX + 10 + xMaxOffset, maxOndoY + yMaxOffset, paint);
                bitcanvas.drawRect(maxOndoX, maxOndoY, maxOndoX + 10, maxOndoY + 10, paint);

//                Log.d("chan dra min", "min:" + minOndoX + "," + minOndoY);
//                Log.d("chan dra max", "max:" + maxOndoX + "," + maxOndoY);

                int xPos = (1920 - CENTER_TARGET_SIZE) / 2;
                int yPos = 480+56;


                if (camType == Consts.MODE_CAM_NOR) {
                    // 타켓 필드 그리기 - 온도용
                    pnt.setAntiAlias(true);
                    bitcanvas.drawBitmap(camBitmap.mMixTarget, null, new Rect(xPos, yPos, xPos + CENTER_TARGET_SIZE, yPos + CENTER_TARGET_SIZE), pnt);

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