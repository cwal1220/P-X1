package com.inspeco.X1

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.hardware.usb.UsbDevice
import android.location.LocationManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.net.Uri.*
import android.net.wifi.WifiManager
import android.os.*
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import com.inspeco.X1.CamView.LoadingWebActivity
import com.inspeco.X1.GraphView.GraphFullActivity
import com.inspeco.X1.HomeView.P1Model
import com.inspeco.X1.HomeView.ShareActivity
import com.inspeco.X1.ReportView.ReportActivity
import com.inspeco.X1.SettingView.SettingActivity
import com.inspeco.X1.StatusJudgView.DiagActivity
import com.inspeco.data.*
import com.inspeco.dialog.CommonDialog
import com.inspeco.extensions.AutoClearedDisposable
import com.inspeco.wave.WaveformView
import com.serenegiant.usb.USBMonitor
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.menu_drawer.*
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.timer


class HomeActivity : AppCompatActivity() {

    private var mUSBMonitor: USBMonitor? = null
    private val TAG = "bobopro-Home"
    private var mWebCamMemo: TextView? = null
    private var mTempCamMemo: TextView? = null
    //private var waveSyncTimer: Timer? = null

    private var lastWaveCircle = -1
    private var isScreenCapture = false
    private val locationManager by lazy {
        getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    private var isThreadRunning=false
    private var waveform_view: WaveformView? = null
    protected var mPlayThread: Thread? = null

    private lateinit var p1 : P1
    private lateinit var x1 : X1
    private lateinit var camBitmap: CamBitmap
    private lateinit var drawListener : DrawerLayout.DrawerListener
    private var uiTimer: Timer? = null
    // 녹음 중 progress 를 위한 타이머
    private var progressTimer = Timer()

    private var vibrator:Vibrator? = null
    private var alarm: Uri? = null
    private var alarmPlayer :MediaPlayer? = null


    // rxjava , ui 이벤트 구독
    private val viewDisposable = AutoClearedDisposable(lifecycleOwner = this, alwaysClearOnStop = false)
    // rxjava, 이벤트 구독 ( api 등 )
    internal val disposable = AutoClearedDisposable(lifecycleOwner = this, alwaysClearOnStop = true)

    private lateinit var p1Model: P1Model

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)
        
        // 만료일자를 설정. 최종 릴리즈에서 제거 요망
//        val targetDate = "2025-01-31"
//        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//
//        try {
//            val target = dateFormat.parse(targetDate) ?: Date()
//            val currentDate = Date()
//
//            if (currentDate.after(target)) {
//                // 현재 날짜가 지정한 날짜를 지난 경우
//                showExpiredDialog()
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
        // 만료일자를 설정 끝

        Log.v(TAG, "ON CREATE")
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        mWebCamMemo = findViewById<TextView>(R.id.text_webCamInfo)
        mTempCamMemo = findViewById<TextView>(R.id.text_tempCamInfo)

        lifecycle.addObserver(viewDisposable)
        lifecycle.addObserver(disposable)
        mUSBMonitor = USBMonitor(this, mOnDeviceConnectListener)

        camBitmap = CamBitmap.getInstance()
        camBitmap.loadBitmap(this)

        /** 터치 리스터 */
        initTouchListener()

        Cfg.load_camOndo(this)
        Cfg.load_p1(this)

        initP1()
        x1 = X1.getInstance()


        // 알람 관련 변수
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        alarm = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        alarmPlayer = MediaPlayer()
        alarmPlayer!!.setDataSource(this, alarm)


        val filter = IntentFilter()
        // filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)            // 와이파이 상태
        filter.addAction(WifiManager.RSSI_CHANGED_ACTION)                            // 안테나 감도 변경
        registerReceiver(p1Model.wifiReceiver, filter)

        drawListener = object : DrawerLayout.DrawerListener {
            override fun onDrawerClosed(view: View){
                if (isScreenCapture) {
                    isScreenCapture = false
                    screenCapture()
                }
            }
            override fun onDrawerOpened(drawerView: View){
                //Toast.makeText(this@HomeActivity,"Opened",Toast.LENGTH_LONG).show()
            }
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            }
            override fun onDrawerStateChanged(newState: Int) {
            }
        }
        homeView.addDrawerListener(drawListener)
        isThreadRunning = true
//        val thread=AudioThread()
//        thread.priority = Thread.MAX_PRIORITY
//        thread.start()

        //registWaveTimer()

        Ini.checkIniFile(this)
    }

    private fun showExpiredDialog() {
        AlertDialog.Builder(this)
            .setTitle("알림")
            .setMessage("앱 사용 기간이 만료되었습니다. \n(만료일: 2025-01-31)\n개발자에게 문의하십시오.")
            .setCancelable(false) // 사용자가 닫을 수 없도록 설정
            .setPositiveButton("확인") { _, _ ->
                finish() // 앱 종료
            }
            .show()
    }

    inner class AudioThread:Thread(){
        override fun run(){
            while(isThreadRunning){
                //
                if (!p1.isMute) {
                    val time2 = System.currentTimeMillis();
                    val iDiff = time2-p1.lastWaveTime
                    if (iDiff<70) {
                        //val a1 = System.currentTimeMillis();
                        System.arraycopy(p1.waveAudio, 0, p1.waveAudio2, 0, 1024)
                        //p1Model.audioPlayer?.write(p1.waveAudio2, 0, 1024)
                        SystemClock.sleep(33)
                        //val a2 = System.currentTimeMillis();
                        //Log.e(TAG, "iDiff ${a2-a1}");

                    } else {
                        if (iDiff>260) {
                            //Log.e(TAG, "iDiff ${iDiff}");
                            SystemClock.sleep(5)
                        }
                    }
                }
                SystemClock.sleep(1)
                //runOnUiThread(UIClass())
            }
        }
    }
    inner class UIClass:Runnable{
        override fun run(){
            //textview.text=System.currentTimeMillis().toString()
        }
    }
//
//    private fun registWaveTimer() {
//
//        if (waveSyncTimer==null) {
//
//
////            mPlayThread = new Thread(new Runnable() {
////                @Override
////                public void run() {
////
////
////                }
////            }
//
////            waveSyncTimer = Timer()
////            waveSyncTimer!!.schedule(object : TimerTask() {
////                override fun run() {
////                    try {
//////                        if (sendWave != null) {
//////                            sendControlData(sendWave)
//////                        }
////                        if (!p1.isMute) {
////                            val time2 = System.currentTimeMillis();
////                            val iDiff = time2-p1.lastWaveTime
////                            if (iDiff<64) {
////                                System.arraycopy(p1.waveAudio, 0, p1.waveAudio2,0,1024)
////                                p1Model.audioPlayer?.write(p1.waveAudio2, 0, 1024)
////                            } else {
////                                Log.e(TAG, "iDiff ${iDiff}");
////
////                            }
////                        }
////
////                    } catch (e: InterruptedException) {
////                        Log.e(TAG, "error");
////                        e.printStackTrace()
////                    }
////                }
////
////            }, 0, 33)
//        }
//
//
//    }


    private fun screenCapture() {
        p1.playShutter(this)
        var fos: FileOutputStream? = null
        var bitmap: Bitmap? = null
        var canvas: Canvas? = null
        val view = homeView
        val w = view!!.width
        val h = view!!.height
        var imgFolder:File? = initFolder(Consts.SCREEN_SHOT_FOLDER)
        var captureImageName = imgFolder!!.absolutePath + "/" + Consts.PREFIX_S + getFileName(
                Consts.SCREEN_SHOT_FOLDER, null
        ) + ".png"

        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(bitmap)
        view.draw(canvas)

        fos = FileOutputStream(captureImageName)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)

        if (fos != null) {
            try {
                fos.close()
            } catch (ioe: IOException) {
                ioe.printStackTrace()
            }
        }
        bitmap.recycle()

        runOnUiThread({
            screenShotLayer.visibility = View.VISIBLE
            screenShotLayer.alpha = 1f
        })

        Handler().postDelayed({
            screenShotLayer.apply {
                alpha = 1f
                visibility = View.VISIBLE
                animate()
                        .alpha(0f)
                        .setDuration(500L)
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                screenShotLayer.visibility = View.GONE
                            }
                        })
            }
        }, 100)

        Handler().postDelayed({
            val msg = getResources().getString(R.string.ScreenShot_Saved)
            Toast.makeText(this@HomeActivity, msg, Toast.LENGTH_LONG).show()
        }, 250)

        this.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, fromFile(File(captureImageName))))

    }



    /**
     * 프로그래스 UI 보이기/숨기기
     */
    private fun showProgressDialog(isShow: Int) {
        layout_progress.visibility = isShow
        //txt_file_name.text = p1Model.dBFileName
        txt_file_name.text = ""

        if (isShow == View.VISIBLE) {
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
                            val msg = getResources().getString(R.string.recording_progress)
                            txt_progress_desc.text = String.format(msg, cnt)
                        }
                    }
                }
            }, 1100, 1150)
        } else {
            progressTimer.cancel()
            runOnUiThread {
                progress_rec.progress = 0
                txt_progress_desc.text = String.format(" %d%%", 0)
            }
        }
    }



    /** 터치 리스터 */
    private fun initTouchListener() {
        menuButton.setOnClickListener {
            homeView.openDrawer(drawerView)
        }

        img_alarm.setOnClickListener {
            clearAlarm()
        }

        buttonCameraMix.setOnClickListener {
            openCameraMix()
        }
        button_cameraMix.setOnClickListener {
            openCameraMix()
        }


        buttonCameraReal.setOnClickListener {
            openCameraReal()
        }
        button_cameraReal.setOnClickListener {
            openCameraReal()
        }

        buttonCameraOndo.setOnClickListener {
            openCameraOndo()
        }
        button_cameraOndo.setOnClickListener {
            openCameraOndo()
        }

        buttonScreenCapture.setOnClickListener {
            isScreenCapture = true
            homeView.closeDrawer(drawerView)

        }

        buttonRecordWave.setOnClickListener{
            val msg = getResources().getString(R.string.Do_you_want_to_start_recoring)
            val yes = getResources().getString(R.string.Yes)
            val no = getResources().getString(R.string.No)

            val dialog = CommonDialog(this, "", msg, yes, no)
            dialog.setOkListener() {
                p1Model.startRecording(false)
                homeView.closeDrawer(drawerView)
                showProgressDialog(View.VISIBLE)
            }
            dialog.show()
        }

        buttonGallery.setOnClickListener {

            // 갤러리 가기
            var targetDir = initFolder(Consts.SCREEN_SHOT_FOLDER)!!.path
            var targetUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

            targetUri = targetUri.buildUpon().appendQueryParameter(
                    "bucketId",
                    targetDir.toLowerCase().hashCode().toString()
            ).build();
            var intent = Intent(Intent.ACTION_VIEW, targetUri);
            startActivity(intent);
        }

        buttonFolder.setOnClickListener {
            val intent = packageManager.getLaunchIntentForPackage("com.sec.android.app.myfiles")
            startActivity(intent)
        }

        buttonMute.setOnClickListener {

            p1.isMute = !p1.isMute

            if (p1.isMute) {
                val msg = getResources().getString(R.string.muted)
                Toast.makeText(this@HomeActivity, msg, Toast.LENGTH_LONG).show()
                buttonMute.setImageResource(R.drawable.homev_menu_muteoff)

            } else {
                val msg = getResources().getString(R.string.dismuted)
                Toast.makeText(this@HomeActivity, msg, Toast.LENGTH_LONG).show()
                buttonMute.setImageResource(R.drawable.homev_menu_mute)
            }
        }

        buttonDiag.setOnClickListener {
            var intent = Intent(this, DiagActivity::class.java)
            startActivity(intent)
        }

        buttonReport.setOnClickListener {
            var intent = Intent(this, ReportActivity::class.java)
            startActivity(intent)
        }

        button_camera4.setOnClickListener {
            var intent = Intent(this, ReportActivity::class.java)
            startActivity(intent)
        }

        buttonSetting.setOnClickListener {
            var intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }

        buttonShare.setOnClickListener {
            var intent = Intent(this, ShareActivity::class.java)
            startActivity(intent)
        }


        waveformView.setOnClickListener {
            var intent = Intent(this, GraphFullActivity::class.java)
            startActivity(intent)
        }

        @Throws(IOException::class)
        fun copyFile(`in`: InputStream, out: OutputStream) {
            val buffer = ByteArray(1024)
            var read: Int
            while (`in`.read(buffer).also { read = it } != -1) {
                out.write(buffer, 0, read)
            }
        }

        buttonPdf.setOnClickListener {
            var lang = Locale.getDefault().language
            var fileName = ""
            var appName = getString(R.string.app_name)  // r.string.app_name value 참
            if (appName == "P1-X1" ) {
                fileName = "X1_Eng_Ver 3.9_20210715.pdf"
            } else {
                if (lang == "ko") {
                    //fileName = appName+"-rev 1.8_k.pdf"
                }else if (lang == "zh") {
                    //fileName = appName+"-rev_1.2_c.pdf"
                }else {
                    //fileName = appName+"-rev 1.8_e.pdf"
                }
            }
            fileName = "X1_Eng_Ver 3.9_20210715.pdf"

            val assetManager = assets

            var in_: InputStream? = null
            var out: OutputStream? = null
            val folder = File(Environment.getExternalStorageDirectory().absolutePath + "/" + Consts.ROOT_FOLDER + "/")
            val file = File(folder, fileName)

            try {
                in_ = assetManager.open(fileName)
                out = FileOutputStream(file)

                copyFile(in_, out)
                in_!!.close()
                in_ = null
                out!!.flush()
                out!!.close()
                out = null
            } catch (e: Exception) {
                Log.d("bobopro", "bobopro file Open fail :" + e.message);
            }

            /*val intent = Intent(Intent.ACTION_VIEW)
              intent.setDataAndType(Uri.parse(file.absolutePath), "application/pdf")
              startActivity(Intent.createChooser(intent, "Open PDF"))*/

            var uri = FileProvider.getUriForFile(this, "com.inspeco.X1", file);

            val i = Intent(Intent.ACTION_VIEW)
//            i.setType("image/*")
//        i.type = "application/pdf"
            i.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            i.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this, "com.inspeco.X1", file))
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                i.setDataAndType(uri, "application/pdf")
            } else {
                i.setDataAndType(Uri.fromFile(file), "application/pdf")
            }
            val chooser = Intent.createChooser(i, "Open PDF")
            startActivity(chooser)

        }

    }

    private fun clearAlarm() {
        p1.alarmCnt = 0
        vibrator!!.cancel()
        alarmPlayer!!.stop()
        if (States.curView == Consts.VIEW_HOME) {
            runOnUiThread {
                img_alarm.visibility = View.GONE
            }
        }
        p1.isPlayingAlarm = false
    }



    /**
     * 알람 체크
     */
    private fun checkAlarm() {

        var level = (p1.db / 100).toInt()
        // 알람 표시 중인 경우에는 표시 안함
        if (!p1.isPlayingAlarm) {
            // Log.d(TAG, "${p1.alarmCnt} / ${Cfg.p1_alram_checksec*22}")
            if (Cfg.p1_alram_set == true) {

                if (level >= Cfg.p1_alram_db) {

                    var isAlarm = false
                    if (Cfg.p1_alram_checksec==0) {
                        isAlarm = true
                    } else if (p1.alarmCnt >=  Cfg.p1_alram_checksec * 22) {
                        isAlarm = true
                    }

                    // 1초에 31개 2~5개 누락 빼고 체크
                    if (isAlarm) {
                        p1.isPlayingAlarm = true
                        if (Cfg.p1_alram_vibe == true) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator!!.vibrate(
                                        VibrationEffect.createWaveform(
                                                longArrayOf(
                                                        500,
                                                        1000
                                                ), 0
                                        )
                                )
                            } else {
                                vibrator!!.vibrate((Cfg.p1_alram_sec * 1000).toLong())
                            }
                        }

                        if (Cfg.p1_alram_sound == true) {
                            try {
                                Log.d(TAG, "Sound Play")
                                alarmPlayer!!.isLooping = true
                                alarmPlayer!!.prepare()
                                alarmPlayer!!.start()
                            } catch (e: Exception) {
                                Log.d(TAG, "Sound Error")
                                e.printStackTrace()
                            }
                        }

                        if (States.curView == Consts.VIEW_HOME) {
                            if (Cfg.p1_alram_icon) {
                                runOnUiThread {
                                    img_alarm.visibility = View.VISIBLE
                                }
                            }
                        }

                        var count = 0
                        val timer = Timer()
                        timer.schedule(object : TimerTask() {
                            override fun run() {
                                count++
                                if (count >= Cfg.p1_alram_sec) {
                                    // 설정되어 있는 시간 이후
                                    clearAlarm()
                                }
                            }
                        }, 1000, 1000)
                    }
                } else {

                }
            }
        }
    }


    private fun openCameraOndo() {
        States.mainContext = this
        p1.tempCamReady = false
        States.isRecording = false


        if (States.webCamState == Consts.DEVICE_ATTATCHED) {
            mUSBMonitor!!.requestPermission(States.deviceWebCam)
            val msg = getResources().getString(R.string.The_camera_is_preparing)
            val toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200)
            toast.show()

        } else if (States.ondoCamState == Consts.DEVICE_ATTATCHED) {
            mUSBMonitor!!.requestPermission(States.deviceTempCam)
            val msg = getResources().getString(R.string.The_thermal_imaging_camera_is_preparing)
            val toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200)
            toast.show()
        } else if ((States.webCamState == Consts.DEVICE_CONNNECTED) || (States.ondoCamState == Consts.DEVICE_CONNNECTED)) {
            p1.camMode = Consts.CAM_ONDO
            States.webCamInitCounter = 0
            States.ondoCamInitCounter = 0
            var intent = Intent(States.mainContext, LoadingWebActivity::class.java)
            startActivity(intent)
        } else {
            // 장치 상태 확인...

            val msg = getResources().getString(R.string.Device_is_disconnected)
            val check = getResources().getString(R.string.Check)
            val dialog = CommonDialog(this, "X1", msg, check, null)
            dialog.show()
            Log.e(TAG, "Check device")
        }
    }

    private fun openCameraReal() {


        Log.d("bobopro", " freq ${p1Model.lastWave!!.wave.freq}")
        Log.d("bobopro", " sens ${p1Model.lastWave!!.wave.sens}")
        Log.d("bobopro", " mode ${p1Model.lastWave!!.wave.mode}")
        Log.d("bobopro", " vol ${p1Model.lastWave!!.wave.vol}")
        Log.d("bobopro", " lang ${p1Model.lastWave!!.wave.lang}")


        States.mainContext = this
        States.isRecording = false
        if (States.webCamState == Consts.DEVICE_ATTATCHED) {
            mUSBMonitor!!.requestPermission(States.deviceWebCam)
//        } else if (States.ondoCamState == Consts.DEVICE_ATTATCHED) {
//            mUSBMonitor!!.requestPermission(States.deviceTempCam)
        } else if ((States.webCamState == Consts.DEVICE_CONNNECTED) ) {
            p1.camMode = Consts.CAM_WEBCAM
            States.webCamInitCounter = 0
            States.ondoCamInitCounter = 0
            var intent = Intent(States.mainContext, LoadingWebActivity::class.java)
            startActivity(intent)
        } else {
            // 장치 상태 확인...
            val msg = getResources().getString(R.string.Device_is_disconnected)
            val check = getResources().getString(R.string.Check)
            val dialog = CommonDialog(this, "X1", msg, check, null)
            dialog.show()
            Log.e(TAG, "Check device")
        }
    }

    private fun openCameraMix() {
        States.mainContext = this
        p1.tempCamReady = false
        States.isRecording = false

        if (States.webCamState == Consts.DEVICE_ATTATCHED) {
            mUSBMonitor!!.requestPermission(States.deviceWebCam)
            val msg = getResources().getString(R.string.The_camera_is_preparing)
            val toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200)
            toast.show()
        } else if (States.ondoCamState == Consts.DEVICE_ATTATCHED) {
            mUSBMonitor!!.requestPermission(States.deviceTempCam)
            val msg = getResources().getString(R.string.The_thermal_imaging_camera_is_preparing)
            val toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200)
            toast.show()

        } else if ((States.webCamState == Consts.DEVICE_CONNNECTED) || (States.ondoCamState == Consts.DEVICE_CONNNECTED)) {
            p1.camMode = Consts.CAM_MIX
            States.webCamInitCounter = 0
            States.ondoCamInitCounter = 0
            var intent = Intent(States.mainContext, LoadingWebActivity::class.java)
            startActivity(intent)
        } else {
            // 장치 상태 확인...

            val msg = getResources().getString(R.string.Device_is_disconnected)
            val check = getResources().getString(R.string.Check)
            val dialog = CommonDialog(this, "X1", msg, check, null)
            dialog.show()
            Log.e(TAG, "Check device")
        }
    }


    /** P1  초기화  */
    private fun initP1() {
        p1 = P1.getInstance()

        p1Model = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())
                .get(P1Model::class.java)
        p1Model.initP1Model(this)
        subscribeP1Data() // p1 데이터 이벤트 구독
        p1Model.connectP1Socket()

        // 다른 뷰에서 오디오 녹음을 위채 참조 전달해 놓음.
        p1.p1Model = p1Model


        val displayMetrics = DisplayMetrics()
        val windowsManager = applicationContext.getSystemService(WINDOW_SERVICE) as WindowManager
        windowsManager.defaultDisplay.getMetrics(displayMetrics)
        p1.deviceWidth  = displayMetrics.widthPixels
        p1.deviceHeight  = displayMetrics.heightPixels

        Log.e(TAG, "${p1.deviceWidth}x${p1.deviceHeight}")

    }


    private fun updateP1UI() {

        if (States.curView == Consts.VIEW_HOME) {
            runOnUiThread {
                var dbLevel = p1.db / 200
                if (dbLevel < 0) dbLevel = -dbLevel
                if (dbLevel > 14) dbLevel = 15
//                home_levelImage.setImageDrawabl
//                //Log.d(TAG, "$dbLevel");

                if (lastWaveCircle != dbLevel) {
                    when (dbLevel) {
                        0 -> home_levelImage.setBackgroundResource(R.drawable.homev_lv00)
                        1 -> home_levelImage.setBackgroundResource(R.drawable.homev_lv01)
                        2 -> home_levelImage.setBackgroundResource(R.drawable.homev_lv02)
                        3 -> home_levelImage.setBackgroundResource(R.drawable.homev_lv03)
                        4 -> home_levelImage.setBackgroundResource(R.drawable.homev_lv04)
                        5 -> home_levelImage.setBackgroundResource(R.drawable.homev_lv05)
                        6 -> home_levelImage.setBackgroundResource(R.drawable.homev_lv06)
                        7 -> home_levelImage.setBackgroundResource(R.drawable.homev_lv07)
                        8 -> home_levelImage.setBackgroundResource(R.drawable.homev_lv08)
                        9 -> home_levelImage.setBackgroundResource(R.drawable.homev_lv09)
                        10 -> home_levelImage.setBackgroundResource(R.drawable.homev_lv10)
                        11 -> home_levelImage.setBackgroundResource(R.drawable.homev_lv11)
                        12 -> home_levelImage.setBackgroundResource(R.drawable.homev_lv12)
                        13 -> home_levelImage.setBackgroundResource(R.drawable.homev_lv13)
                        14 -> home_levelImage.setBackgroundResource(R.drawable.homev_lv14)
                        else -> home_levelImage.setBackgroundResource(R.drawable.homev_lv15)
                    }
                    lastWaveCircle = dbLevel
                }


                label_real_db!!.text = p1.db_str
                label_real_db2.text = "dB"
                avrdBText.text = String.format("%.1f", p1.avrDb)
                hv_ondoText.text = String.format("%.0f", p1.ondo) + "℃"
                hv_humiText.text = String.format("%.0f", p1.humi) + "%"
                checkAlarm()
            }
        } else {
            checkAlarm()
        }

    }


    /**
     * p1 데이터의 이벤트 구독
     */
    private fun subscribeP1Data() {


        viewDisposable.add(
                p1Model.mpP1WaveInfo
                        .subscribe {
                            //                    txt_volume.text = it.getString("vol")
                            when (p1.mode) {
                                0 -> { // real
                                    if (States.curView == Consts.VIEW_HOME) {
                                        runOnUiThread(Runnable {
                                            updateP1UI()
                                        })
                                    }
                                }
                            }
                        }

        )

        waveform_view = findViewById<WaveformView>(R.id.waveformView)

        viewDisposable.add(
                p1Model.mpWaveGraph.observeOn(Schedulers.io())
                        .subscribe {

                            waveform_view!!.dB = p1.db_f
                            waveform_view!!.samples = p1.waveSignal
                            //                            runOnUiThread(Runnable {
                            //                                updateP1UI()
                            //                            })

                        }
        )

    }



    override fun onStop() {
        super.onStop()

        if (mUSBMonitor!!.isRegistered) {
            mUSBMonitor!!.unregister()
        }

    }

    fun getProductName(device: UsbDevice) : String {
        var name = device.productName
        if (name == null) {
            name = "Full HD New"
        }
        return name
    }

    fun checkMyDevicePermission(device: UsbDevice) {
        var name = getProductName(device)

        if ( name.contains("Full") || name.contains("Camera") ) {
            mWebCamMemo!!.text = "Real Cam Attatched"
            States.deviceWebCam = device
            States.webCamState = Consts.DEVICE_ATTATCHED
            Handler().postDelayed({

                Log.w(TAG, "onAttach: ==== Full HD Proc =====>")

//                for (accessory in mUSBMonitor!!.mUsbManager.accessoryList) {
//                    Log.w(TAG, "Accessory ${accessory.toString()}")
////                        if (mUSBMonitor!!.mUsbManager.hasPermission(accessory)) {
////                            outStr += "FT312D has Permission\n"
////                            // OpenAccessory(accessory);
////                        } else {
////                            outStr += "FT312D No Permission\n"
////                        }
//                }


                mUSBMonitor!!.requestPermission(States.deviceWebCam)

//                if (!mUSBMonitor!!.hasPermission(device)) {
//                    Log.w(TAG, "Need User Permission")
//                    mWebCamMemo!!.text = "Need User Permission"
//                    //mUSBMonitor!!.requestPermission(device)
//                }
            }, 250)

        } else if ( name.contains("Xmodule") || name.contains("T3C") || name.contains("S0-6"))  {
            mTempCamMemo!!.text = "Temp Cam Attatched"
            States.deviceTempCam = device
            States.ondoCamState = Consts.DEVICE_ATTATCHED
            x1.isDeviceAttatched = true
            Handler().postDelayed({
                Log.w(TAG, "onAttach: ==== Xmodule Proc =====>")
                mUSBMonitor!!.requestPermission(States.deviceTempCam)

//                if (!mUSBMonitor!!.hasPermission(device)) {
//                    Log.w(TAG, "Need User Permission")
//                    mTempCamMemo!!.text = "Need User Permission"
//                    //mUSBMonitor!!.requestPermission(device)
//                }
            }, 2200)
        }
    }

    override fun onResume() {
        super.onResume()

        Cfg.load_camOndo(this)
        Cfg.load_p1(this)

        States.curView = Consts.VIEW_HOME

        p1.isPlayingAlarm = false
        p1.arrangeMode = false

        if (!mUSBMonitor!!.isRegistered) {
            mUSBMonitor!!.register()
        }

        Log.v(TAG, "Home onResume()")
        mWebCamMemo!!.text = "Check Real Cam"
        mTempCamMemo!!.text = "Check Temp Cam"

        p1Model.startPlayer()



//        val mUsbDeviceList = mUSBMonitor!!.deviceList
//        for (device in mUsbDeviceList) {
//            checkMyDevicePermission(device)
//        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        } else {
            locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1f, p1Model.locationListener)
        }

        if (p1.isMute) {
            buttonMute.setImageResource(R.drawable.homev_menu_muteoff)
        } else {
            buttonMute.setImageResource(R.drawable.homev_menu_mute)
        }

        uiTimer = timer(period = 1000) {

            runOnUiThread {
                if (p1.isDeviceAttatched) {
                    hv_deviceIcon.setImageResource(R.drawable.homev_icon_device)
                } else {
                    hv_deviceIcon.setImageResource(R.drawable.homev_icon_device0)
                }

                if (p1.isDeviceAttatched) {
                    when (p1.wifiLevel) {
                        5, 4 -> hv_wifiIcon.setImageResource(R.drawable.homev_icon_wifi3)
                        3, 2 -> hv_wifiIcon.setImageResource(R.drawable.homev_icon_wifi2)
                        else -> hv_wifiIcon.setImageResource(R.drawable.homev_icon_wifi1)
                    }
                    hv_ondoIcon.visibility = View.VISIBLE
                    hv_humiIcon.visibility = View.VISIBLE
                    hv_ondoText.visibility = View.VISIBLE
                    hv_humiText.visibility = View.VISIBLE
                } else {
                    hv_wifiIcon.setImageResource(R.drawable.homev_icon_wifi0)
                    hv_ondoIcon.visibility = View.GONE
                    hv_humiIcon.visibility = View.GONE
                    hv_ondoText.visibility = View.GONE
                    hv_humiText.visibility = View.GONE
                }

                if (p1.lati!="") {
                    hv_gpsText.text = p1.lati + "\n"+p1.longi
                } else {
                    hv_gpsText.text = "NO SIGNAL"
                }
            }
        }

    }


    override fun onPause() {
        super.onPause()
        States.curView = Consts.VIEW_NONE
        uiTimer?.cancel()

        clearAlarm()
        //p1Model.stopPlayer()
    }

    override fun onDestroy() {
        Log.v(TAG, "Home onDestory()")
        if (mUSBMonitor != null) {
            mUSBMonitor!!.destroy()
            mUSBMonitor = null
        }

        // 타이머 캔슬
//        if (waveSyncTimer != null) {
//            waveSyncTimer!!.cancel()
//        }
        isThreadRunning=false
        homeView.removeDrawerListener(drawListener)
        p1Model.closeP1Socket()
        //locationManager!!.removeUpdates(p1Model.locationListener)
        super.onDestroy()
    }


    override fun onBackPressed() {

        val msg = getResources().getString(R.string.close_app)
        val yes = getResources().getString(R.string.Yes)
        val no = getResources().getString(R.string.No)
        val dialog = CommonDialog(this, "", msg, yes, no)
        dialog.show()
        dialog.setOkListener() {
            super.onBackPressed()
        }

    }


/**
  USB 디바이스 연결
 */
    private val mOnDeviceConnectListener: USBMonitor.OnDeviceConnectListener = object : USBMonitor.OnDeviceConnectListener {
        override fun onAttach(device: UsbDevice) {
            var name = getProductName(device)
            val clsId = device.deviceClass;
            val clsSubId = device.deviceSubclass;
            Log.e(TAG, " aaa onAttach:$name,  $clsId, $clsSubId ")

            checkMyDevicePermission(device)

        }

        override fun onConnect(device: UsbDevice, ctrlBlock: USBMonitor.UsbControlBlock, createNew: Boolean) {
            var name = getProductName(device)
            Log.w(TAG, "onConnect: $name")
            if ( name.contains("Full") || name.contains("USB 2.0 Camera") ) {
                States.webCamState= Consts.DEVICE_CONNNECTED
                mWebCamMemo!!.text = "Real Cam Connected"
                States.webCamCtrlBlock = ctrlBlock
            } else if ( name.contains("Xmodule") || name.contains("T3C") || name.contains("S0-6"))  {
                States.ondoCamState= Consts.DEVICE_CONNNECTED
                mTempCamMemo!!.text = "Temp Cam Connected"
                States.tempCamCtrlBlock = ctrlBlock
            }
        }

        override fun onDisconnect(device: UsbDevice, ctrlBlock: USBMonitor.UsbControlBlock) {
            var name = getProductName(device)
            Log.w(TAG, "onDisconnect: $name")
            if ( name.contains("Full") || name.contains("USB 2.0 Camera") ) {
                States.webCamState= Consts.DEVICE_DISCONNECTED
                mWebCamMemo!!.text = "Real Cam Disconnected"
            } else if ( name.contains("Xmodule") || name.contains("T3C") || name.contains("S0-6"))  {

                States.ondoCamState= Consts.DEVICE_DISCONNECTED
                mTempCamMemo!!.text = "Temp Cam Disconnected"
            }
        }

        override fun onDettach(device: UsbDevice) {
            var name = getProductName(device)
            Log.e(TAG, "onDettach: $name")
            if ( name.contains("Full") || name.contains("USB 2.0 Camera") ) {
                States.webCamState= Consts.DEVICE_DETTATCHED
                mWebCamMemo!!.text = "Real Cam Dettached"
                States.deviceWebCam = null
            } else if ( name.contains("Xmodule") || name.contains("T3C") || name.contains("S0-6"))  {

                x1.isDeviceAttatched = false
                States.ondoCamState= Consts.DEVICE_DETTATCHED
                mTempCamMemo!!.text = "Temp Cam Dettached"
                States.deviceTempCam = null
            }
        }

        override fun onCancel(device: UsbDevice) {
            var name = getProductName(device)
            Log.e(TAG, "onCancel: $name")
            if ( name.contains("Full") || name.contains("USB 2.0 Camera") ) {
                States.webCamState= Consts.DEVICE_DISCONNECTED
                mWebCamMemo!!.text = "Real Cam Canceled"
            } else if ( name.contains("Xmodule") || name.contains("T3C") || name.contains("S0-6"))  {
                States.ondoCamState= Consts.DEVICE_DISCONNECTED
                mTempCamMemo!!.text = "Temp Cam Canceled"
            }
        }
    }


}