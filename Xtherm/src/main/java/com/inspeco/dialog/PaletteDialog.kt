package com.inspeco.dialog


import android.app.Dialog
import android.content.Context
import android.text.TextUtils
import android.view.View
import android.view.Window
import com.inspeco.X1.R
import kotlinx.android.synthetic.main.d_palette.*

/**
 * 기본 다이얼로그
 */
class PaletteDialog(context: Context) : Dialog(context) {

    private var sel01Click: View.OnClickListener? = null
    private var sel02Click: View.OnClickListener? = null
    private var sel03Click: View.OnClickListener? = null

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        setCanceledOnTouchOutside(false)
        setCancelable(true)

        setContentView(R.layout.d_palette)


        dp_sel_01.setOnClickListener {
            this.dismiss()
            sel01Click?.onClick(it)
        }


        dp_sel_02.setOnClickListener {
            this.dismiss()
            sel02Click?.onClick(it)
        }

        dp_sel_03.setOnClickListener {
            this.dismiss()
            sel03Click?.onClick(it)
        }
    }


    fun setSel01Listener(sel01: View.OnClickListener) {
        this.sel01Click = sel01
    }

    fun setSel02Listener(sel02: View.OnClickListener?) {
        this.sel02Click = sel02
    }

    fun setSel03Listener(sel03: View.OnClickListener?) {
        this.sel03Click = sel03
    }

}