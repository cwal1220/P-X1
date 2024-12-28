package com.inspeco.X1.CamView

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.*
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import com.inspeco.X1.R
import com.inspeco.X1.SettingView.SetCam1Fragment
import com.inspeco.X1.SettingView.SetCam2Fragment
import com.inspeco.X1.SettingView.SetCam3Fragment
import com.inspeco.X1.XTerm.ByteUtil
import com.inspeco.X1.StatusJudgView.DiagActivity
import com.inspeco.data.*
import com.inspeco.dialog.CommonDialog
import com.serenegiant.common.BaseActivity
import com.serenegiant.usb.UVCCamera
import com.serenegiant.usb2.WebCam
import com.serenegiant.usbcameracommon.UVCCameraHandler
import com.serenegiant.widget.TouchPoint
import com.serenegiant.widget.UVCCameraTextureView
import kotlinx.android.synthetic.main.activity_web_cam.*
import kotlinx.android.synthetic.main.new_settings.*
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList


// adb tcpip 5555
//adb connect 192.168.0.133

class WebCamActivity : BaseActivity() {
    private val TAG = "bobopro-WebCamActivity"
    private val TEST_MODE_NOCAM = false
    private var mWebCam: WebCam? = null

    private lateinit var p1: P1
    //private lateinit var x1: X1
    var sufaceClickTime = 0L
    private lateinit var mContext : Context
    // 실화상
    private var mWebCamSurface: WebCamTextureView? = null

    // 열화상
    private var mCameraHandler: UVCCameraHandler? = null
    private var mUVCCameraView: OndoTextureView? = null

    private var mTouchHolder: SurfaceHolder? = null
    private lateinit var touchPointList: CopyOnWriteArrayList<TouchPoint>

    private var isActive = false
    private var isPreview = false
    private val mSync = Any()

    private var mWidth = 0f
    private var mHeight = 0f

    private var isLoadingOndo = false

    // 녹음 중 progress 를 위한 타이머
    private var progressTimer = Timer()

    private var alarmCheckTimer = Timer()

    var mPositionX = 0f
    var mPositionY = 0f

    lateinit var mScaleDetector: ScaleGestureDetector

    lateinit var set_cam1_fragment: SetCam1Fragment
    lateinit var set_cam2_fragment: SetCam2Fragment
    lateinit var set_cam3_fragment: SetCam3Fragment

    private val mAudioSource = MediaRecorder.AudioSource.DEFAULT
    private val mSampleRate = 44100
    private val mChannelCount: Int = AudioFormat.CHANNEL_IN_MONO
    private val mAudioFormat: Int = AudioFormat.ENCODING_PCM_16BIT
    private var mBufferSize = AudioTrack.getMinBufferSize(mSampleRate, mChannelCount, mAudioFormat)

    private var mAudioRecord: AudioRecord? = null
    var mAudioTrack: AudioTrack? = null
    private var isRecording = false
    private var prepareRecord = false
    private var isAudioRunning=false


    private var Fix = 0f
    private  var Refltmp = 0f
    private  var Airtmp = 0f
    private  var humi = 0f
    private  var emiss = 0f
    private var distance = 0
    private var stFix = ""
    private var stRefltmp = ""
    private  var stAirtmp = ""
    private  var stHumi = ""
    private  var stEmiss = ""
    private  var stDistance = ""


    private val mByteUtil = ByteUtil()
    private val mSendCommand = SendCommand()
    /////////////////////////////////////////
    /////캡쳐를 위한 멤버
    ////////////////////////////////////////
    private var mediaProjectionManager: MediaProjectionManager? = null
    // 미디어 프로젝션 객체
    lateinit var mediaProjection: MediaProjection
    // 화면크기를 정하고 surface를 통해 화면을 그리게 됨
    lateinit var projectionHandler : Handler

    private val vitualDisplayFlags = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY or DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC

    private lateinit var mImageReader: ImageReader
    private lateinit var mVirtualDisplay: VirtualDisplay
    private lateinit var mDisplay: Display
    private var mDensity = 0
    private var videoFolder:File? = null// 녹화본 저장 폴더
    // 녹화 파일 이름
    private var recVideoName = ""

    // 비디오 녹화중인지를 판단하기 위한 값
    private var isVideoRecord = false
    // 비디오 녹화중 텍스트 표시 타이머
    var videoTimer :Timer? = null// 비디오 녹화 타이머



    inner class AudioPlayThread:Thread(){
        override fun run(){
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
            mAudioTrack = AudioTrack(AudioManager.STREAM_MUSIC, mSampleRate, AudioFormat.CHANNEL_OUT_MONO, mAudioFormat, mBufferSize, AudioTrack.MODE_STREAM)
            mAudioTrack!!.play();
            val readData = ByteArray(mBufferSize)
            while(isAudioRunning){
                val ret = mAudioRecord!!.read(readData, 0, 1024)
                if (ret > 0) {
                    mAudioTrack!!.write(readData, 0, ret);
                }

                Log.d("bobopro", "Audio Input ${ret}")
                //SystemClock.sleep(1)
                //runOnUiThread(UIClass())
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR)

        setContentView(R.layout.activity_web_cam)
        mContext = this

//        webCamInitCounter = 0
//        ondoCamInitCounter = 0

        var displayMetrics = getResources().getDisplayMetrics()
        mWidth = displayMetrics.widthPixels.toFloat()
        mHeight = displayMetrics.heightPixels.toFloat()
        States.screenWidth = displayMetrics.widthPixels
        States.screenHeight = displayMetrics.heightPixels
        States.densityDpi = displayMetrics.densityDpi

        Log.e(TAG, "onCreate ${States.screenWidth} x ${States.screenHeight} ")

        p1 = P1.getInstance()

        p1.xOrg = Cfg.cam3_xOrg
        p1.yOrg = Cfg.cam3_yOrg
        p1.scaleFactor = ((Cfg.cam3_vRatio + Cfg.cam3_hRatio) /2 ) / 100
        p1.vRatio = Cfg.cam3_vRatio
        p1.hRatio = Cfg.cam3_hRatio

        mWebCamSurface = findViewById<View>(R.id.webcam_view) as WebCamTextureView
        mUVCCameraView = findViewById<View>(R.id.camera_view) as OndoTextureView

        mScaleDetector = ScaleGestureDetector(this, scaleListener)
        initButtons()
        initTouchView()

        p1.arrangeMode = false
        p1.ondoSelectMode = false
        arrange_Save_button.visibility = View.GONE
        arrange_cancel_button.visibility = View.GONE
        test_button.visibility = View.GONE
        mix_ondoSetPanel.visibility = View.GONE
        mix_arrangeSetPanel.visibility = View.GONE

        progress_rec.visibility = View.GONE

        Log.v(TAG, "onCreate")

        p1.camType == Consts.MODE_CAM_NOR

        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        // start capture handling thread
        object : Thread() {
            override fun run() {
                Looper.prepare()
                projectionHandler = Handler()
                Looper.loop()
            }
        }.start()

        rightmenu_list.visibility = View.GONE

        States.isViewResumePass = false
        States.isRecording = false

    }


//    override fun onConfigurationChanged(newConfig: Configuration) {
//        super.onConfigurationChanged(newConfig)
//
//        var displayMetrics = getResources().getDisplayMetrics()
//        mWidth = displayMetrics.widthPixels.toFloat()
//        mHeight = displayMetrics.heightPixels.toFloat()
//        States.screenWidth = displayMetrics.widthPixels
//        States.screenHeight = displayMetrics.heightPixels
//        States.densityDpi = displayMetrics.densityDpi
//
//        // Checks the orientation of the screen
//        if (newConfig.orientation === Configuration.ORIENTATION_LANDSCAPE) {
//            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show()
//
//        } else if (newConfig.orientation === Configuration.ORIENTATION_PORTRAIT) {
//            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show()
//        }
//    }


    override fun onResume() {
        super.onResume()
        Log.v(TAG, "onResume")
//        Log.v(TAG, "onResume ============== 1")
//        Log.v(TAG, "onResume ============== 2")
//        Log.v(TAG, "onResume ============== 3")
//        Log.v(TAG, "onResume ============== 4")
//        Log.v(TAG, "onResume ============== 5")
        Log.v(TAG, "onResume ============== $prepareRecord")

        if ((!States.isRecording) && (!States.isViewResumePass)) {
            Handler().postDelayed({
                Log.v(TAG, "onResume ----- run 1")
//                Log.v(TAG, "onResume ----- run 2")
//                Log.v(TAG, "onResume ----- run 3")
                States.curView = Consts.VIEW_CAM

                var displayMetrics = getResources().getDisplayMetrics()
                mWidth = displayMetrics.widthPixels.toFloat()
                mHeight = displayMetrics.heightPixels.toFloat()
                States.screenWidth = displayMetrics.widthPixels
                States.screenHeight = displayMetrics.heightPixels
                States.densityDpi = displayMetrics.densityDpi

                mWebCamSurface = findViewById<View>(R.id.webcam_view) as WebCamTextureView
                mUVCCameraView = findViewById<View>(R.id.camera_view) as OndoTextureView

                p1.x1.center = 0f
                p1.x1.max1 = 0f
                // 실화상

//        val matrix = Matrix()
//        matrix.setScale(0.5f, 0.5f)
//        mWebCamSurface!!.setTransform(matrix)
                // 열화상
                mUVCCameraView!!.setAspectRatio((4f / 3f).toDouble())
                mCameraHandler = UVCCameraHandler.createHandler(this, mUVCCameraView as UVCCameraTextureView,
                        0, 256, 196, 0, null, 0)

                States.mCameraHandler = mCameraHandler
                // 3.698
                // 1420, 1080
                //mUVCCameraView!!.
                // Wifi 테스트 할때는 여기서 열음
                if (TEST_MODE_NOCAM) {
                    mWebCamSurface!!.iniTempBitmap(1920, 1080)
                }

                if (mWebCam != null) {
                    mWebCam!!.destroy()
                    mWebCam = null
                }

                touchPointList!!.clear()
                p1.touchPointList = touchPointList

                //if (TEST_MODE_NOCAM == false) {
                // 온도캠이면 웹캠 열지 않음... States.
                //if (p1.camMode != Consts.CAM_ONDO) {
                States.webCamInitCounter = States.webCamInitCounter + 1
                Log.e(TAG, "webCam CountA ${States.webCamInitCounter}")
                if (States.webCamInitCounter == 1) {

                    Log.e(TAG, "change param  ${States.screenWidth} x ${States.screenHeight} ")
                    if (mWidth > mHeight) {
                        val params = mWebCamSurface!!.layoutParams
                        params.height = mHeight.toInt()
                        mWebCamSurface!!.layoutParams = params
                        mWebCamSurface!!.requestLayout()

                    } else {
                        val params = mWebCamSurface!!.layoutParams
                        params.height = 820
                        mWebCamSurface!!.layoutParams = params
                        mWebCamSurface!!.requestLayout()
                    }

                    mWebCamSurface!!.setAspectRatio((mWidth / mHeight).toDouble())

                    Handler().postDelayed({
                        Log.e(TAG, "webCam Count Run ${States.webCamInitCounter}")
                        openWebCam()
                        mUVCCameraView!!.visibility = View.GONE
                        States.webCamInitCounter = 0
                    }, 380)

                }
                //}

                if (p1.camMode != Consts.CAM_WEBCAM) {
                    isLoadingOndo = true
                    // 온도캠이면 녹음 x..
                    States.ondoCamInitCounter = States.ondoCamInitCounter + 1
                    Log.e(TAG, "ondoCam CountA ${States.ondoCamInitCounter}")

                    if (States.ondoCamInitCounter == 1) {
                        Log.e(TAG, "ondoCam Count Run ${States.ondoCamInitCounter}")
                        Handler().postDelayed({
                            openTempCam()
                            mUVCCameraView!!.visibility = View.VISIBLE
                            States.ondoCamInitCounter = 0
                        }, 3200)
                    }
                }

                if (p1.camMode == Consts.CAM_ONDO) {
                    btn_record_audio.layoutParams.height = 2;
                    btn_record_audio.visibility = View.INVISIBLE
                } else {
                    btn_record_audio.layoutParams.height = 120;
                    btn_record_audio.visibility = View.VISIBLE

                }


                //}

                alarmCheckTimer = Timer()
                alarmCheckTimer.schedule(object : TimerTask() {
                    override fun run() {
                        runOnUiThread {
                            if (cam_img_alarm != null) {
                                if (p1.isPlayingAlarm) {
                                    if (cam_img_alarm.visibility != View.VISIBLE) {
                                        cam_img_alarm.visibility = View.VISIBLE
                                    }
                                } else {
                                    if (cam_img_alarm.visibility != View.GONE) {
                                        cam_img_alarm.visibility = View.GONE
                                    }
                                }

                            }
                        }
                    }
                }, 5000, 500)

                setOndoExtUI()

            }, 100)

        }

        States.isViewResumePass = false

    }

    private fun setOndoExtUI() {
        if (p1.camMode != Consts.CAM_WEBCAM) {
            ondoExtImage.visibility = View.VISIBLE
            ondo_calib.visibility = View.VISIBLE
            ondoSpanImage.visibility = View.VISIBLE

            if (Cfg.ondo_extMode) {
                ondoExtImage.setImageResource(R.mipmap.range_600);
            } else {
                ondoExtImage.setImageResource(R.mipmap.range_120);
            }

        } else {
            ondoExtImage.visibility = View.GONE
            ondo_calib.visibility = View.GONE
            ondoSpanImage.visibility = View.GONE
        }
    }


    private fun getTempPara() {
        val tempPara: ByteArray
        tempPara = mCameraHandler!!.getTemperaturePara(128)
        Log.e(TAG, "getByteArrayTemperaturePara:" + tempPara[16] + "," + tempPara[17] + "," + tempPara[18] + "," + tempPara[19] + "," + tempPara[20] + "," + tempPara[21])
        Fix = ByteUtil.getFloat(tempPara, 0)
        Refltmp = ByteUtil.getFloat(tempPara, 4)
        Airtmp = ByteUtil.getFloat(tempPara, 8)
        humi = ByteUtil.getFloat(tempPara, 12)
        emiss = ByteUtil.getFloat(tempPara, 16)
        distance = ByteUtil.getShort(tempPara, 20).toInt()
        //        distance = tempPara[20];
        Log.e(TAG, "getByteArrayTemperaturePara:$distance")
        stFix = Fix.toString()
        stRefltmp = Refltmp.toString()
        stAirtmp = Airtmp.toString()
        stHumi = humi.toString()
        stEmiss = emiss.toString()
        stDistance = distance.toString()


        correction_text.setText(stFix + "°C")
        correction_seekbar.setProgress((Fix * 10.0f + 30).toInt())

        reflection_text.setText(stRefltmp + "°C")
        reflection_seekbar.setProgress((Refltmp + 10.0f).toInt())

        amb_temp_text.setText(stAirtmp + "°C")
        amb_temp_seekbar.setProgress((Airtmp + 10.0f).toInt())

        humidity_text.setText(stHumi)
        humidity_seekbar.setProgress((humi * 100.0f).toInt())

        emissivity_text.setText(stEmiss)
        emissivity_seekbar.setProgress((emiss * 100.0f).toInt())

        distance_text.setText(stDistance)
        distance_seekbar.setProgress(distance.toInt())
        
    }

    /***********************
     * 버튼 설정 초기화
     * **/
    private fun initButtons() {

        correction_seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val correction = (progress - 30) / 10.0f
                val correctionString = correction.toString()
                correction_text.setText("$correctionString°C")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val currentProgressCo = seekBar.progress
                val fiputCo = (currentProgressCo - 30) / 10.0f
                val iputCo = ByteArray(4)
                ByteUtil.putFloat(iputCo, fiputCo, 0)
                mSendCommand.sendFloatCommand(0 * 4, iputCo[0], iputCo[1], iputCo[2], iputCo[3], 20, 40, 60, 80, 120)
            }
        })


        reflection_seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val correction = (progress - 30) / 10.0f
                stRefltmp = correction.toString()
                reflection_text.setText(stRefltmp + "°C")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val currentProgressRe = seekBar.progress
                val fiputRe = currentProgressRe - 10.0f
                val iputRe = ByteArray(4)
                ByteUtil.putFloat(iputRe, fiputRe, 0)
                mSendCommand.sendFloatCommand(1 * 4, iputRe[0], iputRe[1], iputRe[2], iputRe[3], 20, 40, 60, 80, 120)
            }
        })



        amb_temp_seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val correction = (progress) / 10.0f
                stAirtmp = correction.toString()
                amb_temp_text.setText(stAirtmp + "°C")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val currentProgressAm = seekBar.progress
                val fiputAm = currentProgressAm - 10.0f
                val iputAm = ByteArray(4)
                ByteUtil.putFloat(iputAm, fiputAm, 0)
                mSendCommand.sendFloatCommand(2 * 4, iputAm[0], iputAm[1], iputAm[2], iputAm[3], 20, 40, 60, 80, 120)
            }
        })

        humidity_seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val correction = (progress) / 100.0f
                stHumi = correction.toString()
                humidity_text.setText(stHumi)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val currentProgressHu = seekBar.progress
                val fiputHu = currentProgressHu / 100.0f
                val iputHu = ByteArray(4)
                ByteUtil.putFloat(iputHu, fiputHu, 0)
                mSendCommand.sendFloatCommand(3 * 4, iputHu[0], iputHu[1], iputHu[2], iputHu[3], 20, 40, 60, 80, 120)
            }
        })

        emissivity_seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val correction = (progress) / 100.0f
                stEmiss = correction.toString()
                emissivity_text.setText(stEmiss)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val currentProgressEm = seekBar.progress
                val fiputEm = currentProgressEm / 100.0f
                val iputEm = ByteArray(4)
                ByteUtil.putFloat(iputEm, fiputEm, 0)
                mSendCommand.sendFloatCommand(4 * 4, iputEm[0], iputEm[1], iputEm[2], iputEm[3], 20, 40, 60, 80, 120)
            }
        })

        distance_seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val correction = (progress) / 10.0f
                stDistance = correction.toString()
                distance_text.setText(stDistance)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val currentProgressDi = seekBar.progress
                val fiputDistance = currentProgressDi / 10.0f
                val bIputDi = ByteArray(4)
                ByteUtil.putFloat(bIputDi, fiputDistance, 0)
                mSendCommand.sendFloatCommand(5 * 4, bIputDi[0], bIputDi[1], bIputDi[2], bIputDi[3], 20, 40, 60, 80, 120)
            }
        })




        /***********************
         * 메인화면 환경설정 버튼클릭
        **/

        save_button.setOnClickListener{
            setValue(UVCCamera.CTRL_ZOOM_ABS, 0x80ff)
            val msg = getResources().getString(R.string.saving_wait)
            Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
        }

        // 오른쪽 카메라 설정 메뉴...
        ondo_calib.setOnClickListener{

            Log.d(TAG, "${rightmenu_list.visibility}")
            if (rightmenu_list.visibility == View.VISIBLE) {
                rightmenu_list.visibility = View.GONE
            } else {
                rightmenu_list.visibility = View.VISIBLE
            }
        }


        // 온도 캘리브레이션...
        shut_button.setOnClickListener {

            Handler().postDelayed({
                if (mCameraHandler != null) {
                    mCameraHandler!!.whenShutRefresh()
                    Handler().postDelayed({
                        setValue(UVCCamera.CTRL_ZOOM_ABS, 0x8000)
                        rightmenu_list.visibility = View.GONE
                    }, 100)
                }

            }, 500)
        }


        btn_cam_record.setOnClickListener {
            //if (p1.camMode == Consts.CAM_ONDO) {
                // mUVCCameraView
                //if (mCameraHandler != null) {
                    //if (!mCameraHandler!!.isRecording) {
                    if (!isRecording) {
                        //mCameraHandler!!.startRecording()
                        Log.d(TAG, "Start Recording~~~ 1")
                        Log.d(TAG, "Start Recording~~~ 2")
                        Log.d(TAG, "Start Recording~~~ 3")
                        startActivityForResult(mediaProjectionManager!!.createScreenCaptureIntent(), Consts.REQUEST_REC)

                    } else {
                        mediaProjection.stop()
                        isRecording=false
                        States.isRecording=false
                        val msg = getResources().getString(R.string.Recording_complete)
                        val toast = Toast.makeText(mContext, msg, Toast.LENGTH_LONG)
                        toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200)
                        toast.show()

                        btn_cam_record.setImageResource(R.drawable.camv_btn_movie_record)
                        //  mCameraHandler!!.stopRecording();
                        Log.d(TAG, "Stop Recording~~~ 1")
                        Log.d(TAG, "Stop Recording~~~ 2")
                        Log.d(TAG, "Stop Recording~~~ 3")
                    }

                //}

            //} else {
                // mWebCamSurface!!
            //}


        }

//        btn_cam_record.setOnClickListener {

//            if (!mCameraHandler!!.isRecording()) {
//                btn_cam_record.setImageResource(R.drawable.camv_btn_pause)
//
//                if (mAudioRecord != null) {
//                    mAudioRecord!!.release()
//                    mAudioRecord = null
//                }
//
//               //mWebCam!!.startCapture( mWebCamSurface)
//            } else {
//                btn_cam_record.setImageResource(R.drawable.camv_btn_movie_record)
//
//                //mWebCamSurface!!.stopRecording()
//            }

//            if (isAudioRunning == true) {
//                isAudioRunning = false
//                btn_cam_record.setImageResource(R.drawable.camv_btn_movie_record)
//
//
//                if (mAudioRecord != null) {
//                    mAudioRecord!!.release()
//                    mAudioRecord = null
//                }
//            } else {
//                isAudioRunning = true
//                btn_cam_record.setImageResource(R.drawable.camv_btn_pause)
//                if (mAudioRecord == null) {
//                    //mBufferSize = AudioTrack.getMinBufferSize(mSampleRate, 1, mAudioFormat)
//                    Log.d("bobopro", "Audio Input Size ${mBufferSize}")
//                    mBufferSize = 1024
//                    mAudioRecord = AudioRecord(mAudioSource, mSampleRate, mChannelCount, mAudioFormat, mBufferSize)
//                    Log.d("bobopro", "Audio  ${mAudioRecord!!.getState()}")
//                    mAudioRecord!!.startRecording()
//                }
//                val thread=AudioPlayThread()
//                thread.start()
//            }

//        }


        // 온도 모드 변경
        ondoExtImage.setOnClickListener {
            if (Cfg.ondo_extMode) {
                Cfg.ondo_extMode=false
            } else {
                Cfg.ondo_extMode = true
            }
            Cfg.save_cam2Ondo(this)
            setOndoMode()

        }

        // 스팬 모드 변경
        ondoSpanImage.setOnClickListener {
            if (Cfg.ondo_spanMode) {
                Cfg.ondo_spanMode = false
            } else {
                Cfg.ondo_spanMode = true
            }
        }


        // 갤러리 가기
        btn_cam_gallery.setOnClickListener {
            var targetDir = initFolder(Consts.SCREEN_SHOT_FOLDER)!!.path
            var targetUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

            targetUri = targetUri.buildUpon().appendQueryParameter(
                    "bucketId",
                    targetDir.toLowerCase().hashCode().toString()
            ).build();
            var intent = Intent(Intent.ACTION_VIEW, targetUri);
            startActivity(intent);
            finish()
        }


        btn_cam_diag.setOnClickListener {
            var intent = Intent(this, DiagActivity::class.java)
            startActivity(intent)
        }



        btn_cam_setting.setOnClickListener {

            if (isLoadingOndo) {
                val msg = getResources().getString(R.string.The_thermal_imaging_camera_is_preparing);
                val toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200)
                toast.show()
            } else {
                cam_menu_holder.visibility = View.VISIBLE

                if (p1.camMode== Consts.CAM_ONDO) {
                    /** 온도 모드 설정메뉴 **/
                    set_cam2_fragment = SetCam2Fragment()
                    set_cam2_fragment.setContext(this)

                    set_cam2_fragment.setCloseListener() {
                        cam_menu_holder.visibility = View.GONE

                        supportFragmentManager.beginTransaction()
                                .remove(set_cam2_fragment)
                                .commit()
                    }

                    set_cam2_fragment.setPaletteItemClickListener {
                        if (Cfg.cam2_colorMode == Consts.PaletteRainbow) {
                            mCameraHandler!!.changePalette(2)
                        }
                        if (Cfg.cam2_colorMode == Consts.PaletteAmber) {
                            mCameraHandler!!.changePalette(3)
                        }
                        if (Cfg.cam2_colorMode == Consts.PaletteWhite) {
                            mCameraHandler!!.changePalette(0)
                        }
                    }

                    set_cam2_fragment.setDistanceListener() {
                        //
                        val bIputDi = ByteArray(4)
                        ByteUtil.putInt(bIputDi, Cfg.p1_dist.toInt(), 0)
                        sendShortCommand(5 * 4, bIputDi[0], bIputDi[1], 20, 40, 60)
                    }

                    set_cam2_fragment.setOndoModeListener() {
                        //
                        setOndoMode()

                    }

                    supportFragmentManager.beginTransaction()
                            .replace(R.id.cam_menu_holder, set_cam2_fragment)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commit()

                } else if (p1.camMode== Consts.CAM_MIX) {
                    /** 다화상 설정메뉴  **/

                    Handler().postDelayed({
                        setValue(UVCCamera.CTRL_ZOOM_ABS, 0x8000);
                    }, 500)

                    set_cam3_fragment = SetCam3Fragment()
                    set_cam3_fragment.setContext(this)

                    set_cam3_fragment.setCloseListener() {
                        cam_menu_holder.visibility = View.GONE
                        supportFragmentManager.beginTransaction()
                                .remove(set_cam3_fragment)
                                .commit()
                    }

                    /** 온도 표출 기준온도 설정 **/
                    set_cam3_fragment.setOndoSelectListener() {
                        cam_menu_holder.visibility = View.GONE
                        mix_ondoSetPanel.visibility = View.VISIBLE

                        arrange_Save_button.visibility = View.VISIBLE
                        arrange_cancel_button.visibility = View.VISIBLE

                        check_seekBar.progress = (Cfg.cam3_checkOndo * 10f).toInt()+200
                        oppa_seekBar.progress = (Cfg.cam3_mixOppa).toInt()

                        p1.imsiCheckOndo = Cfg.cam3_checkOndo
                        p1.imsiMixOppa = Cfg.cam3_mixOppa
                        //p1.imsiCheckMinOndo = Cfg.cam3_checkMinOndo
                        //p1.imsiCheckMaxOndo = Cfg.cam3_checkMaxOndo

                        val lo1 = getResources().getString(R.string.Lowest_TEMP_to_overlay)
                        val opa1 = getResources().getString(R.string.Opacity)
                        check_Label.text = String.format( lo1+" : %.1f°", check_seekBar.progress.toFloat() / 10f - 20f) + Cfg.p1_cGiho
                        oppa_Label.text = String.format(opa1+" : %.0f", oppa_seekBar.progress.toFloat()) + "%"

                        p1.ondoSelectMode = true
                        supportFragmentManager.beginTransaction()
                                .remove(set_cam3_fragment)
                                .commit()
                    }

                    /** 오버레이 설정 ( 줌, 위치 ) **/
                    set_cam3_fragment.setArrangeListener() {
                        Log.i(TAG, "Arrange ")
                        cam_menu_holder.visibility = View.GONE

                        mix_arrangeSetPanel.visibility = View.VISIBLE
                        arrange_Save_button.visibility = View.VISIBLE
                        arrange_cancel_button.visibility = View.VISIBLE
                        mPositionX = Cfg.cam3_xOrg
                        mPositionY = Cfg.cam3_yOrg
                        hRatio_Label.text = String.format("%.0f", p1.hRatio * 100)+"%"
                        vRatio_Label.text = String.format("%.0f", p1.vRatio * 100)+"%"
                        p1.arrangeMode = true
                        supportFragmentManager.beginTransaction()
                                .remove(set_cam3_fragment)
                                .commit()
                    }


                    set_cam3_fragment.setDistanceListener() {
                        //
                        val bIputDi = ByteArray(4)
                        ByteUtil.putInt(bIputDi, Cfg.p1_dist.toInt(), 0)
                        sendShortCommand(5 * 4, bIputDi[0], bIputDi[1], 20, 40, 60)
                    }

                    set_cam3_fragment.setDistanceListener() {
                        //
                        val bIputDi = ByteArray(4)
                        ByteUtil.putInt(bIputDi, Cfg.p1_dist.toInt(), 0)
                        sendShortCommand(5 * 4, bIputDi[0], bIputDi[1], 20, 40, 60)
                    }

                    set_cam3_fragment.setOndoModeListener() {
                        //
                        setOndoMode()

                    }

                    supportFragmentManager.beginTransaction()
                            .replace(R.id.cam_menu_holder, set_cam3_fragment)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commit()

                } else if (p1.camMode== Consts.CAM_WEBCAM) {
                    /** 온도 모드 설정메뉴 **/
                    set_cam1_fragment = SetCam1Fragment()
                    set_cam1_fragment.setContext(this)

                    set_cam1_fragment.setCloseListener() {
                        cam_menu_holder.visibility = View.GONE

                        supportFragmentManager.beginTransaction()
                                .remove(set_cam1_fragment)
                                .commit()
                    }

                    supportFragmentManager.beginTransaction()
                            .replace(R.id.cam_menu_holder, set_cam1_fragment)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commit()

                }



            }


        }



        vDec_button.setOnClickListener {
            p1.vRatio -= 0.01f
            vRatio_Label.text = String.format("%.0f", p1.vRatio * 100)+"%"
        }

        vAdd_button.setOnClickListener {
            p1.vRatio += 0.01f
            vRatio_Label.text = String.format("%.0f", p1.vRatio * 100)+"%"
        }

        hDec_button.setOnClickListener {
            p1.hRatio -= 0.01f
            hRatio_Label.text = String.format("%.0f", p1.hRatio * 100)+"%"
        }

        hAdd_button.setOnClickListener {
            p1.hRatio += 0.01f
            hRatio_Label.text = String.format("%.0f", p1.hRatio * 100)+"%"
        }


        check_seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                p1.imsiCheckOndo = seekBar.progress.toFloat() / 10f - 20f
                val lo1 = getResources().getString(R.string.Lowest_TEMP_to_overlay)
                check_Label.text = String.format(lo1 + " : %.1f°", p1.imsiCheckOndo) + Cfg.p1_cGiho
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                p1.imsiCheckOndo = seekBar.progress.toFloat() / 10f - 20f
                val lo1 = getResources().getString(R.string.Lowest_TEMP_to_overlay)
                check_Label.text = String.format(lo1 + " : %.1f°", p1.imsiCheckOndo) + Cfg.p1_cGiho
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                p1.imsiCheckOndo = seekBar.progress.toFloat() / 10f - 20f
                val lo1 = getResources().getString(R.string.Lowest_TEMP_to_overlay)
                check_Label.text = String.format(lo1 + " : %.1f°", p1.imsiCheckOndo) + Cfg.p1_cGiho
            }
        })


        oppa_seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val opa1 = getResources().getString(R.string.Opacity)
                p1.imsiMixOppa = seekBar.progress.toFloat()
                oppa_Label.text = String.format(opa1 + " : %.0f", p1.imsiMixOppa) + "%"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                val opa1 = getResources().getString(R.string.Opacity)
                p1.imsiMixOppa = seekBar.progress.toFloat()
                oppa_Label.text = String.format(opa1 + " : %.0f", p1.imsiMixOppa) + "%"
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val opa1 = getResources().getString(R.string.Opacity)
                p1.imsiMixOppa = seekBar.progress.toFloat()
                oppa_Label.text = String.format(opa1 + " : %.0f", p1.imsiMixOppa) + "%"
            }
        })


        arrange_Save_button.setOnClickListener {
            if (p1.arrangeMode) {
                Cfg.cam3_xOrg = p1.xOrg
                Cfg.cam3_yOrg = p1.yOrg
                Cfg.cam3_scale = p1.scaleFactor
                Cfg.cam3_vRatio = p1.vRatio
                Cfg.cam3_hRatio = p1.hRatio
                Cfg.save_cam3Ondo(this)
                p1.arrangeMode = false
            }

            if (p1.ondoSelectMode) {
                Cfg.cam3_checkOndo = check_seekBar.progress.toFloat() / 10f - 20f
                Cfg.cam3_mixOppa = oppa_seekBar.progress.toFloat()
                Cfg.save_cam3Ondo(this)
                p1.ondoSelectMode = false
            }
            arrange_Save_button.visibility = View.GONE
            arrange_cancel_button.visibility = View.GONE
            mix_ondoSetPanel.visibility = View.GONE
            mix_arrangeSetPanel.visibility = View.GONE
        }

        arrange_cancel_button.setOnClickListener {
            cancelUserMode()

        }


        test_button.setOnClickListener {
            val matrix = Matrix()
            matrix.setScale(1.5f, 1.5f)
            matrix.setTranslate(-1000f, -200f)
            mWebCamSurface!!.setTransform(matrix)
            //matrix.postRotate(-viewFinderRotation.toFloat(), centerX, centerY)
        }

        btn_record_audio.setOnClickListener {
            val msg = getResources().getString(R.string.Do_you_want_to_start_recoring)
            val yes = getResources().getString(R.string.Yes)
            val no = getResources().getString(R.string.No)

            val dialog = CommonDialog(this, "", msg, yes, no)
            dialog.setOkListener() {
                p1.p1Model.startRecording(p1.camMode == Consts.CAM_MIX)
                showProgressDialog(View.VISIBLE)
            }
            dialog.show()
        }


        /** 화상 캡쳐  **/
        savePng_button.setOnClickListener {

            if (checkPermissionWriteExternalStorage()) {
                //val outputFile = MediaMuxerWrapper.getCaptureFile(Environment.DIRECTORY_DCIM, ".png").toString()
                p1.playShutter(this);
                val imgFolder = initFolder(Consts.SCREEN_SHOT_FOLDER)
                val captureImageName = imgFolder!!.absolutePath + "/" + getScreenFileName()+".png"

                Log.d(TAG, "Save bitmap : $captureImageName")

                var bitmap: Bitmap? = null

                var recXcenter = p1.scrXcenter
                val recOndo = p1.scrOndo
                val recHumi = p1.scrHumi
                var recTouch = FloatArray(24, {0f})

                if (recXcenter == Float.NaN) {
                    recXcenter = 0f
                }

                var touchCount = 0
                touchPointList.forEach {
                    recTouch[touchCount] = it.ondo
                    touchCount++
                }

                if (p1.camMode == Consts.CAM_ONDO) {
                    bitmap = mUVCCameraView!!.getBitmap()
                } else {
                    bitmap = mWebCamSurface!!.getBitmap()
                }



                // val bitmap: Bitmap = mWebCamSurface!!.captureStillImage()
                val os = BufferedOutputStream(FileOutputStream(captureImageName))
                try {
                    try {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 85, os)
                        val buffer = ByteArray(4)
                        for( i in 0 until touchCount) {
                            ByteUtil.putFloat(buffer, recTouch[i], 0)
                            os.write(buffer)
                        }
                        // ondo list size -32
                        ByteUtil.putInt(buffer, touchCount, 0)
                        os.write(buffer)

                        ByteUtil.putFloat(buffer, recXcenter, 0)
                        os.write(buffer)

                        // 임시 Ver  -24
                        ByteUtil.putFloat(buffer, 0.01f, 0)
                        os.write(buffer)

                        // lati Ver  -20
                        ByteUtil.putFloat(buffer, p1.fLati, 0)
                        os.write(buffer)
                        // longi Ver  -16
                        ByteUtil.putFloat(buffer, p1.fLongi, 0)
                        os.write(buffer)
                        // p1 측정습도 Ver  -12
                        ByteUtil.putFloat(buffer, recOndo, 0)
                        os.write(buffer)
                        // p1 측정온도 Ver  -8
                        ByteUtil.putFloat(buffer, recHumi, 0)
                        os.write(buffer)
                        // image Info Ver -4
                        ByteUtil.putInt(buffer, 65538, 0)
                        os.write(buffer)
                        os.flush()

                        Log.d(TAG, "Save bitmap Capture ${p1.fLati}, ${p1.fLongi}")
                        val msg = getResources().getString(R.string.ScreenShot_Saved)
                        val toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT)
                        toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200)
                        toast.show()
                        this.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(File(captureImageName))))
                    } catch (e: IOException) {
                    }
                } finally {
                    os.close()
                }

            }

        }

    }

    private fun setOndoMode() {

//
//                        mCameraHandler.setShutterFix(1.2f);
        if (mCameraHandler != null) {
            if (Cfg.ondo_extMode) {
                mCameraHandler!!.setTempRange(400);
                mCameraHandler!!.setShutterFix(1.2f);
            } else {
                mCameraHandler!!.setTempRange(120);
                mCameraHandler!!.setShutterFix(1.5f);
            }
        }

        Handler().postDelayed({
            if (Cfg.ondo_extMode) {
                setValue(UVCCamera.CTRL_ZOOM_ABS, 0x8021)
            } else {
                setValue(UVCCamera.CTRL_ZOOM_ABS, 0x8020)
            }
        }, 200)

        Handler().postDelayed({
            setValue(UVCCamera.CTRL_ZOOM_ABS, 0x8000);
        }, 700)


        Handler().postDelayed({
            if (mCameraHandler != null) {
                mCameraHandler!!.whenShutRefresh()
                Handler().postDelayed({
                    setValue(UVCCamera.CTRL_ZOOM_ABS, 0x8000)
                }, 100)

            }
        }, 1200)


        Handler().postDelayed({
            if (mCameraHandler != null) {
                mCameraHandler!!.whenShutRefresh()
                Handler().postDelayed({
                    setValue(UVCCamera.CTRL_ZOOM_ABS, 0x8000)
                }, 100)

            }
        }, 4950)

        setOndoExtUI()

    }

    private fun cancelUserMode() {
        p1.xOrg = Cfg.cam3_xOrg
        p1.yOrg = Cfg.cam3_yOrg
        p1.scaleFactor = Cfg.cam3_scale

        p1.vRatio = Cfg.cam3_vRatio
        p1.hRatio = Cfg.cam3_hRatio

        p1.arrangeMode = false
        arrange_Save_button.visibility = View.GONE
        arrange_cancel_button.visibility = View.GONE

        p1.ondoSelectMode = false
        mix_ondoSetPanel.visibility = View.GONE
        mix_arrangeSetPanel.visibility = View.GONE
    }


    ///////////////////////////////
    // 실화상 오픈
    fun openWebCam() {
            Log.i(TAG, "openWebCam ============= ")
            val camera = WebCam()
            camera.open(States.webCamCtrlBlock)
            //if (DEBUG) Log.i(TAG, "bobopro supportedSize:" + camera.getSupportedSize());
            try {
                Log.i(TAG, "Try preview")
                //camera.setPreviewSize(640, 480, UVCCamera.FRAME_FORMAT_MJPEG)
                camera.setPreviewSize(1280, 720, UVCCamera.FRAME_FORMAT_MJPEG)
                //camera.setPreviewSize(1920, 1080, UVCCamera.FRAME_FORMAT_MJPEG)
                mWebCamSurface = findViewById<View>(R.id.webcam_view) as WebCamTextureView
                if (TEST_MODE_NOCAM == false) {
                    Log.i(TAG, "iniTempBitmap")
                    mWebCamSurface!!.iniTempBitmap(1920, 1080)
                }
                //mWebCamSurface!!.iniTempBitmap(1920, 1080)
                val st = mWebCamSurface!!.getSurfaceTexture()
                if (st != null) {
                    Log.i(TAG, "bobopro mPreviewSurface")
                    isActive = true
                    if (p1.camMode != Consts.CAM_ONDO) {
                        camera.setPreviewDisplay(Surface(st))
                        camera.startPreview()
                    }
                    isPreview = true
                    Log.i(TAG, "openWebCam Done ==================")
                } else {
                    Log.e(TAG, "bobopro SurfaceTexture null")
                }
                mWebCam = camera

            } catch (e: IllegalArgumentException) {
                Log.i(TAG, "Open Web Cam Fail")
                camera.destroy()
            }
    }



    // 열화상 오픈
    fun openTempCam() {

        // Toast.makeText(MainActivity.this, "Xtherm onConnect", Toast.LENGTH_SHORT).show();
        if (mCameraHandler!= null) {
            mCameraHandler!!.open(States.tempCamCtrlBlock)

            mUVCCameraView!!.iniTempBitmap(1920, 1080)
            Log.i(TAG, "startPreview: getSurfaceTexture")
            val st = mUVCCameraView!!.surfaceTexture
            mCameraHandler!!.startPreview(Surface(st))
            Log.i(TAG, "startPreview: getSurfaceTexture2")

            //isPreviewing = true
            //palette = sharedPreferences.getInt("palette", 0)
            //UnitTemperature = sharedPreferences.getInt("UnitTemperature", 1)


            Handler().postDelayed({
                setValue(UVCCamera.CTRL_ZOOM_ABS, 0x8004)
            }, 150)

            Handler().postDelayed({
                if (Cfg.ondo_extMode) {
                    setValue(UVCCamera.CTRL_ZOOM_ABS, 0x8021)  // 0~600
                } else {
                    setValue(UVCCamera.CTRL_ZOOM_ABS, 0x8020) // -20~120
                }
            }, 400)


            Handler().postDelayed({
                if (mCameraHandler != null) {
                    mCameraHandler!!.whenShutRefresh()

                    Handler().postDelayed({
                        setValue(UVCCamera.CTRL_ZOOM_ABS, 0x8000)
                    }, 100)

                }
            }, 1500)


            Handler().postDelayed({
                if (mCameraHandler != null) {
                    isLoadingOndo = false
                    if (!mCameraHandler!!.isTemperaturing()) {
                        //mUVCCameraView!!.setTemperatureAnalysisMode(0)
                        mCameraHandler!!.startTemperaturing()
                    }
                }

            }, 900)



            if (p1.camMode == Consts.CAM_ONDO) {
                Handler().postDelayed({
                    if (mCameraHandler != null) {
                        if (Cfg.cam2_colorMode == Consts.PaletteRainbow) {
                            mCameraHandler!!.changePalette(2)
                        }
                        if (Cfg.cam2_colorMode == Consts.PaletteAmber) {
                            mCameraHandler!!.changePalette(3)
                        }
                        if (Cfg.cam2_colorMode == Consts.PaletteWhite) {
                            mCameraHandler!!.changePalette(0)
                        }
                        isLoadingOndo = false
                    }
                    p1.tempCamReady = true
                }, 2400)

            }


            Handler().postDelayed({
                if (mCameraHandler != null) {
                    if (Cfg.ondo_extMode) {
                        mCameraHandler!!.setTempRange(400);
                        mCameraHandler!!.setShutterFix(1.2f);
                    } else {
                        mCameraHandler!!.setTempRange(120);
                        mCameraHandler!!.setShutterFix(1.5f);
                    }
                    p1.tempCamReady = true
                }
            }, 2900)

            Handler().postDelayed({
                if (mCameraHandler != null) {
                    getTempPara()
                }
            }, 3100)

        } else {
            isLoadingOndo = false
            Log.d(TAG, " ondo cam mCameraHandler is null ")
        }

    }


    // 핀치 줌..  스케일 제스쳐 처리...
    private val scaleListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {

            if (p1.arrangeMode) {
//                var scaleFactor = p1.scaleFactor * detector.scaleFactor
//                scaleFactor = Math.max(0.8f, Math.min(scaleFactor, 2.5f))
//
//                var fDiff = scaleFactor - p1.scaleFactor
//                p1.scaleFactor = scaleFactor
                Log.v(TAG, "====== ==== ${detector.scaleFactor}")

                p1.vRatio *= detector.scaleFactor
                p1.hRatio *= detector.scaleFactor

                p1.vRatio = Math.max(0.8f, Math.min(p1.vRatio, 2.5f))
                p1.hRatio = Math.max(0.8f, Math.min(p1.hRatio, 2.5f))

                vRatio_Label.text = String.format("%.0f", p1.vRatio * 100)+"%"
                hRatio_Label.text = String.format("%.0f", p1.hRatio * 100)+"%"
                //p1.scaleFactor *= detector.scaleFactor
                //p1.scaleFactor = Math.max(0.8f, Math.min(p1.scaleFactor, 2.5f))
            }
            return true
        }
    }



    @SuppressLint("ClickableViewAccessibility")
    private fun initTouchView() {
        touchPointList = CopyOnWriteArrayList()

        var mLastTouchX = 0f
        var mLastTouchY = 0f

        val Invalid_pointer_id = -1
        var mActivePointerId = Invalid_pointer_id

        mWebCamSurface!!.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {

                if ((p1.camMode == Consts.CAM_MIX) and (p1.arrangeMode)) {
                    mScaleDetector.onTouchEvent(event)
                    when (event?.action) {
                        MotionEvent.ACTION_DOWN -> {
                            val x = event.x
                            val y = event.y
                            mLastTouchX = x
                            mLastTouchY = y
                            mActivePointerId = event.getPointerId(0)
                        }
                        MotionEvent.ACTION_MOVE -> {
                            var pointerIndex = event.findPointerIndex(mActivePointerId)
                            val x = event.getX(pointerIndex)
                            val y = event.getY(pointerIndex)
                            if (!mScaleDetector.isInProgress()) {
                                var distX = x - mLastTouchX
                                var distY = y - mLastTouchY
                                mPositionX += distX
                                mPositionY += distY
                                if (p1.arrangeMode) {
                                    p1.xOrg = mPositionX
                                    p1.yOrg = mPositionY
                                }
                            }
                            mLastTouchX = x
                            mLastTouchY = y
                        }
                        MotionEvent.ACTION_UP -> {
                            mActivePointerId = Invalid_pointer_id
                        }
                        MotionEvent.ACTION_CANCEL -> {
                            mActivePointerId = Invalid_pointer_id
                        }
                        MotionEvent.ACTION_POINTER_UP -> {
                            val pointerIndex = (event?.action and MotionEvent.ACTION_POINTER_INDEX_MASK) shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
                            val pointerId = event.getPointerId(mActivePointerId)
                            if (pointerId == mActivePointerId) {
                                var newPointerIndex = 0
                                if (pointerIndex == 0) {
                                    newPointerIndex = 1
                                }
                                mLastTouchX = event.getX(newPointerIndex)
                                mLastTouchY = event.getY(newPointerIndex)
                                mActivePointerId = event.getPointerId(newPointerIndex)
                            }
                        }
                    }
                } // endif cam_mix and arrange Mode
                else if ((p1.camMode == Consts.CAM_ONDO) or (p1.camMode == Consts.CAM_MIX)) {
                    // 선택영역 온도 포인터

                    if (isLoadingOndo == false) {
                        when (event?.action) {
                            MotionEvent.ACTION_UP -> {
                                val curPoint = TouchPoint()

                                if ((event.y < 920) && (p1.arrangeMode == false) && (p1.ondoSelectMode == false)) {
                                    if (mWidth > mHeight) {
                                        curPoint.x = event.x / 2000
                                        curPoint.y = event.y / 1080
                                    } else {
                                        curPoint.x = event.x / 1200
                                        curPoint.y = event.y / 800
                                    }

                                    curPoint.ondo = 0f
                                    if (touchPointList.size < 1) {
                                        touchPointList.add(curPoint)
                                    } else {
                                        var isRemoved = false
                                        for (i in 0 until touchPointList.size) {
                                            val chkPoint = touchPointList[i]
                                            //Log.d(TAG, "터치 ${curPoint.x}x${curPoint.y} => ${chkPoint.x}x${chkPoint.y}  ")
                                            if ((curPoint.x > chkPoint.x - 98f / 2000f) and (curPoint.x < chkPoint.x + 132f / 2000f)) {
                                                if ((curPoint.y > chkPoint.y - 98f / 2000f) and (curPoint.y < chkPoint.y + 132f / 2000f)) {
                                                    touchPointList.remove(chkPoint)
                                                    isRemoved = true
                                                    break
                                                }
                                            }
                                        }
                                        if (!isRemoved) {
                                            touchPointList.add(curPoint)
                                        }
                                    }
                                    //Log.d(TAG, "터치 카운트 ${touchPointList.size}")
                                    p1.touchPointList = touchPointList

                                } //endif < 800

                            }
                        }

                    }
                }

                // 더블 클릭 처리
                if (!p1.arrangeMode) {
                    if (event?.action == MotionEvent.ACTION_UP) {
                        val now = System.currentTimeMillis()
                        val result = now - sufaceClickTime
                        // Log.d("bobopro", "aaa pan click ${result}")
                        if (result < 350) {
                            if (p1.camType == Consts.MODE_CAM_NOR) {
                                p1.camType = Consts.MODE_CAM_EXT
                            } else {
                                p1.camType = Consts.MODE_CAM_NOR
                            }
                        } else {
                            sufaceClickTime = System.currentTimeMillis()
                        }
                    }

                }

                return true
            }
        })

    }


    // 열화상 커맨드...
    private fun setValue(flag: Int, value: Int): Int {
        return if (mCameraHandler != null) mCameraHandler!!.setValue(flag, value) else 0
    }


    fun sendShortCommand(position: Int, value0: Byte, value1: Byte, interval0: Int, interval1: Int, interval2: Int) {

        if (mCameraHandler != null) {
            val psitionAndValue0 = position shl 8 or (0x000000ff and value0.toInt())
            val handler0 = Handler()
            handler0.postDelayed({
                if (mCameraHandler != null) {
                    mCameraHandler!!.setValue(UVCCamera.CTRL_ZOOM_ABS, psitionAndValue0)
                }
            }, interval0.toLong())
            val psitionAndValue1 = position + 1 shl 8 or (0x000000ff and value1.toInt())
            handler0.postDelayed({
                if (mCameraHandler != null) {
                    mCameraHandler!!.setValue(UVCCamera.CTRL_ZOOM_ABS, psitionAndValue1)
                }
            }, interval1.toLong())
            handler0.postDelayed({
                if (mCameraHandler != null) {
                    mCameraHandler!!.whenShutRefresh()
                }
            }, interval2.toLong())
        }
    }


    override fun onPause() {
        super.onPause()
        Log.v(TAG, "onPause")
        Log.v(TAG, "onPause ============== 1")
        Log.v(TAG, "onPause ============== 2")
        Log.v(TAG, "onPause ============== 3")
        Log.v(TAG, "onPause ============== 4")
        Log.v(TAG, "onPause ============== 5")


        alarmCheckTimer.cancel()

    }

    override fun onStop() {
        super.onStop()
        Log.v(TAG, "onStop")
        Log.v(TAG, "onStop ============== 1")
        Log.v(TAG, "onStop ============== 2")
        Log.v(TAG, "onStop ============== 3")
        Log.v(TAG, "onStop ============== 4")
        Log.v(TAG, "onStop ============== 5")




        alarmCheckTimer.cancel()

//        if (mWebCam != null) mWebCam!!.close()
//
//        //        mSensorManager.unregisterListener(mSensorListener, mSensorMagnetic);
////        mSensorManager.unregisterListener(mSensorListener, mAccelerometer);
//        //System.exit(0);
        //if (mUVCCameraView != null) mUVCCameraView!!.onPause()

        if (mWebCam != null) {
            mWebCam!!.destroy()
            mWebCam = null
        }
        // mWebCamSurface = null

//        if (mWebCamSurface != null) {
//            mWebCamSurface = null
//        }
        if (mCameraHandler != null)
        {
            mCameraHandler!!.close()
            mCameraHandler!!.release()
            mCameraHandler = null
        }

//
//
//        //setCameraButton(false);

    }


    override fun onStart() {
        super.onStart()
        Log.v(TAG, "onStart")
        Log.v(TAG, "onStart ============== 1")
        Log.v(TAG, "onStart ============== 2")
        Log.v(TAG, "onStart ============== 3")
        Log.v(TAG, "onStart ============== 4")
        Log.v(TAG, "onStart ============== 5")


        mWebCamSurface = findViewById<View>(R.id.webcam_view) as WebCamTextureView
    }


//    override fun onRestart() {
//        super.onRestart()
//        Log.v(TAG, "onReStart")
//        if (mUVCCameraView != null) {
//            mUVCCameraView!!.onResume()
//        }
//
//    }

    override fun onDestroy() {
        super.onDestroy()
        States.mCameraHandler = null
        Log.v(TAG, "onDestroy")

//        synchronized(mSync) {
//            isPreview = false
//            isActive = isPreview
//            if (mWebCam != null) {
//                mWebCam!!.destroy()
//                mWebCam = null
//            }
//            //mWebCamSurface = null
//
//            if (mCameraHandler != null) {
//                mCameraHandler!!.close()
//                mCameraHandler!!.release()
//                mCameraHandler = null
//            }
//        }
    }

    // 종료
    override fun onBackPressed() {
        if (rightmenu_list.visibility == View.VISIBLE) {
            rightmenu_list.visibility = View.GONE
        } else if (progress_rec.visibility == View.VISIBLE) {
            val msg = getResources().getString(R.string.The_camera_is_preparing)
            val toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200)
            toast.show()
        } else if (isLoadingOndo) {
            val msg = getResources().getString(R.string.The_thermal_imaging_camera_is_preparing)
            val toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200)
            toast.show()
            Handler().postDelayed({
                isLoadingOndo = false
            }, 2500)


        } else if ( (p1.arrangeMode) || (p1.ondoSelectMode) ) {
            cancelUserMode()
        } else if (cam_menu_holder.visibility == View.VISIBLE){

            cam_menu_holder.visibility = View.GONE
            if (p1.camMode== Consts.CAM_ONDO) {

                supportFragmentManager.beginTransaction()
                        .remove(set_cam2_fragment)
                        .commit()
            } else if (p1.camMode== Consts.CAM_MIX) {
                supportFragmentManager.beginTransaction()
                        .remove(set_cam3_fragment)
                        .commit()
            } else if (p1.camMode== Consts.CAM_WEBCAM) {
                supportFragmentManager.beginTransaction()
                        .remove(set_cam1_fragment)
                        .commit()
            }
        } else {
            super.onBackPressed()
        }
    }



    /**
     * 오디오 레코딩 테스트 프로그래스 UI 보이기/숨기기
     */
    private fun showProgressDialog(isShow: Int) {

        if (isShow == View.VISIBLE) {
            progress_rec.visibility = isShow;
            progressTimer = Timer()
            var count = 0
            progressTimer.schedule(object : TimerTask() {
                override fun run() {
                    count++
                    runOnUiThread {
                        var cnt = count * 10
                        if ((cnt > 120) || (!p1.isRecording)) {
                            showProgressDialog(View.GONE)
                        } else {
                            if (cnt > 100) {
                                cnt = 100
                            }
                            progress_rec.progress = cnt
                        }
                    }
                }
            }, 1100, 1150)
        } else {
            progressTimer.cancel()
            runOnUiThread {
                progress_rec.visibility = isShow;
                progress_rec.progress = 0
                val msg = getResources().getString(R.string.Recording_complete)
                val toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200)
                toast.show()
            }
        }
    }




        ///////////////////////////////////////////////////////////////
    // Override Func  캡쳐..
    ///////////////////////////////////////////////////////////////
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.i("bobopro", "onActivityResult")

        if (requestCode == Consts.REQUEST_REC) {
            // 녹화
            if (resultCode != Activity.RESULT_OK) {
                // 사용자가 권한을 허락하지 않음
                val msg = getResources().getString(R.string.Check_Record_Permission)
                val toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200)
                toast.show()
                // 화면 클리어 되면서 다시 resume타지 않게...
                States.isViewResumePass = true
                return
            }


            States.isRecording = true

            Log.i("bobopro", "Start Recording 1")
            btn_cam_record.setImageResource(R.drawable.camv_btn_pause)

            Handler().postDelayed({
                //playRecordingAnim(RECORDING)
                Log.i("bobopro", "Start Recording 2")
                mediaProjection = mediaProjectionManager!!.getMediaProjection(resultCode, data)
                Log.i("bobopro", "Start Recording 3")
                screenRecorder()
            }, 280)// create virtual display depending on device width / height

        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    /**
     * 화면 동영상 녹화
     */
    private fun screenRecorder() {

//        val decorView = window.decorView
//        val uiOption = decorView.getSystemUiVisibility() or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//        decorView.setSystemUiVisibility(uiOption)

        Log.i("bobopro-screenRecorder", "1")
        prepareRecord = true
        val screenRecorder : MediaRecorder = createRecorder()
        Log.i("bobopro-screenRecorder", "2")
        isRecording = true
        val callback = object : MediaProjection.Callback() {
            override fun onStop() {
                super.onStop()
                if (screenRecorder != null) {
                    runOnUiThread {
                        screenRecorder.stop()
                        screenRecorder.reset()
                        screenRecorder.release()

                    }
                }

                mediaProjection.unregisterCallback(this)
            }
        }

        mediaProjection.registerCallback(callback, null)

        Log.i("bobopro-screenRecorder", "3")
        mediaProjection.createVirtualDisplay(
                "cvd",
                1920,
                1080,
                States.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                screenRecorder.surface,
                null,
                null
        )

        Log.i("bobopro-screenRecorder", "4")
        Handler().postDelayed({
            Log.i("bobopro-screenRecorder", "6")
            screenRecorder.start()
        }, 1350)
        Log.i("bobopro-screenRecorder", "5")

        var count = -2
        videoTimer = Timer()
        videoTimer!!.schedule(object : TimerTask() {
            override fun run() {

                Log.i("bobopro-screenRecorder", "Timer")
                runOnUiThread {
                    if (count >= 0) {
                        //txt_rec_time.text = String.format("00:%02d", count)
                    }
                }

                if (isRecording == false) {
                    this.cancel()
                } else {
                    if (count >= Cfg.p1_recTimeMovie) {
                        // 설정되어 있는 시간 이후
                        States.isRecording = false
                        mediaProjection.stop()

                        runOnUiThread {
                            val msg = getResources().getString(R.string.Recording_complete)
                            val toast = Toast.makeText(mContext, msg, Toast.LENGTH_LONG)
                            toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200)
                            toast.show()
                            btn_cam_record.setImageResource(R.drawable.camv_btn_movie_record)
                        }
                        //playRecordingAnim(RECORDED)
                        this.cancel()
                    }
                }


                count++
            }
        }, 1000, 1000)
    }



    /**
     * Create Recorder
     */
    private fun createRecorder() : MediaRecorder{
        val mediaRecorder = MediaRecorder()


        videoFolder = initFolder(Consts.REC_FOLDER)
        recVideoName = videoFolder!!.absolutePath + "/" + getFileNameForTime() + ".mp4"

        mediaRecorder.setOutputFile(recVideoName)
        //val displayMetrics :DisplayMetrics = Resources.getSystem().displayMetrics
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P))
        mediaRecorder.setVideoSize(1920, 1080)
//        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
//        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
//        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
//        mediaRecorder.setVideoEncodingBitRate(512 * 1000)
//        mediaRecorder.setVideoFrameRate(30)
        try {
            mediaRecorder.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return mediaRecorder
    }


    class SendCommand {
        var psitionAndValue0 = 0
        var psitionAndValue1 = 0
        var psitionAndValue2 = 0
        var psitionAndValue3 = 0
        fun sendFloatCommand(position: Int, value0: Byte, value1: Byte, value2: Byte, value3: Byte, interval0: Int, interval1: Int, interval2: Int, interval3: Int, interval4: Int) {
            if (States.mCameraHandler != null) {
                psitionAndValue0 = position shl 8 or (0x000000ff and value0.toInt())
                val handler0 = Handler()
                handler0.postDelayed({ States.mCameraHandler!!.setValue(UVCCamera.CTRL_ZOOM_ABS, psitionAndValue0) }, interval0.toLong())
                psitionAndValue1 = position + 1 shl 8 or (0x000000ff and value1.toInt())
                handler0.postDelayed({ States.mCameraHandler!!.setValue(UVCCamera.CTRL_ZOOM_ABS, psitionAndValue1) }, interval1.toLong())
                psitionAndValue2 = position + 2 shl 8 or (0x000000ff and value2.toInt())
                handler0.postDelayed({ States.mCameraHandler!!.setValue(UVCCamera.CTRL_ZOOM_ABS, psitionAndValue2) }, interval2.toLong())
                psitionAndValue3 = position + 3 shl 8 or (0x000000ff and value3.toInt())
                handler0.postDelayed({ States.mCameraHandler!!.setValue(UVCCamera.CTRL_ZOOM_ABS, psitionAndValue3) }, interval3.toLong())
                handler0.postDelayed({ States.mCameraHandler!!.whenShutRefresh() }, interval4.toLong())
            }
        }

        private fun whenChangeTempPara() {
            if (States.mCameraHandler != null) {
                States.mCameraHandler!!.whenChangeTempPara()
            }
        }

        fun sendByteCommand(position: Int, value0: Byte, interval0: Int) {
            if (States.mCameraHandler != null) {
                psitionAndValue0 = position shl 8 or (0x000000ff and value0.toInt())
                val handler0 = Handler()
                handler0.postDelayed({ States.mCameraHandler!!.setValue(UVCCamera.CTRL_ZOOM_ABS, psitionAndValue0) }, interval0.toLong())
                handler0.postDelayed({ States.mCameraHandler!!.whenShutRefresh() }, (interval0 + 20).toLong())
            }
        }
    }

}