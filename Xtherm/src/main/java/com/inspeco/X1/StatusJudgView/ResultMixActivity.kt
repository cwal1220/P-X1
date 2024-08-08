package com.inspeco.X1.StatusJudgView

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.l_github.derlio.waveform.soundfile.SoundFile
import com.inspeco.X1.R
import com.inspeco.X1.XTerm.ByteUtil
import com.inspeco.data.*
import kotlinx.android.synthetic.main.activity_diag_result_mix.*
import kotlinx.android.synthetic.main.activity_diag_result_mix.backImageButton
import kotlinx.android.synthetic.main.activity_diag_result_mix.deviceOndoLabel
import kotlinx.android.synthetic.main.activity_diag_result_mix.equipmentLabel
import kotlinx.android.synthetic.main.activity_diag_result_mix.faceImage
import kotlinx.android.synthetic.main.activity_diag_result_mix.faultTypeLabel
import kotlinx.android.synthetic.main.activity_diag_result_mix.finishButton
import kotlinx.android.synthetic.main.activity_diag_result_mix.materialLabel
import kotlinx.android.synthetic.main.activity_diag_result_mix.meterPointer
import kotlinx.android.synthetic.main.activity_diag_result_mix.msgLabel
import kotlinx.android.synthetic.main.activity_diag_result_mix.msgLabel2
import kotlinx.android.synthetic.main.activity_diag_result_mix.ondoDiffLabel
import kotlinx.android.synthetic.main.activity_diag_result_mix.ondoDiffTitle
import kotlinx.android.synthetic.main.activity_diag_result_mix.ondoMaxTitleLabel
import kotlinx.android.synthetic.main.activity_diag_result_mix.ondoMaxValueLabel
import kotlinx.android.synthetic.main.activity_diag_result_mix.ondoMinTitleLabel
import kotlinx.android.synthetic.main.activity_diag_result_mix.ondoMinValueLabel
import kotlinx.android.synthetic.main.activity_diag_result_mix.ondoPatternLabel
import kotlinx.android.synthetic.main.activity_diag_result_mix.resultPage
import kotlinx.android.synthetic.main.activity_diag_result_mix.resultPicture
import kotlinx.android.synthetic.main.activity_diag_result_mix.saveUdrButton
import kotlinx.android.synthetic.main.activity_diag_result_mix.waveHumiLabel
import kotlinx.android.synthetic.main.activity_diag_result_mix.waveOndoLabel
import kotlinx.android.synthetic.main.activity_diag_result_ondo.*
import java.io.File
import java.io.FileInputStream
import java.util.*


class ResultMixActivity : AppCompatActivity() {
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
        setContentView(R.layout.activity_diag_result_mix)

        Ini.checkIniFile(this)
        decorView = window.decorView
        uiOption = window.decorView.systemUiVisibility
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) uiOption = uiOption or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) uiOption = uiOption or View.SYSTEM_UI_FLAG_FULLSCREEN
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) uiOption = uiOption or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        decorView!!.setSystemUiVisibility(uiOption)
        
        finishButton.setOnClickListener { finish() }
        backImageButton.setOnClickListener { finish() }

        saveUdrButton.setOnClickListener {

            if (saveUdrFile(udr,  States.diagMixResult.name, Consts.UDR_MIX_FOLDER)) {
                val toast = Toast.makeText(this, "UDR File Saved.", Toast.LENGTH_SHORT)
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
        Ini.checkIniFile(this)

        // 측정점 최고, 최저 온도
        var index = 0
        States.diagOndoList.forEach {
            if (it < minOndo) {
                minOndo = it
                minIndex = index
            }
            if (it > maxOndo) {
                maxOndo = it
                maxIndex = index
            }
            index++
        }

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

        //availableDB += States.diagVolt

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
        } else if (availableDB >= -20) {
            States.diagPlResult = Ini.diagPLList[5].copy()
        }

        if (States.diagOndoType == Consts.Diag_3Sang ) {
            checkOndo3Sang()
            if (States.diagGubun == Consts.Diag_mixMode) { //복합진단
                Log.d("bobopro", "3상 체크 ${States.diagPlResult.id}, ${States.diagOndoResult.id} ")
                States.diagMixResult.id = 0
                States.diagMixMatrix.forEach {
                    if  ( (it.pl==States.diagPlResult.id) && (it.on3==States.diagOndoResult.id) ) {
                        Log.d("bobopro", "체크 ${States.diagPlResult.id}, ${States.diagOndoResult.id}, ${it.result} ")
                        States.diagMixResult = States.diagResult4List[it.result].copy()
                    }
                }
            }
        } else {
            checkOndoPattern()
            if (States.diagGubun == Consts.Diag_mixMode) {  //복합진단
                Log.d("bobopro", "패턴 체크")
                States.diagMixResult.id = 0
                States.diagMixMatrix.forEach {
                    if  ( (it.pl==States.diagPlResult.id) && (it.on4==States.diagOndoResult.id) ) {
                        States.diagMixResult = States.diagResult4List[it.result].copy()
                    }
                }
            }
        }

        if (States.diagGubun == Consts.Diag_mixMode) {

            val msgStr = String.format("복합결과  Result:%d, Pl %d, Ondo %d",
                     States.diagMixResult.id, States.diagPlResult.id, States.diagOndoResult.id)
//            val toast = Toast.makeText(this,msgStr, Toast.LENGTH_LONG)
//            toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200)
//            toast.show()

            Log.d("bobopro", msgStr)

            dbLabel.text = stringFromFloatAuto(States.diagWaveInfo.realDb)+"dB"
            Glide.with(this).load(File(States.diagImageFile.filePath)).into(resultPicture)


            when (States.diagMixResult.id) {
                1 -> {
                    meterPointer.animate().rotation(22.5f).setDuration(2000)
                    msgLabel.setTextColor(Color.parseColor("#1c531b"))
                    resultPage.setBackgroundColor(Color.parseColor("#1c531b"))
                    faceImage.setImageResource(R.drawable.resultv_face1)
                }
                2 -> {
                    meterPointer.animate().rotation(67.5f).setDuration(2000)
                    msgLabel.setTextColor(Color.parseColor("#154c13"))
                    resultPage.setBackgroundColor(Color.parseColor("#286323"))
                    faceImage.setImageResource(R.drawable.resultv_face2)
                }
                3 -> {
                    meterPointer.animate().rotation(112.5f).setDuration(2000)
                    msgLabel.setTextColor(Color.parseColor("#843d0c"))
                    resultPage.setBackgroundColor(Color.parseColor("#944d1c"))
                    faceImage.setImageResource(R.drawable.resultv_face3)
                }
                4 -> {
                    meterPointer.animate().rotation(159.5f).setDuration(2000)
                    msgLabel.setTextColor(Color.parseColor("#771f1d"))
                    resultPage.setBackgroundColor(Color.parseColor("#872f2d"))
                    faceImage.setImageResource(R.drawable.resultv_face4)


                }
                else -> {
                    val msgStr = String.format("Result Fail.\n %d, Pl %d, Ondo %d",
                            States.diagOndoType, States.diagPlResult.id, States.diagOndoResult)
                    val toast = Toast.makeText(this, msgStr, Toast.LENGTH_LONG)
                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200)
                    toast.show()
                }
            }

            msgLabel.text = States.diagMixResult.name+"\n"+States.diagMixResult.msg
            msgLabel2.text = ": " + States.diagMixResult.msg
            waveOndoLabel.text = stringFromFloatAuto(Cfg.getOndoFC( States.diagWaveInfo.ondo))+"°"+Cfg.p1_cGiho
            waveHumiLabel.text = stringFromFloatAuto(States.diagWaveInfo.humi)+"%"

            equipmentLabel.text = ": " + States.diagEquipment.name
            materialLabel.text = ": " + States.diagMaterial.name
            faultTypeLabel.text = ": " + States.diagFaulty.name

            if (States.diagOndoType == Consts.Diag_3Sang) {
                deviceOndoLabel.text = stringFromFloatAuto(Cfg.getOndoFC( maxOndo))+"°"+Cfg.p1_cGiho
                val threeMethod = getResources().getString(R.string.three_Phase_Comparison_method)
                ondoPatternLabel.text =  ": " + threeMethod


                val maxGiho = 'A'+maxIndex
                val minGiho = 'A'+minIndex
                ondoMaxTitleLabel.text = "POINT "+maxGiho
                ondoMaxValueLabel.text = ": " + stringFromFloatAuto(Cfg.getOndoFC( maxOndo))+"°" + Cfg.p1_cGiho
                ondoMinTitleLabel.text = "POINT "+minGiho
                ondoMinValueLabel.text = ": " + stringFromFloatAuto(Cfg.getOndoFC( minOndo))+"°" + Cfg.p1_cGiho
                ondoDiffLabel.text = ": " + stringFromFloatAuto(Cfg.getOndoFC( ondoDiff))+"°" + Cfg.p1_cGiho

            } else {
//                ondoPatternLabel.text =  ": " + "온도 패턴법"
//                ondoDiff = States.diagTargetOndo - ondoDiff
//                ondoMaxTitleLabel.text = "포인트"
//                ondoMaxValueLabel.text = ": " + stringFromFloatAuto(Cfg.getOndoFC( minOndo))+"°" + Cfg.p1_cGiho
//                ondoMinTitleLabel.text = "TARGET"
//                ondoMinValueLabel.text = ": " + stringFromFloatAuto(Cfg.getOndoFC( States.diagTargetOndo))+"°" + Cfg.p1_cGiho
//                ondoDiffLabel.text = ": " + stringFromFloatAuto(Cfg.getOndoFC( ondoDiff))+"°" + Cfg.p1_cGiho

                deviceOndoLabel.text = stringFromFloatAuto(Cfg.getOndoFC( States.diagTargetOndo ))+"°"+Cfg.p1_cGiho

                val tempPattern = getResources().getString(R.string.TEMP_pattern_method)
                ondoPatternLabel.text =  ": " + tempPattern

                ondoDiff = ondoDiff
                ondoMaxTitleLabel.text = "TARGET"
                ondoMaxValueLabel.text = ": " + stringFromFloatAuto(Cfg.getOndoFC( States.diagTargetOndo))+"°" + Cfg.p1_cGiho
                val refOndo = getResources().getString(R.string.Reference_TEMP)
                ondoMinTitleLabel.text = refOndo
                ondoMinValueLabel.text = ": " + stringFromFloatAuto(Cfg.getOndoFC( minOndo))+"°" + Cfg.p1_cGiho
                ondoDiffLabel.text = ": " + stringFromFloatAuto(Cfg.getOndoFC( ondoDiff))+"°" + Cfg.p1_cGiho

            }



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


        } //end ondo_mix

        udr = UDR(
                States.diagGubun,
                States.diagOndoType,
                States.diagFileData,
                States.diagImageFile,
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
                ondoMaxTitleLabel.text.toString()+ondoMaxValueLabel.text.toString(),
                ondoMinTitleLabel.text.toString()+ondoMinValueLabel.text.toString(),
                ondoDiffTitle.text.toString()+ondoDiffLabel.text.toString(),
                States.diagWaveInfo.realDb,
                States.diagWaveInfo.realDb,
                x4,
                availableDB,
                States.diagMixResult.name,
                States.diagMixResult.id,
                States.diagPlResult.id,
                States.diagOndoResult.id
        )

    }

    private fun checkOndoPattern() {
        var ondoSum = 0f;

//        States.diagOndoList.forEach {
//            ondoSum += it
//        }
//        // 기자재 평균온도 > target
////        val ondo = ondoSum / States.diagOndoList.size
//        val ondo = States.diagTargetOndo

        minOndo = States.diagBaseOndo
        ondoDiff = States.diagTargetOndo - minOndo


        if (States.diagEquipment.eType==1) {
            // 송전 배전 설비는 기온에서 장비 온도차
//            ondoDiff = ondo - States.diagWaveInfo.ondo
            if (ondoDiff < 14f) {
                States.diagOndoResult.id = 1  // 양호
            } else if (ondoDiff < 21f) {
                States.diagOndoResult.id = 2  // 열화가능성
            } else if (ondoDiff < 61f) {
                States.diagOndoResult.id = 3  // 추후 결함
            } else {
                States.diagOndoResult.id = 4  // 결함
            }
        } else {
//            ondoDiff = ondo - States.diagEquipment.baseOndo
            if (ondoDiff < -30f) {
                States.diagOndoResult.id = 1  // 양호
            } else if (ondoDiff < -15) {
                States.diagOndoResult.id = 2  // 열화가능성
            } else if (ondoDiff < 0) {
                States.diagOndoResult.id = 3  // 추후 결함
            } else {
                States.diagOndoResult.id = 4  // 결함
            }
        }
    }

    private fun checkOndo3Sang() {

        ondoDiff = maxOndo - minOndo
        if (ondoDiff <= 5f) {
            States.diagOndoResult.id = 1  // 정상
        } else if (ondoDiff < 10f) {
            States.diagOndoResult.id = 2  // 주의
        } else {
            States.diagOndoResult.id = 3  // Falut
        }
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
        //info { "jtyoo ${String(realDb)}" }
        States.diagWaveInfo.realDb = floatFromString(sRealDb)

        val sHumi = String(Arrays.copyOfRange(audioBytes, audioBytes.size - 12, audioBytes.size - 8))
        States.diagWaveInfo.humi = floatFromString(sHumi)

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