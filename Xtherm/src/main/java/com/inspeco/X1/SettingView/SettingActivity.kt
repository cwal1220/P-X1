package com.inspeco.X1.SettingView

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentTransaction
import com.inspeco.X1.HomeView.P1Model
import com.inspeco.X1.R
import com.inspeco.data.Consts
import com.inspeco.data.P1
import com.inspeco.data.States
import com.inspeco.dialog.CommonDialog
import kotlinx.android.synthetic.main.activity_setting.*

import java.util.*

class SettingActivity : AppCompatActivity() {

    private var tabIndex = 0
    private var oldIndex = -1

    private val tabSetting = 3
    private val tabMix = 0
    private val tabReal = 1
    private val tabOndo = 2

    lateinit var base_fragment : SetBaseFragment
    lateinit var mix_fragment : SetCam3Fragment
    lateinit var ondo_fragment : SetCam2Fragment
    lateinit var wave_fragment : SetCam1Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        tabIndex = tabSetting
    }

    override fun onResume() {
        super.onResume()

        States.curView = Consts.VIEW_SETTING
        updateTabUI()

        tabSet.setOnClickListener {
            tabIndex = tabSetting
            updateTabUI()
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

    }


    private fun updateTabUI() {

        if (tabIndex==tabSetting) {
            tabBgSet.setBackgroundColor(Color.parseColor("#ffffff"))
            tabSet.setImageResource(R.drawable.set_icon_tab1b)
        } else {
            tabBgSet.setBackgroundColor(Color.parseColor("#ededed"))
            tabSet.setImageResource(R.drawable.set_icon_tab1)
        }

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

            if (tabIndex==tabSetting) {
                base_fragment = SetBaseFragment()
                base_fragment.setContext(this)
                supportFragmentManager.beginTransaction()
                        .replace(R.id.cam_menu_holder, base_fragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
            } else if (tabIndex==tabMix) {
                mix_fragment = SetCam3Fragment()
                mix_fragment.setContext(this)
                mix_fragment.hideCloseButton()
                supportFragmentManager.beginTransaction()
                        .replace(R.id.cam_menu_holder, mix_fragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
            } else if (tabIndex==tabReal) {
                wave_fragment = SetCam1Fragment()
                wave_fragment.setContext(this)
                wave_fragment.hideCloseButton()
                supportFragmentManager.beginTransaction()
                        .replace(R.id.cam_menu_holder, wave_fragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
            } else if (tabIndex==tabOndo) {
                ondo_fragment = SetCam2Fragment()
                ondo_fragment.setContext(this)
                ondo_fragment.hideCloseButton()
                supportFragmentManager.beginTransaction()
                        .replace(R.id.cam_menu_holder, ondo_fragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
            }

        }


    }



}