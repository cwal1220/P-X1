package com.inspeco.dialog

import android.app.Dialog
import android.content.Context
import android.text.TextUtils
import android.view.View
import android.view.Window
import com.inspeco.X1.R
import kotlinx.android.synthetic.main.d_common.*

/**
 * 기본 다이얼로그
 */class CommonDialog(context: Context, title:String, msg:String, var ok:String?, var no:String?) : Dialog(context) {

    private var okClick: View.OnClickListener? = null
    private var cancelClick: View.OnClickListener? = null

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        //setCanceledOnTouchOutside(false)
        setCancelable(true)

        setContentView(R.layout.d_common)


        if (TextUtils.isEmpty(title)) {
            txt_title.visibility = View.GONE
        } else {
            txt_title.text = title
            txt_title.visibility = View.VISIBLE
        }

        txt_msg.text = msg

        if (TextUtils.isEmpty(ok)) {
            confirmButton.visibility = View.GONE
        } else {
            confirmButton.visibility = View.VISIBLE
            confirmButton.text = ok
        }

        confirmButton.setOnClickListener {
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

    }

    /**
     * ok 리스너 등록
     */
    fun setOkListener(okClick: View.OnClickListener) {
        this.okClick = okClick
    }

    /**
     * no 리스너 등록
     */
    fun setCancelListener(cancelClick: View.OnClickListener?) {
        this.cancelClick = cancelClick
    }
}