package com.inspeco.X1.ReportView

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentTransaction
import com.inspeco.X1.HomeView.P1Model
import com.inspeco.X1.R
import com.inspeco.X1.StatusJudgView.DiagMixFragment
import com.inspeco.X1.StatusJudgView.DiagOndoFragment
import com.inspeco.X1.StatusJudgView.DiagWaveFragment
import com.inspeco.data.Consts
import com.inspeco.data.P1
import com.inspeco.data.States
import com.inspeco.dialog.CommonDialog
import kotlinx.android.synthetic.main.activity_diag.*
import kotlinx.android.synthetic.main.activity_diag.tabBgMix
import kotlinx.android.synthetic.main.activity_diag.tabBgOndo
import kotlinx.android.synthetic.main.activity_diag.tabBgReal
import kotlinx.android.synthetic.main.activity_diag.tabCameraMix
import kotlinx.android.synthetic.main.activity_diag.tabCameraOndo
import kotlinx.android.synthetic.main.activity_diag.tabCameraReal
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_home.progress_rec
import kotlinx.android.synthetic.main.activity_report.*
import kotlinx.android.synthetic.main.menu_drawer.*
import java.util.*

class ReportActivity : AppCompatActivity() {

    private var tabIndex = 0
    private var oldIndex = -1

    private val tabMix = 0
    private val tabReal = 1
    private val tabOndo = 2

    lateinit var mix_fragment : ReportMixFragment
    lateinit var ondo_fragment : ReportOndoFragment
    lateinit var wave_fragment : ReportWaveFragment

    // 녹음 중 progress 를 위한 타이머
    private var progressTimer = Timer()
    private lateinit var p1 : P1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)
        p1 = P1.getInstance()
    }

    override fun onResume() {
        super.onResume()
        States.curView = Consts.VIEW_REPORT
        updateTabUI()

        if (tabIndex == tabMix) {
            mix_fragment = ReportMixFragment()
            supportFragmentManager.beginTransaction()
                    .replace(R.id.cam_menu_holder, mix_fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit()
        }

        tabCameraMix.setOnClickListener {
            tabIndex = tabMix
            updateTabUI()
        }


        tabCameraReal.setOnClickListener {
            tabIndex = tabReal
            updateTabUI()
        }

        tabCameraOndo.setOnClickListener {
            tabIndex = tabOndo
            updateTabUI()
        }


        audoRecordTest.setOnClickListener{
            val msg = getResources().getString(R.string.Do_you_want_to_start_recoring)
            val yes = getResources().getString(R.string.Yes)
            val no = getResources().getString(R.string.No)

            val dialog = CommonDialog(this, "", msg, yes, no)
            dialog.setOkListener() {
                p1.p1Model.startRecording(false)
                showProgressDialog(View.VISIBLE)
            }
            dialog.show()
        }

    }


    private fun updateTabUI() {

        if (tabIndex==tabMix) {
            tabBgMix.setBackgroundColor(Color.parseColor("#ffffff"))
            tabCameraMix.setImageResource(R.drawable.diagv_menu_mixcamb)
        } else {
            tabBgMix.setBackgroundColor(Color.parseColor("#ededed"))
            tabCameraMix.setImageResource(R.drawable.homev_menu_mixcam)
        }

        if (tabIndex==tabReal) {
            tabBgReal.setBackgroundColor(Color.parseColor("#ffffff"))
            tabCameraReal.setImageResource(R.drawable.diagv_menu_realcamb)
        } else {
            tabBgReal.setBackgroundColor(Color.parseColor("#ededed"))
            tabCameraReal.setImageResource(R.drawable.homev_menu_realcam)
        }

        if (tabIndex==tabOndo) {
            tabBgOndo.setBackgroundColor(Color.parseColor("#ffffff"))
            tabCameraOndo.setImageResource(R.drawable.diagv_menu_ondocamb)
        } else {
            tabBgOndo.setBackgroundColor(Color.parseColor("#ededed"))
            tabCameraOndo.setImageResource(R.drawable.homev_menu_ondocam)
        }

        if (tabIndex != oldIndex) {
            oldIndex = tabIndex

            if (tabIndex==tabMix) {
                mix_fragment = ReportMixFragment()
                supportFragmentManager.beginTransaction()
                        .replace(R.id.cam_menu_holder, mix_fragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
            } else if (tabIndex==tabReal) {
                wave_fragment = ReportWaveFragment()
                supportFragmentManager.beginTransaction()
                        .replace(R.id.cam_menu_holder, wave_fragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
            } else if (tabIndex==tabOndo) {
                ondo_fragment = ReportOndoFragment()
                supportFragmentManager.beginTransaction()
                        .replace(R.id.cam_menu_holder, ondo_fragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
            }

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
                        if ( (cnt > 120) || (!p1.isRecording) ) {
                            showProgressDialog(View.GONE)
                        } else {
                            if (cnt>100) {
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
            }
        }
    }


}