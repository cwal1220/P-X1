package com.inspeco.X1.StatusJudgView

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentTransaction
import com.inspeco.X1.R
import com.inspeco.data.Consts
import com.inspeco.data.Ini.checkIniFile
import com.inspeco.data.States
import kotlinx.android.synthetic.main.activity_diag.*

class DiagActivity : AppCompatActivity() {

    private var tabIndex = 0
    private var oldIndex = -1

    private val tabMix = 0
    private val tabReal = 1
    private val tabOndo = 2

    lateinit var mix_fragment : DiagMixFragment
    lateinit var wave_fragment : DiagWaveFragment
    lateinit var ondo_fragment : DiagOndoFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diag)
        States.diagFileData.fileName = ""
        States.diagImageFile.fileName = ""
        States.diagOndoType = Consts.Diag_3Sang
        States.diagFacility = 0
        States.diagVolt = 0f
        States.diagEquipment.name = ""
        States.diagBaseOndo = 0f
        States.diagOndoList.clear()
        States.diagMaterial.name = ""
        States.diagFaulty.name = ""
        States.diagDistance = 12.5f
        States.diagGubun =  Consts.Diag_mixMode
    }


    override fun onResume() {
        super.onResume()

        States.curView = Consts.VIEW_DIAG
        updateTabUI()

        tabCameraMix.setOnClickListener {
            tabIndex = tabMix
            States.diagGubun =  Consts.Diag_mixMode
            updateTabUI()
        }

        tabCameraReal.setOnClickListener {
            tabIndex = tabReal
            States.diagGubun =  Consts.Diag_waveMode
            updateTabUI()
        }

        tabCameraOndo.setOnClickListener {
            tabIndex = tabOndo
            States.diagGubun =  Consts.Diag_ondoMode
            updateTabUI()
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
                mix_fragment = DiagMixFragment()
                supportFragmentManager.beginTransaction()
                        .replace(R.id.cam_menu_holder, mix_fragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
            } else if (tabIndex==tabReal) {
                wave_fragment = DiagWaveFragment()
                supportFragmentManager.beginTransaction()
                        .replace(R.id.cam_menu_holder, wave_fragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
            } else if (tabIndex==tabOndo) {
                ondo_fragment = DiagOndoFragment()
                supportFragmentManager.beginTransaction()
                        .replace(R.id.cam_menu_holder, ondo_fragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
            }

        }

    }


    override fun onBackPressed() {
        if ( (tabIndex==tabMix) && (States.diagPage==1)) {
            States.diagPage = 0
            mix_fragment.updatePageUI()
        } else  if ( (tabIndex==tabOndo) && (States.diagPage==1))  {
                States.diagPage = 0
                ondo_fragment.updatePageUI()
        } else {

            super.onBackPressed()
        }
    }


}