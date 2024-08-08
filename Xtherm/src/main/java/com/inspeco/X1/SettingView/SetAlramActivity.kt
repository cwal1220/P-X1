package com.inspeco.X1.SettingView

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentTransaction
import com.inspeco.X1.HomeView.P1Model
import com.inspeco.X1.R
import com.inspeco.data.Cfg
import com.inspeco.data.Consts
import com.inspeco.data.P1
import com.inspeco.data.States
import com.inspeco.dialog.CommonDialog
import kotlinx.android.synthetic.main.activity_set_alram.*

import java.util.*

class SetAlramActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_alram)
    }

    override fun onResume() {
        super.onResume()

        updateValuesUI()

        backButton.setOnClickListener {
            finish()
        }


        set_alram_item1.setOnClickListener {
            val a1 = getResources().getString(R.string.Active)
            val b1 = getResources().getString(R.string.Inactive)
            val dialog = SelectAb2Dialog(this, a1, b1)
            dialog.setSel01Listener {
                Cfg.p1_alram_set = true
                saveAndUpdate()
            }
            dialog.setSel02Listener {
                Cfg.p1_alram_set = false
                saveAndUpdate()
            }
            dialog.show()
        }


        set_alram_item2.setOnClickListener {
            val save = getResources().getString(R.string.Save)
            val dialog = SlideSecDialog(this, Cfg.p1_alram_db, 1,30,"dB",save)
            dialog.setOkListener() {
                Cfg.p1_alram_db = States.dialogInt
                saveAndUpdate()
            }
            dialog.show()
        }

        set_alram_item3.setOnClickListener {
            val save = getResources().getString(R.string.Save)
            val dialog = SlideSecDialog(this, Cfg.p1_alram_ondo, 0,85,Cfg.p1_cGiho,save)
            dialog.setOkListener() {
                Cfg.p1_alram_ondo = States.dialogInt
                saveAndUpdate()
            }
            dialog.show()
        }


        set_alram_item4.setOnClickListener {
            val save = getResources().getString(R.string.Save)
            val sec = getResources().getString(R.string.second)

            val dialog = SlideSecDialog(this, Cfg.p1_alram_checksec, 0,10,"초",save)
            dialog.setOkListener() {
                Cfg.p1_alram_checksec = States.dialogInt
                saveAndUpdate()
            }
            dialog.show()
        }

        set_alram_item5.setOnClickListener {
            val save = getResources().getString(R.string.Save)
            val sec = getResources().getString(R.string.second)
            val dialog = SlideSecDialog(this, Cfg.p1_alram_sec, 1,10,"초",save)
            dialog.setOkListener() {
                Cfg.p1_alram_sec = States.dialogInt
                saveAndUpdate()
            }
            dialog.show()
        }


        set_alram_item6.setOnClickListener {
            val a1 = getResources().getString(R.string.Active)
            val b1 = getResources().getString(R.string.Inactive)
            val dialog = SelectAb2Dialog(this, a1, b1)
            dialog.setSel01Listener {
                Cfg.p1_alram_vibe = true
                saveAndUpdate()
            }
            dialog.setSel02Listener {
                Cfg.p1_alram_vibe = false
                saveAndUpdate()
            }
            dialog.show()
        }

        set_alram_item7.setOnClickListener {
            val a1 = getResources().getString(R.string.Active)
            val b1 = getResources().getString(R.string.Inactive)
            val dialog = SelectAb2Dialog(this, a1, b1)
            dialog.setSel01Listener {
                Cfg.p1_alram_sound = true
                saveAndUpdate()
            }
            dialog.setSel02Listener {
                Cfg.p1_alram_sound = false
                saveAndUpdate()
            }
            dialog.show()
        }

        set_alram_item8.setOnClickListener {
            val a1 = getResources().getString(R.string.Active)
            val b1 = getResources().getString(R.string.Inactive)
            val dialog = SelectAb2Dialog(this, a1, b1)
            dialog.setSel01Listener {
                Cfg.p1_alram_icon = true
                saveAndUpdate()
            }
            dialog.setSel02Listener {
                Cfg.p1_alram_icon = false
                saveAndUpdate()
            }
            dialog.show()
        }
    }


    private fun saveAndUpdate() {
        Cfg.save_p1(this)
        updateValuesUI()
    }


    private fun updateValuesUI() {


        val sec = getResources().getString(R.string.second)

        val on = getResources().getString(R.string.On)
        val off = getResources().getString(R.string.Off)


        val alramActive = getResources().getString(R.string.Alram_Active)
        val alramInactive = getResources().getString(R.string.Alram_Inactive)

        value_item2.text = String.format("%d",Cfg.p1_alram_db)+"dB"
        value_item3.text = String.format("%d",Cfg.p1_alram_ondo)+Cfg.p1_cGiho
        value_item4.text = String.format("%d",Cfg.p1_alram_checksec)+sec
        value_item5.text = String.format("%d",Cfg.p1_alram_sec)+sec

        value_item1.text =  if (Cfg.p1_alram_set) {alramActive} else { alramInactive}
        value_item6.text =  if (Cfg.p1_alram_vibe) { on } else { off }
        value_item7.text =  if (Cfg.p1_alram_sound) { on } else { off }
        value_item8.text =  if (Cfg.p1_alram_icon) { on } else { off }
    }


}