package com.inspeco.data;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Locale;

public class Cfg {
    // 열화상 카메라의 해상도 설정(기본값: 256x192)
    public static int thermal_cam_width = 256;
    public static int thermal_cam_height = 192;
    public static float   cam2_maxOndo = 50;
    public static float   cam2_minOndo = 10;
    public static boolean cam2_maxAuto = true;
    public static boolean cam2_minAuto = true;
    public static int     cam2_colorMode = 1;

    public static float cam3_max120Ondo = 60;
    public static float cam3_min120Ondo = 10;
    public static float cam3_max600Ondo = 500;
    public static float cam3_min600Ondo = 120;


    public static boolean cam3_max120Auto = true;
    public static boolean cam3_min120Auto = true;
    public static boolean cam3_max600Auto = true;
    public static boolean cam3_min600Auto = true;

    public static float   cam3_checkOndo = 30;

    public static float   cam3_mixOppa = 1;
    public static int     cam3_colorMode = 1;

    public static float   ondo_offSet = 0f;
    public static boolean ondo_extMode = false;

    public static boolean ondo_spanMode = false;

    public static float cam3_xOrg = 0f;
    public static float cam3_yOrg = 0f;
    public static float cam3_scale = 1f;
    public static float cam3_hRatio = 1f;
    public static float cam3_vRatio = 1f;
    public static String userName = "X1";

    public static int RecordingTime = 10;

    public static Integer p1_avrDbSec = 10;
    public static Integer p1_maxDbSec = 10;
    public static Integer p1_volume = 50;
    public static Integer p1_eFeq = 15;
    public static float p1_dist = 12.5f;

    public static boolean p1_alram_set = false;
    public static Integer p1_alram_db = 15;
    public static Integer p1_alram_ondo = 45;
    public static Integer p1_alram_checksec = 5;
    public static Integer p1_alram_sec = 2;
    public static boolean p1_alram_vibe = false;
    public static boolean p1_alram_sound = false;
    public static boolean p1_alram_icon = false;

    public static String p1_dateStr = "YYMMDD";
    public static String p1_dateStr2 = "yyyy-MM-dd";
    public static Integer p1_recTimeWave = 10;
    public static Integer p1_recTimeMovie = 10;
    public static boolean p1_isC = true;
    public static String p1_cGiho = "C";

    public static boolean p1_isMainWaveShow = true;

    public static boolean isFirst = true;

    public static void clear_p1Config() {
        isFirst = true;
        cam2_maxOndo = 50;
        cam2_minOndo = 10;
        cam2_maxAuto = true;
        cam2_minAuto = true;
        cam2_colorMode = 1;

        cam3_max120Ondo = 60;
        cam3_min120Ondo = 10;
        cam3_max120Ondo = 500;
        cam3_min120Ondo = 120;

        cam3_max120Auto = true;
        cam3_min120Auto = true;
        cam3_max600Auto = true;
        cam3_min600Auto = true;
        cam3_checkOndo = 30;

        cam3_mixOppa = 80;
        cam3_colorMode = 1;

        ondo_offSet = 0;

        cam3_xOrg = 0f;
        cam3_yOrg = 0f;
        cam3_scale = 1f;
        cam3_hRatio = 1f;
        cam3_vRatio = 1f;
        RecordingTime = 10;

        p1_avrDbSec = 10;
        p1_maxDbSec = 10;
        p1_volume = 50;
        p1_eFeq = 15;
        p1_dist = 12.5f;

        p1_alram_set = false;
        p1_alram_db = 15;
        p1_alram_checksec = 5;
        p1_alram_sec = 2;
        p1_alram_vibe = false;
        p1_alram_sound = false;
        p1_alram_icon = false;

        p1_dateStr = "yyyyMMdd";
        p1_dateStr2 = "yyyy-MM-dd";

        p1_recTimeWave = 10;
        p1_recTimeMovie = 10;
        p1_isC = true;
        p1_isMainWaveShow = true;

        String lang = Locale.getDefault().getLanguage();
        if (lang == "en") {
            p1_dateStr = "MMddyyyy";
            p1_dateStr2 = "MM-dd-yyyy";
            Cfg.p1_isC = false;
        }
        if (Cfg.p1_isC) {
            p1_cGiho="C";
        } else {
            p1_cGiho="F";
        }

    }

    public static void load_p1(Context context) {
        Log.d("bobopro Cfg", "load p1 : "+userName);
        SharedPreferences pref = context.getSharedPreferences(userName, Activity.MODE_PRIVATE);
        isFirst = pref.getBoolean("isFirst", true);
        p1_avrDbSec = pref.getInt("p1_avrDbSec", 10);
        p1_maxDbSec = pref.getInt("p1_maxDbSec", 10);
        p1_volume = pref.getInt("p1_volume", 50);
        p1_eFeq = pref.getInt("p1_eFeq", 15);
        p1_dist = pref.getFloat("p1_dist", 12.5f);

        p1_alram_set = pref.getBoolean("p1_alram_set", false);

        p1_alram_db = pref.getInt("p1_alram_db", 15);
        p1_alram_ondo = pref.getInt("p1_alram_ondo", 45);
        p1_alram_checksec = pref.getInt("p1_alram_checksec", 5);
        p1_alram_sec = pref.getInt("p1_alram_sec", 2);

        p1_alram_vibe = pref.getBoolean("p1_alram_vibe", false);
        p1_alram_sound = pref.getBoolean("p1_alram_sound", false);
        p1_alram_icon = pref.getBoolean("p1_alram_icon", false);

        p1_dateStr = pref.getString("p1_dateStr", "yyyyMMdd");
        p1_dateStr2 = pref.getString("p1_dateStr2", "yyyy-MM-dd");
        p1_recTimeWave = pref.getInt("p1_recTimeWave", 10);
        p1_recTimeMovie = pref.getInt("p1_recTimeMovie", 10);
        p1_isC = pref.getBoolean("p1_isC", true);

        p1_isMainWaveShow = pref.getBoolean("p1_isMainWaveShow", true);

        if (isFirst) {
            clear_p1Config();
        }

        if (Cfg.p1_isC) {
            p1_cGiho="C";
            Log.d("bobopro Cfg", "========= is C");
        } else {
            p1_cGiho="F";
            Log.d("bobopro Cfg", "========= is F");
        }

    }


    public static float getOndoFC(float value) {
        if (!Cfg.p1_isC) {
            return ( value * 1.8f ) + 32f;
        } else return value;
    }

    public static String getOndoFCNorGiho(float value, String giho) {
        float calcOndo = 0f;
        if (!Cfg.p1_isC) {
            calcOndo =  ( value * 1.8f ) + 32f;
        } else calcOndo = value;

        if (Float.isNaN(calcOndo) || calcOndo<-19.9) {
            return " - ";
        } else if ( ondo_extMode && calcOndo<100.0) {
            return " - ";
        } else {
            String str = String.format("%.1f", calcOndo);

            //Log.i("bobopro1", str);
                if (str.length()>8) {
                    str = " - ";
                } else {
                    str = str + giho;
                }
            //Log.i("bobopro2", str);
            return str;
        }
    }

    public static String getOndoFCNor0(float value) {
        float calcOndo = 0f;
        if (!Cfg.p1_isC) {
            calcOndo =  ( value * 1.8f ) + 32f;
        } else calcOndo = value;

        if (Float.isNaN(calcOndo) || calcOndo<-19.9) {
            return " - ";
        } else if ( ondo_extMode && calcOndo<100.0) {
            return " - ";
        } else {
            String str = String.format("%.0f", calcOndo);
                if (str.length()>8) {
                    str = " - ";
            }

            return str;
        }

    }



    public static void save_p1(Context context) {
        Log.d("bobopro Cfg", "save p1 "+userName);
        SharedPreferences pref = context.getSharedPreferences(userName, Activity.MODE_PRIVATE);
        pref.edit().putBoolean("isFirst", false).apply();
        isFirst=false;
        pref.edit().putInt("p1_avrDbSec", p1_avrDbSec).apply();
        pref.edit().putInt("p1_maxDbSec", p1_maxDbSec).apply();
        pref.edit().putInt("p1_volume", p1_volume).apply();
        pref.edit().putInt("p1_eFeq", p1_eFeq).apply();
        pref.edit().putFloat("p1_dist", p1_dist).apply();

        pref.edit().putBoolean("p1_alram_set", p1_alram_set).apply();
        pref.edit().putInt("p1_alram_db", p1_alram_db).apply();
        pref.edit().putInt("p1_alram_checksec", p1_alram_checksec).apply();
        pref.edit().putInt("p1_alram_ondo", p1_alram_ondo).apply();
        pref.edit().putInt("p1_alram_sec", p1_alram_sec).apply();
        pref.edit().putBoolean("p1_alram_vibe", p1_alram_vibe).apply();
        pref.edit().putBoolean("p1_alram_sound", p1_alram_sound).apply();
        pref.edit().putBoolean("p1_alram_icon", p1_alram_icon).apply();

        pref.edit().putString("p1_dateStr", p1_dateStr).apply();
        pref.edit().putString("p1_dateStr2", p1_dateStr2).apply();
        pref.edit().putInt("p1_recTimeWave", p1_recTimeWave).apply();
        pref.edit().putInt("p1_recTimeMovie", p1_recTimeMovie).apply();
        pref.edit().putBoolean("p1_isC", p1_isC).apply();

        if (Cfg.p1_isC) {
            Log.d("bobopro Cfg", "========= is C");
        } else {
            Log.d("bobopro Cfg", "========= is F");
        }


        pref.edit().putBoolean("p1_isMainWaveShow", p1_isMainWaveShow).apply();

    }


    public static void save_cam2Ondo(Context context) {
        Log.d("bobopro Cfg", "save2 ondo "+userName);
        SharedPreferences pref = context.getSharedPreferences(userName, Activity.MODE_PRIVATE);
        pref.edit().putBoolean("isFirst", false).apply();
        isFirst=false;
        pref.edit().putFloat("CAM2_MAX_ONDO", cam2_maxOndo).apply();
        pref.edit().putFloat("CAM2_MIN_ONDO", cam2_minOndo).apply();
        pref.edit().putBoolean("CAM2_MAX_AUTO", cam2_maxAuto).apply();
        pref.edit().putBoolean("CAM2_MIN_AUTO", cam2_minAuto).apply();
        pref.edit().putInt("CAM2_COLOR_MODE", cam2_colorMode).apply();
        pref.edit().putFloat("ONDO_OFFSET", ondo_offSet).apply();
        pref.edit().putBoolean("ONDO_EXTMODE", ondo_extMode).apply();
    }


    public static void save_cam3Ondo(Context context) {
        Log.d("bobopro Cfg", "save3 ondo "+userName);
        SharedPreferences pref = context.getSharedPreferences(userName, Activity.MODE_PRIVATE);
        pref.edit().putBoolean("isFirst", false).apply();
        isFirst=false;
        pref.edit().putFloat("CAM3_MAX120_ONDO", cam3_max120Ondo).apply();
        pref.edit().putFloat("CAM3_MIN120_ONDO", cam3_min120Ondo).apply();
        pref.edit().putFloat("CAM3_MAX600_ONDO", cam3_max600Ondo).apply();
        pref.edit().putFloat("CAM3_MIN600_ONDO", cam3_min600Ondo).apply();
        pref.edit().putFloat("CAM3_CHECK_ONDO", cam3_checkOndo).apply();
        pref.edit().putFloat("CAM3_MIX_OPPA", cam3_mixOppa).apply();
//        pref.edit().putFloat("CAM3_CHECKMIN_ONDO", cam3_checkMinOndo).apply();
//        pref.edit().putFloat("CAM3_CHECKMAX_ONDO", cam3_checkMaxOndo).apply();
        pref.edit().putBoolean("CAM3_MAX120_AUTO", cam3_max120Auto).apply();
        pref.edit().putBoolean("CAM3_MIN120_AUTO", cam3_min120Auto).apply();
        pref.edit().putBoolean("CAM3_MAX600_AUTO", cam3_max600Auto).apply();
        pref.edit().putBoolean("CAM3_MIN600_AUTO", cam3_min600Auto).apply();
        pref.edit().putInt("CAM3_COLOR_MODE", cam3_colorMode).apply();
        pref.edit().putFloat("CAM3_XORG", cam3_xOrg).apply();
        pref.edit().putFloat("CAM3_YORG", cam3_yOrg).apply();
        pref.edit().putFloat("CAM3_SCALE", cam3_scale).apply();
        pref.edit().putFloat("CAM3_HRATIO", cam3_hRatio).apply();
        pref.edit().putFloat("CAM3_VRATIO", cam3_vRatio).apply();
        pref.edit().putFloat("ONDO_OFFSET", ondo_offSet).apply();
        pref.edit().putBoolean("ONDO_EXTMODE", ondo_extMode).apply();
    }


    public static void load_camOndo(Context context) {
        Log.d("bobopro Cfg", "load ondo "+userName);

        SharedPreferences pref = context.getSharedPreferences(userName, Activity.MODE_PRIVATE);
        cam2_maxOndo = pref.getFloat("CAM2_MAX_ONDO", 30.0f);
        cam2_minOndo = pref.getFloat("CAM2_MIN_ONDO", 10.0f);
        cam3_max120Ondo = pref.getFloat("CAM3_MAX120_ONDO", 30.0f);
        cam3_min120Ondo = pref.getFloat("CAM3_MIN120_ONDO", 10.0f);
        cam3_max600Ondo = pref.getFloat("CAM3_MAX600_ONDO", 30.0f);
        cam3_min600Ondo = pref.getFloat("CAM3_MIN600_ONDO", 10.0f);

        cam3_checkOndo = pref.getFloat("CAM3_CHECK_ONDO", 24.0f);
        cam3_mixOppa = pref.getFloat("CAM3_MIX_OPPA", 80.0f);
        //cam3_checkMinOndo = pref.getFloat("CAM3_CHECKMIN_ONDO", 10.0f);
        //cam3_checkMaxOndo = pref.getFloat("CAM3_CHECKMAX_ONDO", 10.0f);

        cam2_maxAuto = pref.getBoolean("CAM2_MAX_AUTO", true);
        cam2_minAuto = pref.getBoolean("CAM2_MIN_AUTO", true);
        cam3_max120Auto = pref.getBoolean("CAM3_MAX120_AUTO", true);
        cam3_min120Auto = pref.getBoolean("CAM3_MIN120_AUTO", true);
        cam3_max600Auto = pref.getBoolean("CAM3_MAX600_AUTO", true);
        cam3_min600Auto = pref.getBoolean("CAM3_MIN600_AUTO", true);

        cam3_xOrg = pref.getFloat("CAM3_XORG", 0.0f);
        cam3_yOrg = pref.getFloat("CAM3_YORG", 0.0f);
        cam3_scale = pref.getFloat("CAM3_SCALE", 1.0f);
        cam3_hRatio = pref.getFloat("CAM3_HRATIO", 1.0f);
        cam3_vRatio = pref.getFloat("CAM3_VRATIO", 1.0f);
        ondo_offSet = pref.getFloat("ONDO_OFFSET", 0.0f);
        ondo_extMode = pref.getBoolean("ONDO_EXTMODE", false);

        cam2_colorMode = pref.getInt("CAM2_COLOR_MODE", 1);
        cam3_colorMode = pref.getInt("CAM3_COLOR_MODE", 1);
    }



    public static String load_UserList(Context context) {
        SharedPreferences pref = context.getSharedPreferences("X1", Activity.MODE_PRIVATE);
        return pref.getString("Users","[]");
    }

    public static void save_UserList(Context context, String str) {
        SharedPreferences pref = context.getSharedPreferences("X1", Activity.MODE_PRIVATE);
        pref.edit().putString("Users", str).apply();
    }



}
