package com.inspeco.X1.SettingView

import android.app.Dialog
import android.content.Context
import android.text.TextUtils
import android.view.View
import android.view.Window
import android.widget.SeekBar
import com.inspeco.X1.R
import com.inspeco.data.Cfg
import com.inspeco.data.Consts
import com.inspeco.data.States
import kotlinx.android.synthetic.main.d_slide_ondo.*

/**
 * 기본 다이얼로그
 */class SlideOndoDialog(context: Context, value: Float, suffix:String, ok: String) : Dialog(context) {
    private val TAG = "bobopro-ondo"
    private var okClick: View.OnClickListener? = null
    private var sSuffix = "°C"

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        setCanceledOnTouchOutside(true)
        setCancelable(true)

        setContentView(R.layout.d_slide_ondo)

        val intValue = ((value * 10f)+100).toInt()

        noSlideBar.progress = intValue

        setNoLabel(noSlideBar.progress)

        confirmButton.text = ok
        sSuffix = suffix

        confirmButton.setOnClickListener {
            States.dialogFloat = ((noSlideBar.progress / 10f)-10.0f).toFloat()
            this.dismiss()
            okClick?.onClick(it)
        }


    noSlideBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                setNoLabel(seekBar.progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {
                setNoLabel(seekBar.progress)
            }
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                setNoLabel(seekBar.progress)
            }
        })
    }

    fun setNoLabel(no: Int) {
        val number1 = (no/10f)-10.0f
        titleLabel.text = String.format("%.1f", number1) + sSuffix
    }


    /**
     * ok 리스너 등록
     */
    fun setOkListener(okClick: View.OnClickListener) {
        this.okClick = okClick
    }

}