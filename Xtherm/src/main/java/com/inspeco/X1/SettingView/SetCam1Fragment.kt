package com.inspeco.X1.SettingView

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.fragment.app.Fragment
import com.inspeco.X1.R
import com.inspeco.X1.StatusJudgView.EditDistanceDialog
import com.inspeco.data.Cfg
import com.inspeco.data.Consts
import com.inspeco.data.P1
import com.inspeco.data.States
import com.inspeco.dialog.Cam3OndoDialog
import com.inspeco.dialog.CamOndoSettingDialog
import com.inspeco.dialog.PaletteDialog
import kotlinx.android.synthetic.main.fragment_set_cam1.*


class SetCam1Fragment : Fragment() {

    private var closeClick: View.OnClickListener? = null
    private var paletteItemClick: View.OnClickListener? = null
    private var isHideClose = false
    private lateinit var p1 : P1

    private lateinit var mContext: Context

    fun setContext(aContext: Context) {
        mContext = aContext
    }

    fun hideCloseButton() {
        isHideClose = true
    }


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        p1 = P1.getInstance()
        return inflater.inflate(R.layout.fragment_set_cam1, container, false)
    }


    override fun onResume() {
        super.onResume()

        if (isHideClose) {
            fset_cam1_close.visibility = View.GONE
        }
        val sec = getResources().getString(R.string.second)
        val save = getResources().getString(R.string.Save)
        set_cam1_item1.setOnClickListener {
            val dialog = SlideSecDialog(mContext, Cfg.p1_maxDbSec, 1, 30,sec,save)
            dialog.setOkListener() {
                Cfg.p1_maxDbSec = States.dialogInt
                Cfg.save_p1(context)
            }
            dialog.show()
        }


        set_cam1_item2.setOnClickListener {
            val sec = getResources().getString(R.string.second)
            val save = getResources().getString(R.string.Save)
            val dialog = SlideSecDialog(mContext, Cfg.p1_avrDbSec, 1,30,sec,save)
            dialog.setOkListener() {
                Cfg.p1_avrDbSec = States.dialogInt
                Cfg.save_p1(context)
            }
            dialog.show()
        }

        set_cam1_item3.setOnClickListener {
            val dialog = EditPercentDialog(mContext, Cfg.p1_volume)
            dialog.setSaveClickListener() {
                Cfg.p1_volume = States.dialogInt
                Cfg.save_p1(context)
                p1.p1Model.sendControlDataVolume(Cfg.p1_volume)
            }
            dialog.show()
        }


        set_cam1_item4.setOnClickListener {
            val dialog = EditEfeqDialog(mContext, Cfg.p1_eFeq)
            dialog.setSaveClickListener() {
                Cfg.p1_eFeq = States.dialogInt
                Cfg.save_p1(context)
                p1.p1Model.sendControlDataFreq(Cfg.p1_eFeq)
            }
            dialog.show()
        }



        fset_cam1_close.setOnClickListener {
            closeClick?.onClick(it)
        }

        set_cam1_item5.setOnClickListener {
            val dialog = EditDistanceDialog(mContext, Cfg.p1_dist)
            dialog.setSaveClickListener() {
                Cfg.p1_dist = States.dialogFloat
                Cfg.save_p1(context)
            }
            dialog.show()
        }

    }


    fun setCloseListener(onClick: View.OnClickListener) {
        this.closeClick = onClick
    }

    fun setPaletteItemClickListener(onClick: View.OnClickListener) {
        this.paletteItemClick = onClick
    }


}