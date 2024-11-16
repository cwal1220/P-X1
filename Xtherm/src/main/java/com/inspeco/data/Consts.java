package com.inspeco.data;

/**
 * 상수 정의
 */
public class Consts {

    public static int CAM_WEBCAM = 10;
    public static int CAM_ONDO = 20;
    public static int CAM_MIX = 30;

    public static int VIEW_NONE   = 0;
    public static int VIEW_SPLASH = 10;
    public static int VIEW_HOME   = 100;
    public static int VIEW_CAM    = 200;
    public static int VIEW_DIAG   = 300;
    public static int VIEW_REPORT = 400;
    public static int VIEW_SETTING = 500;
    public static int VIEW_GRAPH_WAVE = 601;
    public static int VIEW_GRAPH_FFT = 602;

    public static int  MODE_CAM_NOR = 11;
    public static int  MODE_CAM_EXT = 12;

    public static int  PaletteRainbow = 1;
    public static int  PaletteAmber = 2;
    public static int  PaletteWhite = 3;

    public static int DEVICE_ATTATCHED = 1;
    public static int DEVICE_DETTATCHED = 2;
    public static int DEVICE_NEEDPERMISSION = 3;
    public static int DEVICE_DISCONNECTED = 20;
    public static int DEVICE_CONNNECTED = 10;

    public static int Diag_3Sang = 3;
    public static int Diag_OndoPattern = 4;
    public static int Diag_mixMode = 0;
    public static int Diag_waveMode = 1;
    public static int Diag_ondoMode = 2;

    public static int Diag_FacilitySupply = 1;
    public static int Diag_FacilitySend = 2;
    public static int Diag_FacilityTrans = 3;

    public static int Diag_MaterialCeramic = 1;
    public static int Diag_MaterialPolymer = 2;
    public static int Diag_MaterialGlass = 3;
    public static int Diag_MaterialEtc = 4;


    public static String WifiName = "INSPECO";

    public static int  RATE_HZ = 44100;
    public static int  SAMPLE_SIZE = 4096;
    public static int  SAMPLE_SIZE_TCP = 4000;
    public static int  FFT_SAMPLE_SIZE_TCP = 8000;
    public static int  FFT_SAMPLE_SIZE_UDP = 4096;
    public static int  FLOAT_SIZE = 4096;

    public static String  LOCAL_BROAD_CASTING_GRAPH = "co.kr.inspeco.p1.LOCAL_BC_GRAPH"; //내부 브로드 캐스팅
    public static String  LOCAL_BROAD_CASTING_RECORD = "co.kr.inspeco.p1.LOCAL_BC_RECORD"; //내부 브로드 캐스팅
    public static String  LOCAL_BROAD_CASTING_CAMERA = "co.kr.inspeco.p1.LOCAL_BC_CAMERA"; //내부 브로드 캐스팅
    public static String  LOCAL_BROAD_CASTING_MAIN_LASER = "co.kr.inspeco.p1.LOCAL_BC_MAIN_LASER"; //내부 브로드 캐스팅
    public static String  PERMISSION = "co.kr.inspeco.p1.broadcast.permission.ACTION_PERMISSION";



    // 폴더명 지정
    //public static int  ROOT_FOLDER = "FD-DISEF-02"
    //public static int  ROOT_FOLDER2 = ".FD-DISEF-02"
    //public static int  ROOT_FOLDER = "P1"
    //public static int  ROOT_FOLDER2 = ".P1"
    public static String  ROOT_FOLDER = "X1";
    public static String  ROOT_FOLDER2 = ".X1";
    //public static int  ROOT_FOLDER = "Inspeco"
    //public static int  ROOT_FOLDER2 = ".InspecoH"


    public static int Report_FileUdr = 1;
    public static int Report_FileReport = 2;

    public static String  AUDIO_RECORDER_FOLDER = "AudioRecorder";
    public static String  AUDIO_RECORDER_MIX_FOLDER = "AudioRecorderMix";
    public static String  AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
    public static String  SCREEN_SHOT_FOLDER = "ScreenShot";
    public static String  WAVEFORM_SHOT_FOLDER = "WaveFormShot";
    public static String  VIDEO_FOLDER = "Video";
    public static String  REC_FOLDER = "Rec";
    public static String  UDR_WAVE_FOLDER = "UDR_W";
    public static String  UDR_TEMP_FOLDER = "UDR_T";
    public static String  UDR_MIX_FOLDER = "UDR_M";
    public static String  REPORT_WAVE_FOLDER = "Report_W";
    public static String  REPORT_TEMP_FOLDER = "Report_T";
    public static String  REPORT_MIX_FOLDER = "Report_M";
    public static String  DOCUMENT_FOLDER = "Document";
    public static String  DIAGNOSIS_INI = "diagnosis.txt";
    public static String  DIAGNOSIS_PL = "diag_pl.txt";
    public static String  DIAGNOSIS_COND = "diag_condi.txt";
    public static String  DIAGNOSIS_EQUIP = "diag_equip.txt";
    public static String  DIAGNOSIS_DVOLT = "diag_dvolt.txt";
    public static String  DIAGNOSIS_TVOLT = "diag_tvolt.txt";

    // request code
    public static int  REQUEST_CAPTURE = 1001;
    public static int  REQUEST_REC = 1002;
    public static int  REQUEST_SEL_IMG_POLE = 1003;
    public static int  REQUEST_SEL_IMG_EQUIPMENT = 1004;
    public static int  REQUEST_SEL_IMG_WAVEFORM = 1005;
    public static int  REQUEST_SEL_AUDIO_FILE = 1006;
    public static int  REQUEST_SEL_UDR_FILE = 1007;
    public static int  REQUEST_SEL_REP_FILE = 1008;
    public static int  REQUEST_SEL_SHARE = 1009;

    // intent key
    public static String  SELECT_FILE = "selectFile";
    public static String  SEL_TYPE = "sel_type";
    public static int  SEL_TYPE_POLE = 0;
    public static int  SEL_TYPE_EQUIPMENT = 1;
    public static int  SEL_TYPE_AUDIO = 2;
    public static int  SEL_TYPE_UDR = 3;
    public static int  SEL_TYPE_REP = 4;
    public static int  SEL_TYPE_VIDEO = 5;
    public static int  SEL_TYPE_DOCUMENT = 6;
    public static int  SEL_TYPE_PIC_WAVEFORM = 7;

    // 하단 버튼의 갯수
    public static int  BOT_BTN_COUNT = 7;
    // 하단 버튼의 고유 아이디
    public static int  BTN_VIDEO_MODE = 0; // 비디오 모드 변경 버튼
    public static int  BTN_CATPURE = 1;   // 스크린샷 버튼
    public static int  BTN_RECORDING = 2; // 레코딩 버튼
    public static int  BTN_LOCK= 3;   // 키 lock
    public static int  BTN_MUTE= 4;   // mute
    public static int  BTN_SETTING = 5; // 설정 버튼
    public static int  BTN_DIAGNOSIS = 6; // 상태판정 버튼
    public static int  BTN_REPORT = 7; // 리포트 버튼
    public static int  BTN_SHARE = 8; // 공유
    public static int  BTN_GALLERY = 9; // 갤러리
//    public static int  BTN_DEFAULT = 9 // 기본값

    // 주파수(freq) 최대 / 최소
    public static int  MAX_FREQ = 140;
    public static int  MIN_FREQ = 0;
    // 감도(sens) 최대/최소
    public static int  MAX_SENS = 300;
    public static int  MIN_SENS = 0;
    // 볼륨(vol) 최대/최소
    public static int  MAX_VOL = 100;
    public static int  MIN_VOL = 1;

    public static String  PREFIX_S = "S";
    public static String  PREFIX_F = "F";
}