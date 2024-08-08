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
import com.inspeco.X1.R
import com.inspeco.X1.XTerm.ByteUtil
import com.inspeco.data.*
import kotlinx.android.synthetic.main.activity_diag_result_mix.*
import kotlinx.android.synthetic.main.activity_diag_result_ondo.*
import kotlinx.android.synthetic.main.activity_diag_result_ondo.backImageButton
import kotlinx.android.synthetic.main.activity_diag_result_ondo.deviceOndoLabel
import kotlinx.android.synthetic.main.activity_diag_result_ondo.equipmentLabel
import kotlinx.android.synthetic.main.activity_diag_result_ondo.faceImage
import kotlinx.android.synthetic.main.activity_diag_result_ondo.faultTypeLabel
import kotlinx.android.synthetic.main.activity_diag_result_ondo.finishButton
import kotlinx.android.synthetic.main.activity_diag_result_ondo.materialLabel
import kotlinx.android.synthetic.main.activity_diag_result_ondo.meterImage
import kotlinx.android.synthetic.main.activity_diag_result_ondo.meterPointer
import kotlinx.android.synthetic.main.activity_diag_result_ondo.msgLabel
import kotlinx.android.synthetic.main.activity_diag_result_ondo.msgLabel2
import kotlinx.android.synthetic.main.activity_diag_result_ondo.ondoDiffLabel
import kotlinx.android.synthetic.main.activity_diag_result_ondo.ondoDiffTitle
import kotlinx.android.synthetic.main.activity_diag_result_ondo.ondoMaxTitleLabel
import kotlinx.android.synthetic.main.activity_diag_result_ondo.ondoMaxValueLabel
import kotlinx.android.synthetic.main.activity_diag_result_ondo.ondoMinTitleLabel
import kotlinx.android.synthetic.main.activity_diag_result_ondo.ondoMinValueLabel
import kotlinx.android.synthetic.main.activity_diag_result_ondo.ondoPatternLabel
import kotlinx.android.synthetic.main.activity_diag_result_ondo.resultPage
import kotlinx.android.synthetic.main.activity_diag_result_ondo.resultPicture
import kotlinx.android.synthetic.main.activity_diag_result_ondo.saveUdrButton
import kotlinx.android.synthetic.main.activity_diag_result_ondo.waveHumiLabel
import kotlinx.android.synthetic.main.activity_diag_result_ondo.waveOndoLabel
import java.io.File


class ResultOndoActivity : AppCompatActivity() {
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
        setContentView(R.layout.activity_diag_result_ondo)


        decorView = window.decorView
        uiOption = window.decorView.systemUiVisibility
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) uiOption = uiOption or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) uiOption = uiOption or View.SYSTEM_UI_FLAG_FULLSCREEN
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) uiOption = uiOption or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        decorView!!.setSystemUiVisibility(uiOption)

        finishButton.setOnClickListener { finish() }
        backImageButton.setOnClickListener { finish() }

        saveUdrButton.setOnClickListener {
            if (saveUdrFile(udr, States.diagMixResult.name, Consts.UDR_TEMP_FOLDER)) {
                val toast = Toast.makeText(this, "UDR File saved.", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, -200)
                toast.show()
            }
        }

    }

    override fun onResume() {
        super.onResume()
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


        if (States.diagOndoType == Consts.Diag_3Sang ) {
            val idx = checkOndo3Sang()-1
            Log.d("bobopro", "3상 체크 (${idx+1})")
            States.diagOndoResult = States.diagResult3List[idx].copy()

            meterImage.setImageResource(R.drawable.resultv_meter_ondo2)
            when (States.diagOndoResult.id) {
                1 -> {
                    meterPointer.animate().rotation(30f).setDuration(2000)
                    msgLabel.setTextColor(Color.parseColor("#1c531b"))
                    resultPage.setBackgroundColor(Color.parseColor("#1c531b"))
                    faceImage.setImageResource(R.drawable.resultv_face1)
                }
                2 -> {
                    meterPointer.animate().rotation(90f).setDuration(2000)
                    msgLabel.setTextColor(Color.parseColor("#154c13"))
                    resultPage.setBackgroundColor(Color.parseColor("#285323"))
                    faceImage.setImageResource(R.drawable.resultv_face2)
                }
                3 -> {
                    meterPointer.animate().rotation(150f).setDuration(2000)
                    msgLabel.setTextColor(Color.parseColor("#843d0c"))
                    resultPage.setBackgroundColor(Color.parseColor("#944d1c"))
                    faceImage.setImageResource(R.drawable.resultv_face3)
                }
            }

        } else {
            val idx = checkOndoPattern()-1
            Log.d("bobopro", "패턴 체크 (${idx+1})")
            States.diagOndoResult = States.diagResult4List[idx].copy()
            meterImage.setImageResource(R.drawable.resultv_meter_ondo1)

            when (States.diagOndoResult.id) {
                1 -> {
                    meterPointer.animate().rotation(22.5f).setDuration(2000)
                    msgLabel.setTextColor(Color.parseColor("#1c531b"))
                    resultPage.setBackgroundColor(Color.parseColor("#1c531b"))
                    faceImage.setImageResource(R.drawable.resultv_face1)
                }
                2 -> {
                    meterPointer.animate().rotation(67.5f).setDuration(2000)
                    msgLabel.setTextColor(Color.parseColor("#154c13"))
                    resultPage.setBackgroundColor(Color.parseColor("#285323"))
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
            }


        }




        Glide.with(this).load(File(States.diagImageFile.filePath)).into(resultPicture)

        msgLabel.text = States.diagOndoResult.name+"\n"+States.diagOndoResult.msg
        msgLabel2.text = ": " + States.diagOndoResult.msg


        waveOndoLabel.text = stringFromFloatAuto(Cfg.getOndoFC(States.diagOndo))+"°"+Cfg.p1_cGiho
        waveHumiLabel.text = stringFromFloatAuto(States.diagHumi)+"%"

        equipmentLabel.text = ": " + States.diagEquipment.name
        materialLabel.text = ": " + States.diagMaterial.name
        faultTypeLabel.text = ": " + States.diagFaulty.name


        if (States.diagOndoType == Consts.Diag_3Sang) {
            deviceOndoLabel.text = stringFromFloatAuto( Cfg.getOndoFC( maxOndo))+"°"+Cfg.p1_cGiho
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
            deviceOndoLabel.text = stringFromFloatAuto( Cfg.getOndoFC( States.diagTargetOndo))+"°"+Cfg.p1_cGiho
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


        States.diagMixResult.id = 0
        States.diagPlResult.id = 0


        udr = UDR(
                States.diagGubun,
                States.diagOndoType,
                States.diagFileData,
                States.diagImageFile,
                States.diagOndo,
                States.diagHumi,
                States.diagDistance,
                States.diagLati,
                States.diagLongi,
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
                0f,
                0f,
                States.diagMixResult.name,
                States.diagMixResult.id,
                States.diagPlResult.id,
                States.diagOndoResult.id
        )

    }


    // 온도 패턴법 : 타겟 온도와 기준온도의 차이로 계산
    private fun checkOndoPattern(): Int {
 //       var ondoSum = 0f;

//        States.diagOndoList.forEach {
//            ondoSum += it
//        }
        // 기자재 평균온도
//        val ondo = ondoSum / States.diagOndoList.size
//        val ondo = States.diagTargetOndo
        minOndo = States.diagBaseOndo
        ondoDiff = States.diagTargetOndo - minOndo

        if (States.diagEquipment.eType==1) {
            // 송전 배전 설비는 기온에서 장비 온도차
            //minOndo = States.diagWaveInfo.ondo

            if (ondoDiff < 14f) {
                return 1  // 양호
            } else if (ondoDiff < 21f) {
                return 2  // 열화가능성
            } else if (ondoDiff < 61f) {
                return 3  // 추후 결함
            } else {
                return 4  // 결함
            }
        } else {

            if (ondoDiff < -30f) {
                return 1  // 양호
            } else if (ondoDiff < -15) {
                return 2  // 열화가능성
            } else if (ondoDiff < 0) {
                return 3  // 추후 결함
            } else {
                return 4  // 결함
            }
        }
    }


    // 삼상 체크 ( 터치 리스트 2개 이상의 점 중에서 최고점, 최저점 )
    private fun checkOndo3Sang(): Int {

        ondoDiff = maxOndo - minOndo
        if (ondoDiff <= 5f) {
            return 1  // 정상
        } else if (ondoDiff < 10f) {
            return 2  // 주의
        } else {
            return 3  // Falut
        }
    }


}