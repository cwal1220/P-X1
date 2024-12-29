package com.inspeco.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.inspeco.X1.R;

import java.util.ArrayList;

public class CamBitmap {


    public Bitmap mIconGps, mIconHumi, mIconOndo, mIconLevel, mIconSpan;
    public Bitmap mTargetField, mOndoTarget, mMixTarget;
    public Bitmap mOndoPointer;
    public Bitmap mSpectrum;


    public ArrayList<Bitmap> lvMeter;
    public ArrayList<Bitmap> lvCenter;

    private static class LazyHolder {
        public static final CamBitmap uniqueInstance = new CamBitmap();
    }

    public static CamBitmap getInstance() {
        return CamBitmap.LazyHolder.uniqueInstance;
    }

    private CamBitmap() {
       lvMeter = new ArrayList<Bitmap>();
       lvCenter = new ArrayList<Bitmap>();

    }

    public void loadBitmap(Context context) {
        mIconGps = BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_icon_gps);
        mIconHumi = BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_icon_humi);
        mIconOndo = BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_icon_ondo);
        mIconLevel = BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_icon_level);
        mIconSpan = BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_icon_span);
        mTargetField = BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_img_target_field);
        mOndoTarget = BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_img_ondo_target);
        mMixTarget = BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_img_mix_target);
        mOndoPointer = BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_icon_ondo_pointer);
        mSpectrum = BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_spectrum);

        lvMeter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_00));
        lvMeter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_01));
        lvMeter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_02));
        lvMeter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_03));
        lvMeter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_04));
        lvMeter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_04));
        lvMeter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_05));
        lvMeter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_06));
        lvMeter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_07));
        lvMeter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_08));
        lvMeter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_09));
        lvMeter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_10));
        lvMeter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_11));
        lvMeter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_12));
        lvMeter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_13));
        lvMeter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_14));
        lvMeter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_15));
        lvMeter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_16));
        lvMeter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_17));
        lvMeter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_18));
        lvMeter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_19));
        lvMeter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_20));
        lvMeter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_21));
        lvMeter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_22));
        lvMeter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_23));
        lvMeter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_24));
        lvMeter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_25));
        lvMeter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_26));
        lvMeter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_27));
        lvMeter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_28));
        lvMeter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_29));
        lvMeter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_30));

        lvCenter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_c00));
        lvCenter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_c01));
        lvCenter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_c02));
        lvCenter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_c03));
        lvCenter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_c04));
        lvCenter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_c04));
        lvCenter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_c05));
        lvCenter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_c06));
        lvCenter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_c07));
        lvCenter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_c08));
        lvCenter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_c09));
        lvCenter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_c10));
        lvCenter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_c11));
        lvCenter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_c12));
        lvCenter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_c13));
        lvCenter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_c14));
        lvCenter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_c15));
        lvCenter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_c16));
        lvCenter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_c17));
        lvCenter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_c18));
        lvCenter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_c19));
        lvCenter.add(BitmapFactory.decodeResource(context.getResources(), R.mipmap.camv_lv_c20));

    }



}
