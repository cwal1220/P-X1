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
import kotlinx.android.synthetic.main.d_slide_sec.*

/**
 * 기본 다이얼로그
 */class SlideSecDialog(context: Context, secValue: Int, minValue1:Int, maxValue: Int, suffix:String, ok: String) : Dialog(context) {
    private val TAG = "bobopro-초"
    private var okClick: View.OnClickListener? = null
    private var sSuffix = "초"
    private var minValue = 0

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        setCanceledOnTouchOutside(false)
        setCancelable(true)

        setContentView(R.layout.d_slide_sec)

        noSlideBar.progress = secValue
        setNoLabel(noSlideBar.progress)
        minValue = minValue1

        confirmButton.text = ok
        noSlideBar.max = maxValue
        sSuffix = suffix
        setNoLabel(secValue)
        max_label.text = String.format("%d", maxValue)
        min_label.text = String.format("%d", minValue)

        confirmButton.setOnClickListener {

            if (noSlideBar.progress<minValue) {
                noSlideBar.progress = minValue
            }

            States.dialogInt = noSlideBar.progress
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
        var number1 = no;
        if (number1<minValue) { number1=minValue};
        titleLabel.text = String.format("%d", number1) + sSuffix
    }


    /**
     * ok 리스너 등록
     */
    fun setOkListener(okClick: View.OnClickListener) {
        this.okClick = okClick
    }

}