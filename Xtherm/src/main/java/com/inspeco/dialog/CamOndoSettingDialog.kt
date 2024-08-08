package com.inspeco.dialog

import android.app.Dialog
import android.content.Context
import android.text.TextUtils
import android.view.View
import android.view.Window
import android.widget.SeekBar
import com.inspeco.X1.R
import com.inspeco.data.Cfg
import com.inspeco.data.Consts
import kotlinx.android.synthetic.main.d_cam3_ondo.*
import kotlinx.android.synthetic.main.d_common.cancelButton
import kotlinx.android.synthetic.main.d_common.confirmButton

/**
 * 기본 다이얼로그
 */class CamOndoSettingDialog(context: Context, camMode: Int, ok: String?, no: String?) : Dialog(context) {
    private val TAG = "bobopro-온도설정"
    private var okClick: View.OnClickListener? = null
    private var cancelClick: View.OnClickListener? = null

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        setCanceledOnTouchOutside(false)
        setCancelable(true)

        setContentView(R.layout.d_cam_ondo)

        if (TextUtils.isEmpty(ok)) {
            confirmButton.visibility = View.GONE
        } else {
            confirmButton.visibility = View.VISIBLE
            confirmButton.text = ok
        }

        if ( Cfg.ondo_extMode) {
            dc3_max_seekBar.max = 200+6000
            dc3_min_seekBar.max = 200+6000
        } else {
            dc3_max_seekBar.max = 200+1200
            dc3_min_seekBar.max = 200+1200
        }

        confirmButton.setOnClickListener {

            if ( Cfg.ondo_extMode) {
                Cfg.cam3_max600Ondo = dc3_max_seekBar.progress.toFloat() / 10f - 20f
                Cfg.cam3_min600Ondo = dc3_min_seekBar.progress.toFloat() / 10f - 20f
                Cfg.cam3_max600Auto =  max_checkBox.isChecked
                Cfg.cam3_min600Auto =  min_checkBox.isChecked
                Cfg.save_cam3Ondo(context)
            } else if (camMode == Consts.CAM_MIX) {
                Cfg.cam3_max120Ondo = dc3_max_seekBar.progress.toFloat() / 10f - 20f
                Cfg.cam3_min120Ondo = dc3_min_seekBar.progress.toFloat() / 10f - 20f
                Cfg.cam3_max120Auto =  max_checkBox.isChecked
                Cfg.cam3_min120Auto =  min_checkBox.isChecked
                Cfg.save_cam3Ondo(context)
            }

            this.dismiss()
            okClick?.onClick(it)

        }

        if (TextUtils.isEmpty(no)) {
            cancelButton.visibility = View.GONE
        } else {
            cancelButton.visibility = View.VISIBLE
            cancelButton.text = no
        }

        cancelButton.setOnClickListener {
            this.dismiss()
            cancelClick?.onClick(it)
        }


        if (Cfg.ondo_extMode) {
            dc3_max_seekBar.progress = (Cfg.cam3_max600Ondo * 10f).toInt()+200
            dc3_min_seekBar.progress = (Cfg.cam3_min600Ondo * 10f).toInt()+200
            max_checkBox.isChecked = Cfg.cam3_max600Auto
            min_checkBox.isChecked = Cfg.cam3_min600Auto
        } else if (camMode == Consts.CAM_MIX) {
            dc3_max_seekBar.progress = (Cfg.cam3_max120Ondo * 10f).toInt()+200
            dc3_min_seekBar.progress = (Cfg.cam3_min120Ondo * 10f).toInt()+200
            max_checkBox.isChecked = Cfg.cam3_max120Auto
            min_checkBox.isChecked = Cfg.cam3_min120Auto
        }

        dc3_max_label.text = String.format("%.1f°", dc3_max_seekBar.progress.toFloat() / 10f -20f) + Cfg.p1_cGiho;
        dc3_min_label.text = String.format("%.1f°", dc3_min_seekBar.progress.toFloat() / 10f -20f) + Cfg.p1_cGiho;




        dc3_max_seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                dc3_max_label.text = String.format("%.1f°", (seekBar.progress.toFloat() / 10f-20f)) + Cfg.p1_cGiho;
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {
                dc3_max_label.text = String.format("%.1f°", (seekBar.progress.toFloat() / 10f)-20f) + Cfg.p1_cGiho;
            }
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                dc3_max_label.text = String.format("%.1f°", (seekBar.progress.toFloat() / 10f)-20f) + Cfg.p1_cGiho;
            }
        })


        dc3_min_seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                dc3_min_label.text = String.format("%.1f°", (seekBar.progress.toFloat() / 10f -20f)) + Cfg.p1_cGiho;
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                dc3_min_label.text = String.format("%.1f°", (seekBar.progress.toFloat() / 10f)-20f) + Cfg.p1_cGiho;
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                dc3_min_label.text = String.format("%.1f°", (seekBar.progress.toFloat() / 10f)-20f) + Cfg.p1_cGiho;
            }
        })

    }

    /**
     * ok 리스너 등록
     */
    fun setOkListener(okClick: View.OnClickListener) {
        this.okClick = okClick
    }

    /**
     * cancel 리스너 등록
     */
    fun setCancelListener(cancelClick: View.OnClickListener?) {
        this.cancelClick = cancelClick
    }
}