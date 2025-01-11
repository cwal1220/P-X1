package com.inspeco.X1.StatusJudgView

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.l_github.derlio.waveform.soundfile.SoundFile
import com.inspeco.X1.R
import com.inspeco.X1.XTerm.ByteUtil
import com.inspeco.data.*
import kotlinx.android.synthetic.main.activity_diag_result_wave.*
import java.io.File
import java.io.FileInputStream
import java.util.*


class ResultWaveActivity : AppCompatActivity() {
    private var minOndo = 255f
    private var maxOndo = 0f
    private var minIndex = 0
    private var maxIndex = 0
    private var ondoDiff = 0f
    private var lati = 0f
    private var longi = 0f

    private var udr:UDR? = null
    private var decorView: View? = null
    private var uiOption = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diag_result_wave)


        decorView = window.decorView
        uiOption = window.decorView.systemUiVisibility
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) uiOption = uiOption or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) uiOption = uiOption or View.SYSTEM_UI_FLAG_FULLSCREEN
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) uiOption = uiOption or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        decorView!!.setSystemUiVisibility(uiOption)

        finishButton.setOnClickListener { finish() }
        backImageButton.setOnClickListener { finish() }

        saveUdrButton.setOnClickListener {

            if (saveUdrFile(udr, States.diagMixResult.name, Consts.UDR_WAVE_FOLDER)) {
                val toast = Toast.makeText(this, "UDR File saved.", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200)
                toast.show()
            }
        }

    }

    override fun onResume() {
        super.onResume()
//        val rotate = RotateAnimation(0f, 180f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
//        rotate.duration = 5000
//        rotate.interpolator = LinearInterpolator()
//        meterPointer.startAnimation(rotate)
        readWaveInfo()

        //var index = 0
        Ini.checkIniFile(this)


        val x1 = 13.5f - States.diagDistance
        val x2 = 24.0f - States.diagWaveInfo.ondo
        val x3 = 50.0f - States.diagWaveInfo.humi
        var x4 = (-1.6160f * x1) + (0.1335f * x2) - (0.015f * x3) + States.diagWaveInfo.realDb
        val x5 = if (States.diagEquipment.value == 0f || States.diagMaterial.value == 0f) {
            0f
        } else {
            States.diagEquipment.value / States.diagMaterial.value
        }
        val x6 = States.diagFaulty.value
        var availableDB = -7.85144584978602f + (0.370997970192539f * x4) + (12.3702872138801f * x5) + (36.4301254010607f * x6)

        //availableDB += ( States.diagVolt * 0.01f )

        Log.d("bobopro wavediag", "readDb ${States.diagWaveInfo.realDb} x4 ${x4}  availDB ${availableDB}" )

        if (availableDB >= Ini.diagPLList[0].value) {
            States.diagPlResult = Ini.diagPLList[0].copy()
        } else if (availableDB >= Ini.diagPLList[1].value) {
            States.diagPlResult = Ini.diagPLList[1].copy()
        } else if (availableDB >= Ini.diagPLList[2].value) {
            States.diagPlResult = Ini.diagPLList[2].copy()
        } else if (availableDB >= Ini.diagPLList[3].value) {
            States.diagPlResult = Ini.diagPLList[3].copy()
        } else if (availableDB >= Ini.diagPLList[4].value) {
            States.diagPlResult = Ini.diagPLList[4].copy()
        } else if (availableDB >= Ini.diagPLList[5].value) {
            States.diagPlResult = Ini.diagPLList[5].copy()
        } else if (availableDB >= -30) {
            States.diagPlResult = Ini.diagPLList[5].copy()
        }


        val msgStr = String.format("wave  Pl %d", States.diagPlResult.id)
//            val toast = Toast.makeText(this,msgStr, Toast.LENGTH_LONG)
//            toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200)
//            toast.show()
        Log.d("bobopro wavediag", msgStr)

        dbLabel.text = stringFromFloatAuto(States.diagWaveInfo.realDb)+"dB"

        when (States.diagPlResult.id) {
            6 -> {
                meterPointer.animate().rotation(15f).setDuration(2000)
            }
            5 -> {
                meterPointer.animate().rotation(45f).setDuration(2000)
            }
            4 -> {
                meterPointer.animate().rotation(75f).setDuration(2000)
            }
            3 -> {
                meterPointer.animate().rotation(105f).setDuration(2000)
            }
            2 -> {
                meterPointer.animate().rotation(135f).setDuration(2000)
            }
            1 -> {
                meterPointer.animate().rotation(165f).setDuration(2000)
            }
        }

            when (States.diagPlResult.id) {
                5,6 -> {
                    msgLabel.setTextColor(Color.parseColor("#1c531b"))
                    resultPage.setBackgroundColor(Color.parseColor("#1c531b"))
                    faceImage.setImageResource(R.drawable.resultv_face1)
                }
                2,3,4 -> {
                    msgLabel.setTextColor(Color.parseColor("#154c13"))
                    resultPage.setBackgroundColor(Color.parseColor("#286323"))
                    faceImage.setImageResource(R.drawable.resultv_face2)
                }
                1 -> {
                    msgLabel.setTextColor(Color.parseColor("#843d0c"))
                    resultPage.setBackgroundColor(Color.parseColor("#944d1c"))
                    faceImage.setImageResource(R.drawable.resultv_face3)
                }
            }

            msgLabel.text = States.diagPlResult.name+"\n"+States.diagPlResult.msg
            msgLabel2.text = ": " + States.diagPlResult.msg
            waveOndoLabel.text = stringFromFloatAuto(Cfg.getOndoFC(States.diagWaveInfo.ondo))+"°"+Cfg.p1_cGiho
            waveHumiLabel.text = stringFromFloatAuto(States.diagWaveInfo.humi)+"%"

            equipmentLabel.text = ": " + States.diagEquipment.name
            materialLabel.text = ": " + States.diagMaterial.name
            faultTypeLabel.text = ": " + States.diagFaulty.name


            var soundfile = SoundFile.create(
                    States.diagFileData.filePath,
                    object : SoundFile.ProgressListener {
                        override fun reportProgress(fractionComplete: Double): Boolean {
                            return true
                        }
                    })
            //Log.i("bobopro", " wave Form Width $waveFormWidth")
            //waveformView.
            waveformView.setAudioFile(soundfile)




        udr = UDR(
                States.diagGubun,
                States.diagOndoType,
                States.diagFileData,
                FileData(),
//                States.diagImageFile,
                States.diagWaveInfo.ondo,
                States.diagWaveInfo.humi,
                States.diagDistance,
                lati,
                longi,
                States.diagEquipment.name,
                States.diagMaterial.name,
                States.diagFaulty.name,
                States.diagVolt,
                maxOndo,
                minOndo,
                "0",
                "0",
                "0",
                States.diagWaveInfo.realDb,
                States.diagWaveInfo.realDb,
                x4,
                availableDB,
                States.diagPlResult.id.toString(),
                States.diagMixResult.id,
                States.diagPlResult.id,
                States.diagOndoResult.id,
        )

    }


    /**
     * 계산
     */
    private fun readWaveInfo() {

        // 오디오 파일에서 거리, 온도, 습도를 가져온다
        val waveFile = File(States.diagFileData.filePath)
        /*// 거리 정보 저장 ( 8자리, DIS00.00 )
        // 온도 정보 저장 ( 7자리, TEM00.0 )
        // 습도 정보 저장 ( 7자리, HUM00.0 )
        // real dB 정보 저장 ( 8자리, RDB00.00)
        */
        val fis = FileInputStream(waveFile)
        val audioBytes = fis.readBytes()

        val sRealDb = String(Arrays.copyOfRange(audioBytes, audioBytes.size - 5, audioBytes.size))
        Log.d("bobopro", "realDb ${sRealDb}")
        //info { "jtyoo ${String(realDb)}" }
        States.diagWaveInfo.realDb = floatFromString(sRealDb)

        val sHumi = String(Arrays.copyOfRange(audioBytes, audioBytes.size - 12, audioBytes.size - 8))
        States.diagWaveInfo.humi = floatFromString(sHumi)
        //info { "jtyoo ${String(hum)}" }

        val sOndo = String(Arrays.copyOfRange(audioBytes, audioBytes.size - 19, audioBytes.size - 15))
        States.diagWaveInfo.ondo = floatFromString(sOndo)

        lati = ByteUtil.getFloat(audioBytes, audioBytes.size - 38)
        longi = ByteUtil.getFloat(audioBytes, audioBytes.size - 34)
        if (lati.isNaN()) {
            lati = 0f
        }
        if (longi.isNaN()) {
            longi = 0f
        }

    }

}